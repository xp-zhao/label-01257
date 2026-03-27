package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库房区域实体
 *
 * @author WMS
 */
@Data
@TableName("wms_warehouse_area")
public class WarehouseArea implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 库房ID
     */
    private Long warehouseId;

    /**
     * 区域编码
     */
    private String areaCode;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 区域类型：STORAGE/PICKING/RECEIVING/SHIPPING/TEMPORARY
     */
    private String areaType;

    /**
     * 面积
     */
    private BigDecimal areaSize;

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

    /**
     * 库房名称
     */
    @TableField(exist = false)
    private String warehouseName;
}
