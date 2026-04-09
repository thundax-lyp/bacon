package com.github.thundax.bacon.common.core.util;

public final class PageParamNormalizer {

    public static final int DEFAULT_PAGE_NO = 1;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 200;

    private PageParamNormalizer() {
    }

    public static int normalizePageNo(Integer pageNo) {
        return pageNo == null || pageNo < 1 ? DEFAULT_PAGE_NO : pageNo;
    }

    public static int normalizePageSize(Integer pageSize) {
        return normalizePageSize(pageSize, DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
    }

    public static int normalizePageSize(Integer pageSize, int defaultPageSize) {
        return normalizePageSize(pageSize, defaultPageSize, MAX_PAGE_SIZE);
    }

    public static int normalizePageSize(Integer pageSize, int defaultPageSize, int maxPageSize) {
        int normalizedDefaultPageSize = Math.max(defaultPageSize, 1);
        int normalizedMaxPageSize = Math.max(maxPageSize, normalizedDefaultPageSize);
        if (pageSize == null || pageSize < 1) {
            return normalizedDefaultPageSize;
        }
        return Math.min(pageSize, normalizedMaxPageSize);
    }
}
