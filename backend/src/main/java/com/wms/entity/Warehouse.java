package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库房实体
 *
 * @author WMS
 */
@Data
@TableName("wms_warehouse")
public class Warehouse implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 库房编码
     */
    private String warehouseCode;

    /**
     * 库房名称
     */
    private String warehouseName;

    /**
     * 地址
     */
    private String address;

    /**
     * 总面积
     */
    private BigDecimal totalArea;

    /**
     * 已用面积
     */
    private BigDecimal usedArea;

    /**
     * 负责人
     */
    private String managerName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 状态：1启用 0停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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
