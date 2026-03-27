package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.entity.*;
import com.wms.mapper.*;
import com.wms.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final InventoryMapper inventoryMapper;
    private final InboundOrderMapper inboundMapper;
    private final OutboundOrderMapper outboundMapper;
    private final WarehouseMapper warehouseMapper;
    private final ProductMapper productMapper;
    private final TransportTaskMapper taskMapper;
    private final OutboundOrderItemMapper outboundItemMapper;

    @Override
    public List<Map<String, Object>> getInventorySummary(Long warehouseId) {
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId));

        // 按库房分组统计
        Map<Long, List<Inventory>> grouped = inventories.stream()
                .collect(Collectors.groupingBy(Inventory::getWarehouseId));

        List<Map<String, Object>> result = new ArrayList<>();
        grouped.forEach((wId, list) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("warehouseId", wId);
            item.put("warehouseName", list.get(0).getWarehouseName());
            item.put("productCount", list.stream().map(Inventory::getProductId).distinct().count());
            item.put("totalQuantity", list.stream().mapToInt(Inventory::getQuantity).sum());
            item.put("availableQuantity", list.stream().mapToInt(Inventory::getAvailableQty).sum());
            item.put("lockedQuantity", list.stream().mapToInt(Inventory::getLockedQty).sum());
            result.add(item);
        });
        return result;
    }

    @Override
    public List<Map<String, Object>> getInventoryDetail(Long warehouseId, Long productId) {
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId)
                .eq(productId != null, Inventory::getProductId, productId)
                .orderByDesc(Inventory::getQuantity));

        return inventories.stream().map(inv -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", inv.getId());
            item.put("productCode", inv.getProductCode());
            item.put("productName", inv.getProductName());
            item.put("warehouseName", inv.getWarehouseName());
            item.put("batchNo", inv.getBatchNo());
            item.put("quantity", inv.getQuantity());
            item.put("availableQty", inv.getAvailableQty());
            item.put("lockedQty", inv.getLockedQty());
            item.put("expiryDate", inv.getExpiryDate());
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getInboundSummary(String startDate, String endDate) {
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InboundOrder::getStatus, 3); // 已完成
        // 检查日期参数是否有效（非空且非空字符串）
        boolean hasValidStartDate = startDate != null && !startDate.trim().isEmpty();
        boolean hasValidEndDate = endDate != null && !endDate.trim().isEmpty();
        if (hasValidStartDate && hasValidEndDate) {
            wrapper.between(InboundOrder::getInboundTime,
                    LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        List<InboundOrder> orders = inboundMapper.selectList(wrapper);
        // 过滤掉 inboundTime 为 null 的记录
        Map<String, List<InboundOrder>> grouped = orders.stream()
                .filter(o -> o.getInboundTime() != null)
                .collect(Collectors.groupingBy(o -> o.getInboundTime().toLocalDate().toString()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey());
                    item.put("orderCount", entry.getValue().size());
                    item.put("totalQuantity", entry.getValue().stream().mapToInt(o -> o.getActualQuantity() != null ? o.getActualQuantity() : 0).sum());
                    return item;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getOutboundSummary(String startDate, String endDate) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OutboundOrder::getStatus, 4); // 已出库
        // 检查日期参数是否有效（非空且非空字符串）
        boolean hasValidStartDate = startDate != null && !startDate.trim().isEmpty();
        boolean hasValidEndDate = endDate != null && !endDate.trim().isEmpty();
        if (hasValidStartDate && hasValidEndDate) {
            wrapper.between(OutboundOrder::getOutboundTime,
                    LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }

        List<OutboundOrder> orders = outboundMapper.selectList(wrapper);
        // 过滤掉 outboundTime 为 null 的记录
        Map<String, List<OutboundOrder>> grouped = orders.stream()
                .filter(o -> o.getOutboundTime() != null)
                .collect(Collectors.groupingBy(o -> o.getOutboundTime().toLocalDate().toString()));

        return grouped.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey());
                    item.put("orderCount", entry.getValue().size());
                    item.put("totalQuantity", entry.getValue().stream().mapToInt(o -> o.getActualQuantity() != null ? o.getActualQuantity() : 0).sum());
                    return item;
                }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getTurnoverAnalysis(Long warehouseId) {
        List<Inventory> inventories = inventoryMapper.selectList(new LambdaQueryWrapper<Inventory>()
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId));

        // 获取最近30天的出库明细数据
        LocalDateTime thirtyDaysAgo = LocalDate.now().minusDays(30).atStartOfDay();
        List<OutboundOrder> completedOrders = outboundMapper.selectList(new LambdaQueryWrapper<OutboundOrder>()
                .eq(OutboundOrder::getStatus, 4)
                .ge(OutboundOrder::getOutboundTime, thirtyDaysAgo));
        
        // 获取这些订单的出库明细
        List<Long> orderIds = completedOrders.stream().map(OutboundOrder::getId).collect(Collectors.toList());
        Map<Long, Integer> productOutboundQty = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OutboundOrderItem> items = outboundItemMapper.selectList(new LambdaQueryWrapper<OutboundOrderItem>()
                    .in(OutboundOrderItem::getOrderId, orderIds));
            // 按商品ID汇总出库数量
            productOutboundQty = items.stream()
                    .collect(Collectors.groupingBy(
                            OutboundOrderItem::getProductId,
                            Collectors.summingInt(i -> i.getActualQuantity() != null ? i.getActualQuantity() : 0)
                    ));
        }

        // 计算周转率：周转率 = 出库量 / 当前库存
        Map<Long, List<Inventory>> grouped = inventories.stream()
                .collect(Collectors.groupingBy(Inventory::getProductId));

        final Map<Long, Integer> finalOutboundQty = productOutboundQty;
        return grouped.entrySet().stream().map(entry -> {
            Map<String, Object> item = new HashMap<>();
            List<Inventory> list = entry.getValue();
            Long productId = entry.getKey();
            item.put("productId", productId);
            item.put("productCode", list.get(0).getProductCode());
            item.put("productName", list.get(0).getProductName());
            int totalQty = list.stream().mapToInt(Inventory::getQuantity).sum();
            item.put("currentStock", totalQty);
            
            // 计算真实周转率
            int outboundQty = finalOutboundQty.getOrDefault(productId, 0);
            item.put("outboundQty", outboundQty);
            double turnoverRate = totalQty > 0 ? Math.round(outboundQty * 100.0 / totalQty) / 100.0 : 0;
            item.put("turnoverRate", turnoverRate);
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getWarningReport() {
        List<Inventory> warnings = inventoryMapper.selectWarningList();
        return warnings.stream().map(inv -> {
            Map<String, Object> item = new HashMap<>();
            item.put("productCode", inv.getProductCode());
            item.put("productName", inv.getProductName());
            item.put("warehouseName", inv.getWarehouseName());
            item.put("quantity", inv.getQuantity());
            item.put("expiryDate", inv.getExpiryDate());
            // 判断预警类型：区分已过期、即将过期和库存不足
            if (inv.getExpiryDate() != null && inv.getExpiryDate().isBefore(LocalDate.now())) {
                item.put("warningType", "已过期");
            } else if (inv.getExpiryDate() != null && inv.getExpiryDate().isBefore(LocalDate.now().plusDays(30))) {
                item.put("warningType", "即将过期");
            } else {
                item.put("warningType", "库存不足");
            }
            return item;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // 库房数量
        stats.put("warehouseCount", warehouseMapper.selectCount(new LambdaQueryWrapper<Warehouse>()
                .eq(Warehouse::getStatus, 1)));

        // 商品数量
        stats.put("productCount", productMapper.selectCount(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)));

        // 库存总量
        List<Inventory> inventories = inventoryMapper.selectList(null);
        stats.put("totalInventory", inventories.stream().mapToInt(Inventory::getQuantity).sum());

        // 今日入库
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = todayStart.plusDays(1);
        List<InboundOrder> todayInbound = inboundMapper.selectList(new LambdaQueryWrapper<InboundOrder>()
                .eq(InboundOrder::getStatus, 3)
                .between(InboundOrder::getInboundTime, todayStart, todayEnd));
        stats.put("todayInbound", todayInbound.stream().mapToInt(InboundOrder::getActualQuantity).sum());

        // 今日出库
        List<OutboundOrder> todayOutbound = outboundMapper.selectList(new LambdaQueryWrapper<OutboundOrder>()
                .eq(OutboundOrder::getStatus, 4)
                .between(OutboundOrder::getOutboundTime, todayStart, todayEnd));
        stats.put("todayOutbound", todayOutbound.stream().mapToInt(OutboundOrder::getActualQuantity).sum());

        // 待处理入库单
        stats.put("pendingInbound", inboundMapper.selectCount(new LambdaQueryWrapper<InboundOrder>()
                .eq(InboundOrder::getStatus, 1)));

        // 待审核出库单
        stats.put("pendingOutbound", outboundMapper.selectCount(new LambdaQueryWrapper<OutboundOrder>()
                .eq(OutboundOrder::getStatus, 1)));

        // 进行中运输任务
        stats.put("activeTransport", taskMapper.selectCount(new LambdaQueryWrapper<TransportTask>()
                .eq(TransportTask::getStatus, 2)));

        // 预警数量
        stats.put("warningCount", inventoryMapper.selectWarningList().size());

        return stats;
    }
}
