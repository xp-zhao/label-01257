package com.wms.service;

import java.util.List;
import java.util.Map;

/**
 * 报表服务接口
 *
 * @author WMS
 */
public interface ReportService {

    /**
     * 库存汇总报表
     */
    List<Map<String, Object>> getInventorySummary(Long warehouseId);

    /**
     * 库存明细报表
     */
    List<Map<String, Object>> getInventoryDetail(Long warehouseId, Long productId);

    /**
     * 入库汇总报表
     */
    List<Map<String, Object>> getInboundSummary(String startDate, String endDate);

    /**
     * 出库汇总报表
     */
    List<Map<String, Object>> getOutboundSummary(String startDate, String endDate);

    /**
     * 周转率分析
     */
    List<Map<String, Object>> getTurnoverAnalysis(Long warehouseId);

    /**
     * 异常预警
     */
    List<Map<String, Object>> getWarningReport();

    /**
     * 仪表盘统计
     */
    Map<String, Object> getDashboardStats();
}
