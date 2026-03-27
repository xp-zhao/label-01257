package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 货架实体
 *
 * @author WMS
 */
@Data
@TableName("wms_shelf")
public class Shelf implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 库房ID
     */
    private Long warehouseId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 货架编码
     */
    private String shelfCode;

    /**
     * 货架名称
     */
    private String shelfName;

    /**
     * 层数
     */
    private Integer layerCount;

    /**
     * 列数
     */
    private Integer columnCount;

    /**
     * 最大承重(kg)
     */
    private BigDecimal maxWeight;

    /**
     * 状态：1启用 0停用
     */
    private Integer status;

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

    /**
     * 删除标记
     */
    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String warehouseName;

    @TableField(exist = false)
    private String areaName;
}
