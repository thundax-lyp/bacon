package com.github.thundax.bacon.common.id.core;

public enum IdProviderType {
    TINYID,
    LEAF;

    public static IdProviderType from(String provider) {
        if (provider == null || provider.isBlank()) {
            return TINYID;
        }
        try {
            return IdProviderType.valueOf(provider.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TINYID;
        }
    }
}
