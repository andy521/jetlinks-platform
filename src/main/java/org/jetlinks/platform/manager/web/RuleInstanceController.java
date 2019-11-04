package org.jetlinks.platform.manager.web;

import com.sun.deploy.security.ruleset.DefaultRule;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.ResourceAction;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.id.IDGenerator;
import org.jetlinks.platform.manager.entity.RuleInstanceEntity;
import org.jetlinks.platform.manager.service.RuleInstanceService;
import org.jetlinks.rule.engine.api.DefaultRuleData;
import org.jetlinks.rule.engine.api.RuleData;
import org.jetlinks.rule.engine.api.RuleDataHelper;
import org.jetlinks.rule.engine.api.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("rule-engine/instance")
@Resource(id = "rule-instance", name = "规则引擎-实例")
public class RuleInstanceController implements ReactiveServiceCrudController<RuleInstanceEntity, String> {

    @Autowired
    private RuleInstanceService instanceService;

    @Autowired
    private RuleEngine ruleEngine;

    @PostMapping("/{id}/_start")
    @ResourceAction(id = "start", name = "启动")
    public Mono<Boolean> start(@PathVariable String id) {
        return instanceService.start(id)
                .thenReturn(true);
    }

    @PostMapping("/{id}/_stop")
    @ResourceAction(id = "stop", name = "停止")
    public Mono<Boolean> stop(@PathVariable String id) {
        return instanceService.stop(id)
                .thenReturn(true);
    }

    @PostMapping("/{id}/_execute/{startWith}/{endWith}")
    @ResourceAction(id = "execute", name = "执行")
    public Flux<Object> execute(@PathVariable String id,
                                @PathVariable String startWith,
                                @PathVariable String endWith,
                                @RequestBody Flux<DefaultRuleData> payload) {
        return ruleEngine
                .getInstance(id)
                .switchIfEmpty(Mono.error(NotFoundException::new))
                .flatMapMany(context -> context
                        .execute(payload
                                .map(ruleData -> {
                                    ruleData.setId(IDGenerator.SNOW_FLAKE_STRING.generate());
                                    RuleDataHelper.markStartWith(ruleData, startWith);
                                    return RuleDataHelper.markSyncReturn(ruleData, endWith);
                                })
                        ));
    }

    @Override
    public ReactiveCrudService<RuleInstanceEntity, String> getService() {
        return instanceService;
    }
}
