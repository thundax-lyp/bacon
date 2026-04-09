package com.github.thundax.bacon.storage.application.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 分片上传超时清理调度器。
 */
@Slf4j
@Component
public class MultipartUploadCleanupScheduler {

    private final MultipartUploadCleanupService multipartUploadCleanupService;

    public MultipartUploadCleanupScheduler(MultipartUploadCleanupService multipartUploadCleanupService) {
        this.multipartUploadCleanupService = multipartUploadCleanupService;
    }

    @Scheduled(
            fixedDelayString = "${bacon.storage.multipart-cleanup.fixed-delay-millis:600000}",
            initialDelayString = "${bacon.storage.multipart-cleanup.fixed-delay-millis:600000}")
    public void cleanupExpiredMultipartUploads() {
        int cleanedCount = multipartUploadCleanupService.cleanupExpiredSessions();
        if (cleanedCount > 0) {
            log.info("Multipart upload expired cleanup batch finished, cleanedCount={}", cleanedCount);
        }
    }
}
