package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.Shelf;
import com.wms.entity.Warehouse;
import com.wms.entity.WarehouseArea;
import com.wms.mapper.ShelfMapper;
import com.wms.mapper.WarehouseAreaMapper;
import com.wms.mapper.WarehouseMapper;
import com.wms.service.WarehouseAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseAreaServiceImpl extends ServiceImpl<WarehouseAreaMapper, WarehouseArea> implements WarehouseAreaService {

    private final WarehouseMapper warehouseMapper;
    private final ShelfMapper shelfMapper;

    @Override
    public Page<WarehouseArea> queryPage(Page<WarehouseArea> page, Long warehouseId, String areaCode, String areaType) {
        LambdaQueryWrapper<WarehouseArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, WarehouseArea::getWarehouseId, warehouseId)
                .like(StringUtils.hasText(areaCode), WarehouseArea::getAreaCode, areaCode)
                .eq(StringUtils.hasText(areaType), WarehouseArea::getAreaType, areaType)
                .orderByDesc(WarehouseArea::getCreateTime);
        Page<WarehouseArea> result = this.page(page, wrapper);
        // 填充库房名称
        result.getRecords().forEach(area -> {
            Warehouse warehouse = warehouseMapper.selectById(area.getWarehouseId());
            if (warehouse != null) {
                area.setWarehouseName(warehouse.getWarehouseName());
            }
        });
        return result;
    }

    @Override
    public List<WarehouseArea> getAreasByWarehouseId(Long warehouseId) {
        return this.list(new LambdaQueryWrapper<WarehouseArea>()
                .eq(WarehouseArea::getWarehouseId, warehouseId)
                .eq(WarehouseArea::getStatus, 1)
                .orderByAsc(WarehouseArea::getAreaCode));
    }

    @Override
    public void addArea(WarehouseArea area) {
        // 校验区域面积不超过库房剩余面积
        validateAreaSize(area, null);
        this.save(area);
        log.info("新增区域成功: {}", area.getAreaName());
    }

    @Override
    public void updateArea(WarehouseArea area) {
        // 校验区域面积不超过库房剩余面积（排除当前区域原有面积）
        validateAreaSize(area, area.getId());
        this.updateById(area);
        log.info("更新区域成功: {}", area.getAreaName());
    }

    /**
     * 校验区域面积是否超过库房剩余面积
     * @param area 区域信息
     * @param excludeAreaId 需要排除的区域ID（编辑时排除自身）
     */
    private void validateAreaSize(WarehouseArea area, Long excludeAreaId) {
        if (area.getAreaSize() == null || area.getWarehouseId() == null) {
            return;
        }
        
        // 获取库房信息
        Warehouse warehouse = warehouseMapper.selectById(area.getWarehouseId());
        if (warehouse == null) {
            throw new BusinessException("库房不存在");
        }
        
        BigDecimal warehouseTotalArea = warehouse.getTotalArea();
        if (warehouseTotalArea == null) {
            return; // 库房未设置总面积，不校验
        }
        
        // 计算该库房下所有区域的面积之和（排除当前编辑的区域）
        LambdaQueryWrapper<WarehouseArea> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WarehouseArea::getWarehouseId, area.getWarehouseId());
        if (excludeAreaId != null) {
            wrapper.ne(WarehouseArea::getId, excludeAreaId);
        }
        List<WarehouseArea> existingAreas = this.list(wrapper);
        
        BigDecimal usedArea = existingAreas.stream()
                .map(WarehouseArea::getAreaSize)
                .filter(size -> size != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 计算剩余可用面积
        BigDecimal remainingArea = warehouseTotalArea.subtract(usedArea);
        
        // 校验新增/修改的区域面积是否超过剩余面积
        if (area.getAreaSize().compareTo(remainingArea) > 0) {
            throw new BusinessException("区域面积超过库房剩余可用面积，库房剩余面积为 " + remainingArea + " ㎡");
        }
    }

    @Override
    public void deleteArea(Long id) {
        WarehouseArea area = this.getById(id);
        if (area == null) {
            throw new BusinessException("区域不存在");
        }
        long shelfCount = shelfMapper.selectCount(new LambdaQueryWrapper<Shelf>()
                .eq(Shelf::getAreaId, id));
        if (shelfCount > 0) {
            throw new BusinessException("该区域下有货架，不能删除");
        }
        this.removeById(id);
        log.info("删除区域成功: {}", area.getAreaName());
    }
}
