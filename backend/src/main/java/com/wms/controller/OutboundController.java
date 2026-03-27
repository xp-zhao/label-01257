package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.OutboundOrder;
import com.wms.service.OutboundService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 出库管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @GetMapping
    public Result<PageResult<OutboundOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Integer status) {
        Page<OutboundOrder> page = outboundService.queryPage(new Page<>(pageNum, pageSize), orderNo, warehouseId, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/{id}")
    public Result<OutboundOrder> getById(@PathVariable Long id) {
        return Result.success(outboundService.getDetail(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody OutboundOrder order) {
        outboundService.createOrder(order);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OutboundOrder order) {
        order.setId(id);
        outboundService.updateOrder(order);
        return Result.success();
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id) {
        outboundService.submitOrder(id);
        return Result.success();
    }

    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        boolean approved = (boolean) params.getOrDefault("approved", true);
        String auditorName = (String) params.getOrDefault("auditorName", "");
        outboundService.auditOrder(id, approved, auditorName);
        return Result.success();
    }

    @PostMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        outboundService.confirmOutbound(id);
        return Result.success();
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        outboundService.cancelOrder(id);
        return Result.success();
    }
}
