package com.github.thundax.bacon.inventory.infra.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.github.thundax.bacon.inventory.infra.persistence.dataobject.InventoryDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface InventoryMapper extends BaseMapper<InventoryDO> {

    @Select("""
            <script>
            select id, tenant_id, sku_id, warehouse_no, on_hand_quantity, reserved_quantity, available_quantity,
                   status, version, created_by, created_at, updated_by, updated_at
            from bacon_inventory_inventory
            where tenant_id = #{tenantId}
            <if test="skuId != null">
                and sku_id = #{skuId}
            </if>
            <if test="status != null">
                and status = #{status}
            </if>
            order by sku_id asc
            limit #{offset}, #{pageSize}
            </script>
            """)
    java.util.List<InventoryDO> selectPageByCondition(@Param("tenantId") Long tenantId,
                                                      @Param("skuId") Long skuId,
                                                      @Param("status") String status,
                                                      @Param("offset") long offset,
                                                      @Param("pageSize") int pageSize);

    @Select("""
            <script>
            select count(1)
            from bacon_inventory_inventory
            where tenant_id = #{tenantId}
            <if test="skuId != null">
                and sku_id = #{skuId}
            </if>
            <if test="status != null">
                and status = #{status}
            </if>
            </script>
            """)
    long countByCondition(@Param("tenantId") Long tenantId,
                          @Param("skuId") Long skuId,
                          @Param("status") String status);
}
