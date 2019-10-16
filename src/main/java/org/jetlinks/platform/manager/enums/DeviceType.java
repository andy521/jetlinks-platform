package org.jetlinks.platform.manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.dict.Dict;
import org.hswebframework.web.dict.EnumDict;

@AllArgsConstructor
@Getter
@Dict("device-type")
public enum DeviceType implements EnumDict<String> {
    device("设备"),
    gateway("网关")
    ;

    private String text;

    @Override
    public String getValue() {
        return name();
    }


}
