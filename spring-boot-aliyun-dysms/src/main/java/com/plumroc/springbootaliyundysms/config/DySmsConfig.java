package com.plumroc.springbootaliyundysms.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author PlumRoc
 */
@Data
@Configuration
public class DySmsConfig {

    @Value("${tools.aliyun.access-key}")
    private String accessKey;

    @Value("${tools.aliyun.access-key-secret}")
    private String accessSecret;

    @Value("${tools.aliyun.sms.template-code}")
    private String templateCode;

    @Value("${tools.aliyun.sms.sign-name}")
    private String signName;

    @Value("${tools.aliyun.sms.region}")
    private String region;


}
