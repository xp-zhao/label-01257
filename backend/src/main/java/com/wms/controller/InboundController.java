package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.InboundOrder;
import com.wms.service.InboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 入库管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @GetMapping
    public Result<PageResult<InboundOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status) {
        Page<InboundOrder> page = inboundService.queryPage(new Page<>(pageNum, pageSize), orderNo, warehouseId, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/{id}")
    public Result<InboundOrder> getById(@PathVariable Long id) {
        return Result.success(inboundService.getDetail(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody InboundOrder order) {
        inboundService.createOrder(order);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody InboundOrder order) {
        order.setId(id);
        inboundService.updateOrder(order);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        inboundService.submitOrder(id);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        inboundService.confirmInbound(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        inboundService.cancelOrder(id);
        return Result.success();
    }
}
