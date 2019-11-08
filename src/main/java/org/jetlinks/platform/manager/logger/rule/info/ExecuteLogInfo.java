package org.jetlinks.platform.manager.logger.rule.info;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author bsetfeng
 * @since 1.0
 **/
@Getter
@Setter
public class ExecuteLogInfo {

    private String instanceId;

    private String nodeId;

    private String level;

    private String message;

    private long createTime = System.currentTimeMillis();

    private long timestamp;

    //private List<Object> args;

    private String context;
}
