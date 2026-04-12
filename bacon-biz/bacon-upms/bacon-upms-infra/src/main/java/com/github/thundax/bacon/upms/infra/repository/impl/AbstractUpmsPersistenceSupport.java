package com.github.thundax.bacon.upms.infra.repository.impl;

abstract class AbstractUpmsPersistenceSupport {

    protected final String limit(int pageNo, int pageSize) {
        int safePageNo = Math.max(pageNo, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNo - 1) * safePageSize;
        return "limit " + offset + "," + safePageSize;
    }

    protected final boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    protected final String trim(String value) {
        return value == null ? null : value.trim();
    }
}
