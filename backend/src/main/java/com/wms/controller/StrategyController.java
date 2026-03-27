package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.PickingStrategy;
import com.wms.entity.ReplenishStrategy;
import com.wms.entity.StorageStrategy;
import com.wms.service.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 策略管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;

    // ========== 存储策略 ==========
    @GetMapping("/storage")
    public Result<PageResult<StorageStrategy>> storageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String strategyName,
            @RequestParam(required = false) Long warehouseId) {
        Page<StorageStrategy> page = strategyService.queryStoragePage(new Page<>(pageNum, pageSize), strategyName, warehouseId);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/storage")
    public Result<Void> addStorage(@Valid @RequestBody StorageStrategy strategy) {
        strategyService.addStorageStrategy(strategy);
        return Result.success();
    }

    @PutMapping("/storage/{id}")
    public Result<Void> updateStorage(@PathVariable Long id, @Valid @RequestBody StorageStrategy strategy) {
        strategy.setId(id);
        strategyService.updateStorageStrategy(strategy);
        return Result.success();
    }

    @DeleteMapping("/storage/{id}")
    public Result<Void> deleteStorage(@PathVariable Long id) {
        strategyService.deleteStorageStrategy(id);
        return Result.success();
    }

    // ========== 拣货策略 ==========
    @GetMapping("/picking")
    public Result<PageResult<PickingStrategy>> pickingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String strategyName,
            @RequestParam(required = false) Long warehouseId) {
        Page<PickingStrategy> page = strategyService.queryPickingPage(new Page<>(pageNum, pageSize), strategyName, warehouseId);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/picking")
    public Result<Void> addPicking(@Valid @RequestBody PickingStrategy strategy) {
        strategyService.addPickingStrategy(strategy);
        return Result.success();
    }

    @PutMapping("/picking/{id}")
    public Result<Void> updatePicking(@PathVariable Long id, @Valid @RequestBody PickingStrategy strategy) {
        strategy.setId(id);
        strategyService.updatePickingStrategy(strategy);
        return Result.success();
    }

    @DeleteMapping("/picking/{id}")
    public Result<Void> deletePicking(@PathVariable Long id) {
        strategyService.deletePickingStrategy(id);
        return Result.success();
    }

    // ========== 补货策略 ==========
    @GetMapping("/replenish")
    public Result<PageResult<ReplenishStrategy>> replenishList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String strategyName,
            @RequestParam(required = false) Long warehouseId) {
        Page<ReplenishStrategy> page = strategyService.queryReplenishPage(new Page<>(pageNum, pageSize), strategyName, warehouseId);
        return Result.success(PageResult.from(page));
    }

    @PostMapping("/replenish")
    public Result<Void> addReplenish(@Valid @RequestBody ReplenishStrategy strategy) {
        strategyService.addReplenishStrategy(strategy);
        return Result.success();
    }

    @PutMapping("/replenish/{id}")
    public Result<Void> updateReplenish(@PathVariable Long id, @Valid @RequestBody ReplenishStrategy strategy) {
        strategy.setId(id);
        strategyService.updateReplenishStrategy(strategy);
        return Result.success();
    }

    @DeleteMapping("/replenish/{id}")
    public Result<Void> deleteReplenish(@PathVariable Long id) {
        strategyService.deleteReplenishStrategy(id);
        return Result.success();
    }
}
