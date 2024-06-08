package cn.whu.file.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@Data
@ConfigurationProperties(prefix = "minio")  // 文件上传 配置前缀file.oss
public class MinIOConfigProperties implements Serializable {

    private String accessKey;//application.yml等配置里的minio.accessKey
    private String secretKey;//application.yml等配置里的minio.secretKey
    private String bucket;//application.yml等配置里的minio.bucket
    private String endpoint;//application.yml等配置里的minio.endpoint
    private String readPath;//application.yml等配置里的minio.readPath
}
