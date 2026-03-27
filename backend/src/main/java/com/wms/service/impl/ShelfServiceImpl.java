package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.Inventory;
import com.wms.entity.Shelf;
import com.wms.entity.Warehouse;
import com.wms.entity.WarehouseArea;
import com.wms.mapper.InventoryMapper;
import com.wms.mapper.ShelfMapper;
import com.wms.mapper.WarehouseAreaMapper;
import com.wms.mapper.WarehouseMapper;
import com.wms.service.ShelfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShelfServiceImpl extends ServiceImpl<ShelfMapper, Shelf> implements ShelfService {

    private final WarehouseMapper warehouseMapper;
    private final WarehouseAreaMapper areaMapper;
    private final InventoryMapper inventoryMapper;

    @Override
    public Page<Shelf> queryPage(Page<Shelf> page, Long warehouseId, Long areaId, String shelfCode) {
        LambdaQueryWrapper<Shelf> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(warehouseId != null, Shelf::getWarehouseId, warehouseId)
                .eq(areaId != null, Shelf::getAreaId, areaId)
                .like(StringUtils.hasText(shelfCode), Shelf::getShelfCode, shelfCode)
                .orderByDesc(Shelf::getCreateTime);
        Page<Shelf> result = this.page(page, wrapper);
        result.getRecords().forEach(shelf -> {
            Warehouse warehouse = warehouseMapper.selectById(shelf.getWarehouseId());
            WarehouseArea area = areaMapper.selectById(shelf.getAreaId());
            if (warehouse != null) shelf.setWarehouseName(warehouse.getWarehouseName());
            if (area != null) shelf.setAreaName(area.getAreaName());
        });
        return result;
    }

    @Override
    public List<Shelf> getShelvesByAreaId(Long areaId) {
        return this.list(new LambdaQueryWrapper<Shelf>()
                .eq(Shelf::getAreaId, areaId)
                .eq(Shelf::getStatus, 1)
                .orderByAsc(Shelf::getShelfCode));
    }

    @Override
    public void addShelf(Shelf shelf) {
        this.save(shelf);
        log.info("新增货架成功: {}", shelf.getShelfName());
    }

    @Override
    public void updateShelf(Shelf shelf) {
        this.updateById(shelf);
        log.info("更新货架成功: {}", shelf.getShelfName());
    }

    @Override
    public void deleteShelf(Long id) {
        Shelf shelf = this.getById(id);
        if (shelf == null) {
            throw new BusinessException("货架不存在");
        }
        long invCount = inventoryMapper.selectCount(new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getShelfId, id));
        if (invCount > 0) {
            throw new BusinessException("该货架上有库存，不能删除");
        }
        this.removeById(id);
        log.info("删除货架成功: {}", shelf.getShelfName());
    }
}
