package com.plumroc.springbootuploadminio.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIo配置类
 *
 * @author PlumRoc
 * @date 2022-11-12
 */
@Data
@Configuration
public class MinIoConfig {

    @Value("${spring.minio.endpoint}")
    private String endpoint;
    @Value("${spring.minio.accessKey}")
    private String accessKey;
    @Value("${spring.minio.secretKey}")
    private String secretKey;
    @Value("${spring.minio.bucketName}")
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
