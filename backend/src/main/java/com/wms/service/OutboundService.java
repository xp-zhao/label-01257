package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.OutboundOrder;

/**
 * 出库服务接口
 *
 * @author WMS
 */
public interface OutboundService extends IService<OutboundOrder> {

    Page<OutboundOrder> queryPage(Page<OutboundOrder> page, String orderNo, Long warehouseId, Integer status);

    OutboundOrder getDetail(Long id);

    void createOrder(OutboundOrder order);

    void updateOrder(OutboundOrder order);

    void submitOrder(Long id);

    void auditOrder(Long id, boolean approved, String auditorName);

    void confirmOutbound(Long id);

    void cancelOrder(Long id);
}
