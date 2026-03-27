package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存调整记录实体
 *
 * @author WMS
 */
@Data
@TableName("wms_inventory_adjust")
public class InventoryAdjust implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调整单号
     */
    private String adjustNo;

    /**
     * 库存ID
     */
    private Long inventoryId;

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
     * 库房ID
     */
    private Long warehouseId;

    /**
     * 调整类型：GAIN盘盈/LOSS盘亏/DAMAGE报损/OTHER其他
     */
    private String adjustType;

    /**
     * 调整前数量
     */
    private Integer beforeQuantity;

    /**
     * 调整数量（正数增加，负数减少）
     */
    private Integer adjustQuantity;

    /**
     * 调整后数量
     */
    private Integer afterQuantity;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 操作人
     */
    private String operatorName;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
