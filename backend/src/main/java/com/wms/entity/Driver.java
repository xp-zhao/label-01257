package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 司机实体
 *
 * @author WMS
 */
@Data
@TableName("wms_driver")
public class Driver implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 司机工号
     */
    private String driverCode;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 驾驶证号
     */
    private String licenseNo;

    /**
     * 准驾车型
     */
    private String licenseType;

    /**
     * 状态：1空闲/2任务中/0离职
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
