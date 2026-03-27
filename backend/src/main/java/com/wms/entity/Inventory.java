package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存实体
 *
 * @author WMS
 */
@Data
@TableName("wms_inventory")
public class Inventory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

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
     * 库房名称
     */
    private String warehouseName;

    /**
     * 货架ID
     */
    private Long shelfId;

    /**
     * 库位编码
     */
    private String locationCode;

    /**
     * 批次号
     */
    private String batchNo;

    /**
     * 库存数量
     */
    private Integer quantity;

    /**
     * 可用数量
     */
    private Integer availableQty;

    /**
     * 锁定数量
     */
    private Integer lockedQty;

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

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
