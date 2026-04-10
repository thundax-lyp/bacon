package com.github.thundax.bacon.upms.interfaces.consumer;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.upms.application.audit.SysLogConsumeApplicationService;
import org.springframework.stereotype.Component;

@Component
public class SysLogMqConsumer {

    private final SysLogConsumeApplicationService sysLogConsumeApplicationService;

    public SysLogMqConsumer(SysLogConsumeApplicationService sysLogConsumeApplicationService) {
        this.sysLogConsumeApplicationService = sysLogConsumeApplicationService;
    }

    /**
     * Bind this method from the concrete MQ listener implementation in UPMS.
     */
    public void consume(SysLogDTO sysLogDTO) {
        sysLogConsumeApplicationService.consume(sysLogDTO);
    }

    public void consume(Long tenantId, SysLogDTO sysLogDTO) {
        BaconContextHolder.runWithTenantId(tenantId, () -> sysLogConsumeApplicationService.consume(sysLogDTO));
    }
}
