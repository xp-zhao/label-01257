package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Warehouse;
import com.wms.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 库房管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public Result<PageResult<Warehouse>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(required = false) String warehouseName,
            @RequestParam(required = false) Integer status) {
        Page<Warehouse> page = warehouseService.queryPage(new Page<>(pageNum, pageSize), warehouseCode, warehouseName, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/all")
    public Result<List<Warehouse>> getAll() {
        return Result.success(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        return Result.success(warehouseService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Warehouse warehouse) {
        warehouseService.addWarehouse(warehouse);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Warehouse warehouse) {
        warehouse.setId(id);
        warehouseService.updateWarehouse(warehouse);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return Result.success();
    }

    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> getStats(@PathVariable Long id) {
        return Result.success(warehouseService.getWarehouseStats(id));
    }

    @GetMapping("/{id}/remaining-area")
    public Result<Map<String, Object>> getRemainingArea(
            @PathVariable Long id,
            @RequestParam(required = false) Long excludeAreaId) {
        return Result.success(warehouseService.getRemainingArea(id, excludeAreaId));
    }
}
