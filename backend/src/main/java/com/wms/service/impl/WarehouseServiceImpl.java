package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.Inventory;
import com.wms.entity.Warehouse;
import com.wms.entity.WarehouseArea;
import com.wms.mapper.InventoryMapper;
import com.wms.mapper.WarehouseAreaMapper;
import com.wms.mapper.WarehouseMapper;
import com.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库房服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    private final WarehouseAreaMapper areaMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public Page<Warehouse> queryPage(Page<Warehouse> page, String warehouseCode, String warehouseName, Integer status) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(warehouseCode), Warehouse::getWarehouseCode, warehouseCode)
                .like(StringUtils.hasText(warehouseName), Warehouse::getWarehouseName, warehouseName)
                .eq(status != null, Warehouse::getStatus, status)
                .orderByDesc(Warehouse::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return this.list(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getStatus, 1)
                .orderByAsc(Warehouse::getWarehouseCode));
    }

    @Override
    public void addWarehouse(Warehouse warehouse) {
        long count = this.count(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWarehouseCode, warehouse.getWarehouseCode()));
        if (count > 0) {
            throw new BusinessException("库房编码已存在");
        }
        this.save(warehouse);
        log.info("新增库房成功: {}", warehouse.getWarehouseName());
    }

    @Override
    public void updateWarehouse(Warehouse warehouse) {
        Warehouse existing = this.getById(warehouse.getId());
        if (existing == null) {
            throw new BusinessException("库房不存在");
        }
        long count = this.count(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getWarehouseCode, warehouse.getWarehouseCode())
                .ne(Warehouse::getId, warehouse.getId()));
        if (count > 0) {
            throw new BusinessException("库房编码已存在");
        }
        this.updateById(warehouse);
        log.info("更新库房成功: {}", warehouse.getWarehouseName());
    }

    @Override
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new BusinessException("库房不存在");
        }
        // 检查是否有区域
        long areaCount = areaMapper.selectCount(new LambdaQueryWrapper<WarehouseArea>()
                .eq(WarehouseArea::getWarehouseId, id));
        if (areaCount > 0) {
            throw new BusinessException("该库房下有区域，不能删除");
        }
        this.removeById(id);
        log.info("删除库房成功: {}", warehouse.getWarehouseName());
    }

    @Override
    public Map<String, Object> getWarehouseStats(Long id) {
        Warehouse warehouse = this.getById(id);
        if (warehouse == null) {
            throw new BusinessException("库房不存在");
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("warehouse", warehouse);

        // 区域数量
        long areaCount = areaMapper.selectCount(new LambdaQueryWrapper<WarehouseArea>()
                .eq(WarehouseArea::getWarehouseId, id));
        stats.put("areaCount", areaCount);

        // 库存统计
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getWarehouseId, id));
        int totalQuantity = inventories.stream().mapToInt(Inventory::getQuantity).sum();
        int productCount = (int) inventories.stream().map(Inventory::getProductId).distinct().count();
        stats.put("totalQuantity", totalQuantity);
        stats.put("productCount", productCount);

        return stats;
    }

    @Override
    public Map<String, Object> getRemainingArea(Long warehouseId, Long excludeAreaId) {
        Warehouse warehouse = this.getById(warehouseId);
        if (warehouse == null) {
            throw new BusinessException("库房不存在");
        }

        Map<String, Object> result = new HashMap<>();
        BigDecimal totalArea = warehouse.getTotalArea() != null ? warehouse.getTotalArea() : BigDecimal.ZERO;
        result.put("totalArea", totalArea);

        // 计算已用面积（排除指定区域）
        LambdaQueryWrapper<WarehouseArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarehouseArea::getWarehouseId, warehouseId);
        if (excludeAreaId != null) {
            wrapper.ne(WarehouseArea::getId, excludeAreaId);
        }
        List<WarehouseArea> areas = areaMapper.selectList(wrapper);
        
        BigDecimal usedArea = areas.stream()
                .map(WarehouseArea::getAreaSize)
                .filter(size -> size != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        result.put("usedArea", usedArea);
        result.put("remainingArea", totalArea.subtract(usedArea));

        return result;
    }
}
