package org.jetlinks.platform.manager.logger.rule;

import org.hswebframework.web.bean.FastBeanCopier;
import org.jetlinks.core.utils.FluxUtils;
import org.jetlinks.platform.manager.elasticsearch.ElasticsearchSaveService;
import org.jetlinks.platform.manager.enums.EsDataType;
import org.jetlinks.platform.manager.logger.rule.info.ExecuteEventInfo;
import org.jetlinks.platform.manager.logger.rule.info.ExecuteLogInfo;
import org.jetlinks.rule.engine.api.events.NodeExecuteEvent;
import org.jetlinks.rule.engine.api.events.RuleEvent;
import org.jetlinks.rule.engine.cluster.logger.LogInfo;
import org.jetlinks.rule.engine.cluster.worker.NodeExecuteLogEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import javax.annotation.PostConstruct;
import java.time.Duration;

@Component
public class RuleLogHandler {

    private final ElasticsearchSaveService saveService;

    private volatile FluxSink<ExecuteEventInfo> eventInfoFluxSink;

    private volatile FluxSink<ExecuteLogInfo> logInfoFluxSink;

    @Autowired
    public RuleLogHandler(ElasticsearchSaveService saveService) {
        this.saveService = saveService;
    }

    @EventListener
    public void handleRuleLog(LogInfo event) {
        ExecuteLogInfo logInfo = FastBeanCopier.copy(event, new ExecuteLogInfo());
        if (logInfoFluxSink != null) {
            logInfoFluxSink.next(logInfo);
        } else {
            collectLogInfo(Flux.just(logInfo));
        }
    }

    @EventListener
    public void handleRuleExecuteEvent(NodeExecuteEvent event) {
        //不记录BEFORE和RESULT事件
        if (!RuleEvent.NODE_EXECUTE_BEFORE.equals(event.getEvent())&&!RuleEvent.NODE_EXECUTE_RESULT.equals(event.getEvent())) {
            ExecuteEventInfo eventInfo = FastBeanCopier.copy(event, new ExecuteEventInfo());
            if (eventInfoFluxSink != null) {
                eventInfoFluxSink.next(eventInfo);
            } else {
                collectEventInfo(Flux.just(eventInfo));
            }
        }
    }


    @PostConstruct
    public void init() {
        collectLogInfo(Flux.create(fluxSink -> logInfoFluxSink = fluxSink));
        collectEventInfo(Flux.create(fluxSink -> eventInfoFluxSink = fluxSink));
    }

    private void collectLogInfo(Flux<ExecuteLogInfo> logInfoFlux) {
        FluxUtils.bufferRate(logInfoFlux, 800, Duration.ofSeconds(2))
                .subscribe(data -> saveService.asyncBulkSave(data, EsDataType.EXECUTE_LOG_INDEX));
    }

    private void collectEventInfo(Flux<ExecuteEventInfo> eventInfoFlux) {
        FluxUtils.bufferRate(eventInfoFlux, 800, Duration.ofSeconds(2))
                .subscribe(data -> saveService.asyncBulkSave(data, EsDataType.EXECUTE_EVENT_LOG_INDEX));
    }


}
