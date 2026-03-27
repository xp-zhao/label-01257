package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 补货策略实体
 *
 * @author WMS
 */
@Data
@TableName("wms_replenish_strategy")
public class ReplenishStrategy implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String strategyCode;
    private String strategyName;
    private Long warehouseId;
    private Long productId;
    private Integer minQuantity;
    private Integer maxQuantity;
    private Integer replenishQuantity;
    private String triggerType;
    private Integer status;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String warehouseName;

    @TableField(exist = false)
    private String productName;
}
