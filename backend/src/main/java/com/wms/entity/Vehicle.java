package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆实体
 *
 * @author WMS
 */
@Data
@TableName("wms_vehicle")
public class Vehicle implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 车辆类型
     */
    private String vehicleType;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 型号
     */
    private String model;

    /**
     * 载重(kg)
     */
    private BigDecimal loadCapacity;

    /**
     * 容积(m³)
     */
    private BigDecimal volume;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 状态：1空闲/2使用中/3维修中/0报废
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
}
