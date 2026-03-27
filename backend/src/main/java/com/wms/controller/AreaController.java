package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.WarehouseArea;
import com.wms.service.WarehouseAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 区域管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/areas")
@RequiredArgsConstructor
public class AreaController {

    private final WarehouseAreaService areaService;

    @GetMapping
    public Result<PageResult<WarehouseArea>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String areaCode,
            @RequestParam(required = false) String areaType) {
        Page<WarehouseArea> page = areaService.queryPage(new Page<>(pageNum, pageSize), warehouseId, areaCode, areaType);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/warehouse/{warehouseId}")
    public Result<List<WarehouseArea>> getByWarehouse(@PathVariable Long warehouseId) {
        return Result.success(areaService.getAreasByWarehouseId(warehouseId));
    }

    @GetMapping("/{id}")
    public Result<WarehouseArea> getById(@PathVariable Long id) {
        return Result.success(areaService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody WarehouseArea area) {
        areaService.addArea(area);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody WarehouseArea area) {
        area.setId(id);
        areaService.updateArea(area);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        areaService.deleteArea(id);
        return Result.success();
    }
}
