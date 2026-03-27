package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Inventory;
import com.wms.entity.InventoryAdjust;
import com.wms.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 库存管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public Result<PageResult<Inventory>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String batchNo) {
        Page<Inventory> page = inventoryService.queryPage(new Page<>(pageNum, pageSize), warehouseId, productId, batchNo);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/warning")
    public Result<List<Inventory>> getWarningList() {
        return Result.success(inventoryService.getWarningList());
    }

    @PostMapping("/adjust")
    public Result<Void> adjust(@Valid @RequestBody InventoryAdjust adjust) {
        inventoryService.adjustInventory(adjust);
        return Result.success();
    }

    @GetMapping("/adjust")
    public Result<PageResult<InventoryAdjust>> adjustList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) String adjustType) {
        Page<InventoryAdjust> page = inventoryService.queryAdjustPage(new Page<>(pageNum, pageSize), productId, adjustType);
        return Result.success(PageResult.from(page));
    }
}
