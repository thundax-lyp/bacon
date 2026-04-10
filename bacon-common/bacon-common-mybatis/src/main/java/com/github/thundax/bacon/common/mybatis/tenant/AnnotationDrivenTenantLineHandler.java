package com.github.thundax.bacon.common.mybatis.tenant;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

public class AnnotationDrivenTenantLineHandler implements TenantLineHandler {

    private static final String TENANT_ID_COLUMN = "tenant_id";

    private final Map<String, Boolean> tenantReadTableCache = new ConcurrentHashMap<>();

    @Override
    public Expression getTenantId() {
        Long tenantId = BaconContextHolder.currentTenantId();
        return new LongValue(tenantId == null ? -1L : tenantId);
    }

    @Override
    public String getTenantIdColumn() {
        return TENANT_ID_COLUMN;
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (BaconContextHolder.currentTenantId() == null) {
            return true;
        }
        return !tenantReadTableCache.computeIfAbsent(tableName, this::isTenantReadEnabled);
    }

    private boolean isTenantReadEnabled(String tableName) {
        TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
        if (tableInfo == null || tableInfo.getEntityType() == null) {
            return false;
        }
        return TenantScopeSupport.isReadEnabled(tableInfo.getEntityType());
    }
}
