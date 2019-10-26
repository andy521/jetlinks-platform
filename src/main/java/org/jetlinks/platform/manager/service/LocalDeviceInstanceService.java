package org.jetlinks.platform.manager.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.exception.NotFoundException;
import org.jetlinks.core.device.DeviceRegistry;
import org.jetlinks.core.utils.FluxUtils;
import org.jetlinks.platform.events.DeviceConnectedEvent;
import org.jetlinks.platform.manager.entity.DeviceInstanceEntity;
import org.jetlinks.platform.manager.entity.DeviceProductEntity;
import org.jetlinks.platform.manager.entity.DevicePropertiesEntity;
import org.jetlinks.platform.manager.web.response.DeviceInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LocalDeviceInstanceService extends GenericReactiveCrudService<DeviceInstanceEntity, String> {

    @Autowired
    private DeviceRegistry registry;

    @Autowired
    private LocalDeviceProductService deviceProductService;

    @Autowired
    private LocalDevicePropertiesService propertiesService;


    public void deploy(String id) {

    }

    private volatile FluxSink<String> deviceIdSink;

    @PostConstruct
    public void init() {
        syncState(Flux.create(fluxSink -> deviceIdSink = fluxSink), false)
                .doOnError(err -> {
                    log.error(err.getMessage(), err);
                })
                .subscribe((i) -> {
                    log.info("同步设备状态成功:{}", i);
                });
    }

    @EventListener
    public void handleDeviceConnectEvent(DeviceConnectedEvent event) {
        if (deviceIdSink != null) {
            deviceIdSink.next(event.getSession().getDeviceId());
        } else {
            syncState(Flux.just(event.getSession().getDeviceId()), false)
                    .doOnError(err -> log.error(err.getMessage(), err))
                    .subscribe((i) -> log.info("同步设备状态成功"));
        }
    }

    public Mono<DeviceInfo> getDeviceInfoById(String id) {
        return findById(Mono.just(id))
                .zipWhen(instance -> deviceProductService
                        .findById(Mono.just(instance.getProductId())), DeviceInfo::of)
                .switchIfEmpty(Mono.error(NotFoundException::new));
//        return findById(Mono.just(id))
//                .zipWhen(instance -> deviceProductService
//                                .findById(Mono.just(instance.getProductId()))
//                                .zipWith(propertiesService.createQuery()
//                                        .where(DevicePropertiesEntity::getDeviceId, id)
//                                        .fetch()
//                                        .collectList()),
//                        ((deviceInstanceEntity, tuple) -> DeviceInfo.of(deviceInstanceEntity, tuple.getT1(), tuple.getT2())))
//                .switchIfEmpty(Mono.error(NotFoundException::new));
    }

    /**
     * 同步设备状态
     *
     * @param deviceId 设备id集合
     * @param force    是否强制同步,将会检查设备的真实状态
     * @return 同步成功数量
     */
    public Flux<Integer> syncState(Flux<String> deviceId, boolean force) {

        return FluxUtils.bufferRate(deviceId.
                        flatMap(registry::getDevice)
                        .publishOn(Schedulers.parallel())
                        .flatMap(operation -> {
                            if (force) {
                                return operation.checkState()
                                        .zipWith(Mono.just(operation.getDeviceId()));
                            }
                            return operation.getState()
                                    .zipWith(Mono.just(operation.getDeviceId()));
                        })
                , 800, Duration.ofSeconds(5))
                .map(list -> list.stream().collect(Collectors.groupingBy(Tuple2::getT1, Collectors.mapping(Tuple2::getT2, Collectors.toSet()))))
                .map(Map::entrySet)
                .flatMap(Flux::fromIterable)
                .flatMap(e -> getRepository().createUpdate()
                        .set(DeviceInstanceEntity::getState, org.jetlinks.platform.manager.enums.DeviceState.of(e.getKey()))
                        .where().in(DeviceInstanceEntity::getId, e.getValue())
                        .execute());

    }

//    public void updateRegistry(DeviceInstanceEntity entity) {
//        Runnable runnable = () -> {
//            DeviceProductEntity productEntity = productService.selectByPk(entity.getProductId());
//
//            logger.info("update device instance[{}:{}] registry info", entity.getId(), entity.getName());
//            DeviceInfo productInfo = new DeviceInfo();
//            productInfo.setId(entity.getId());
//            if (null != productEntity) {
//                productInfo.setProductId(productEntity.getId());
//                productInfo.setProductName(productEntity.getName());
//                productInfo.setProtocol(productEntity.getMessageProtocol());
//            }
//
//            productInfo.setName(entity.getName());
//            productInfo.setCreatorId(entity.getCreatorId());
//            productInfo.setCreatorName(entity.getCreatorName());
//            productInfo.setProjectId(entity.getProductId());
//            productInfo.setProjectName(entity.getProductName());
//
//            DeviceOperation operation = registry.getDevice(entity.getId());
//            operation.update(productInfo);
//
//            Optional.ofNullable(entity.getDeriveMetadata())
//                    .ifPresent(operation::updateMetadata);
//
//            if (operation.getState() == DeviceState.unknown) {
//                operation.putState(DeviceState.noActive);
//            }
//            //自定义配置
//
//            ofNullable(entity.getSecurity())
//                    .ifPresent(operation::putAll);
//        };
//        if (TransactionSynchronizationManager.isSynchronizationActive()) {
//            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
//                @Override
//                public void afterCommit() {
//                    runnable.run();
//                }
//            });
//        } else {
//            runnable.run();
//        }
//    }
}
