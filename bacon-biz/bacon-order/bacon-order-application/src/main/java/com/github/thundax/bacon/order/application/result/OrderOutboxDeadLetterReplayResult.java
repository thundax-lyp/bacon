package com.github.thundax.bacon.order.application.result;

public record OrderOutboxDeadLetterReplayResult(Long deadLetterId, String replayStatus, String message) {}
