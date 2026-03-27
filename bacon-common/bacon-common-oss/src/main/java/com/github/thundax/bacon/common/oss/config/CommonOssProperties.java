package com.github.thundax.bacon.common.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 通用对象存储配置。
 */
@Data
@ConfigurationProperties(prefix = "bacon.common.oss")
public class CommonOssProperties {

    /** 是否启用通用对象存储客户端。 */
    private boolean enabled = false;
    /** 存储类型，当前仅支持 S3_API。 */
    private String type = "S3_API";
    /** S3 API 服务端点，例如 MinIO 或兼容 S3 的对象存储网关。 */
    private String endpoint;
    /** 外部访问基地址，为空时回退到 endpoint/bucket。 */
    private String publicBaseUrl;
    /** 桶名称。 */
    private String bucketName;
    /** Access Key。 */
    private String accessKey;
    /** Secret Key。 */
    private String secretKey;
    /** 区域，默认 us-east-1。 */
    private String region = "us-east-1";
    /** 是否启用 path-style 访问。 */
    private boolean pathStyleAccess = true;
}
