package org.jetlinks.platform.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.jetlinks.platform.manager.entity.RuleInstanceEntity;
import org.jetlinks.platform.manager.enums.RuleInstanceState;
import org.jetlinks.rule.engine.api.Rule;
import org.jetlinks.rule.engine.api.RuleEngine;
import org.jetlinks.rule.engine.api.RuleInstanceContext;
import org.jetlinks.rule.engine.api.model.RuleEngineModelParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RuleInstanceService extends GenericReactiveCrudService<RuleInstanceEntity, String> implements CommandLineRunner {

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private RuleEngineModelParser modelParser;

    public Mono<Void> stop(String id) {
        return this.ruleEngine
                .getInstance(id)
                .flatMap(RuleInstanceContext::stop)
                .switchIfEmpty(Mono.empty())
                .then(createUpdate()
                        .set(RuleInstanceEntity::getState, RuleInstanceState.stopped)
                        .where(RuleInstanceEntity::getId,id)
                        .execute())
                .then();
    }

    public Mono<RuleInstanceContext> start(String id) {
        return findById(Mono.just(id))
                .flatMap(this::doStart);
    }

    private Mono<RuleInstanceContext> doStart(RuleInstanceEntity entity) {
        return Mono.defer(() -> {
            Rule rule = entity.toRule(modelParser);
            return ruleEngine.startRule(rule)
                    .flatMap(ctx -> createUpdate()
                            .set(RuleInstanceEntity::getState, RuleInstanceState.started)
                            .where(entity::getId)
                            .execute()
                            .thenReturn(ctx));
        });
    }

    @Override
    public void run(String... args) {
        createQuery()
                .where()
                .is(RuleInstanceEntity::getState, RuleInstanceState.started)
                .fetch()
                .flatMap(this::doStart)
                .subscribe(context -> {
                    log.debug("start rule {}", context.getId());
                });
    }
}
