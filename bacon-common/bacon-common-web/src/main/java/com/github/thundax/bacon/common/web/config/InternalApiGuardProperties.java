package com.github.thundax.bacon.common.web.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 内部 Provider 接口鉴权配置。
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "bacon.web.internal-api")
public class InternalApiGuardProperties {

    /** 是否启用内部接口鉴权。 */
    private boolean enabled = false;

    /** 内部调用 token 请求头。 */
    private String headerName = "X-Bacon-Provider-Token";

    /** 内部调用共享 token。 */
    private String token;

    /** 需要保护的路径模式。 */
    private List<String> includePathPatterns = List.of("/providers/**");
}
