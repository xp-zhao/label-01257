package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 入库单明细实体
 *
 * @author WMS
 */
@Data
@TableName("wms_inbound_order_item")
public class InboundOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 入库单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 计划数量
     */
    private Integer planQuantity;

    /**
     * 实际数量
     */
    private Integer actualQuantity;

    /**
     * 货架ID
     */
    private Long shelfId;

    /**
     * 库位编码
     */
    private String locationCode;

    /**
     * 生产日期
     */
    private LocalDate productionDate;

    /**
     * 过期日期
     */
    private LocalDate expiryDate;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
