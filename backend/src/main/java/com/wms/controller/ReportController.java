package com.wms.controller;

import com.wms.common.Result;
import com.wms.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 报表管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 仪表盘统计
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(reportService.getDashboardStats());
    }

    /**
     * 库存汇总报表
     */
    @GetMapping("/inventory/summary")
    public Result<List<Map<String, Object>>> getInventorySummary(
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(reportService.getInventorySummary(warehouseId));
    }

    /**
     * 库存明细报表
     */
    @GetMapping("/inventory/detail")
    public Result<List<Map<String, Object>>> getInventoryDetail(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId) {
        return Result.success(reportService.getInventoryDetail(warehouseId, productId));
    }

    /**
     * 入库汇总报表
     */
    @GetMapping("/inbound/summary")
    public Result<List<Map<String, Object>>> getInboundSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getInboundSummary(startDate, endDate));
    }

    /**
     * 出库汇总报表
     */
    @GetMapping("/outbound/summary")
    public Result<List<Map<String, Object>>> getOutboundSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(reportService.getOutboundSummary(startDate, endDate));
    }

    /**
     * 周转率分析
     */
    @GetMapping("/turnover")
    public Result<List<Map<String, Object>>> getTurnover(
            @RequestParam(required = false) Long warehouseId) {
        return Result.success(reportService.getTurnoverAnalysis(warehouseId));
    }

    /**
     * 异常预警报表
     */
    @GetMapping("/warning")
    public Result<List<Map<String, Object>>> getWarning() {
        return Result.success(reportService.getWarningReport());
    }
}
