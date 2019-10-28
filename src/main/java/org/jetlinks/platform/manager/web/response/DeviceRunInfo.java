package org.jetlinks.platform.manager.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetlinks.platform.manager.enums.DeviceState;

/**
 * @author bsetfeng
 * @since 1.0
 **/
@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class DeviceRunInfo {

    private long onlineTime;

    private long offlineTime;

    //设备状态
    private DeviceState state;
}
