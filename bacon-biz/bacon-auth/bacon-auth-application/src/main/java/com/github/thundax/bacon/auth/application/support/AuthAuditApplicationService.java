package com.github.thundax.bacon.auth.application.support;

import org.springframework.stereotype.Service;

@Service
public class AuthAuditApplicationService {

    public void record(String actionType, String resultStatus, String summary) {}
}
