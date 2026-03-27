package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.InboundOrder;

/**
 * 入库服务接口
 *
 * @author WMS
 */
public interface InboundService extends IService<InboundOrder> {

    Page<InboundOrder> queryPage(Page<InboundOrder> page, String orderNo, Long warehouseId, Integer status);

    InboundOrder getDetail(Long id);

    void createOrder(InboundOrder order);

    void updateOrder(InboundOrder order);

    void submitOrder(Long id);

    void confirmInbound(Long id);

    void cancelOrder(Long id);
}
