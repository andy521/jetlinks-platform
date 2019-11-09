package org.jetlinks.platform.manager.notify.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @author bsetfeng
 * @since 1.0
 **/
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DefaultEmailTemplate {

    //附件 key:附件名称 value:附件uri
    private Map<String, Object> attachments;

    //图片 key:text中图片占位符 value:图片uri
    private Map<String, Object> images;

    private String subject;

    private String text;
}
