package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 运输任务实体
 *
 * @author WMS
 */
@Data
@TableName("wms_transport_task")
public class TransportTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务单号
     */
    private String taskNo;

    /**
     * 任务类型：DELIVERY配送/TRANSFER调拨/PICKUP取货
     */
    private String taskType;

    /**
     * 起点地址
     */
    private String fromAddress;

    /**
     * 终点地址
     */
    private String toAddress;

    /**
     * 司机ID
     */
    private Long driverId;

    /**
     * 司机姓名
     */
    private String driverName;

    /**
     * 司机电话
     */
    private String driverPhone;

    /**
     * 车辆ID
     */
    private Long vehicleId;

    /**
     * 车牌号
     */
    private String plateNo;

    /**
     * 计划发车时间
     */
    private LocalDateTime planStartTime;

    /**
     * 计划到达时间
     */
    private LocalDateTime planEndTime;

    /**
     * 实际发车时间
     */
    private LocalDateTime actualStartTime;

    /**
     * 实际到达时间
     */
    private LocalDateTime actualEndTime;

    /**
     * 状态：0待分配/1已分配/2运输中/3已完成/4已取消
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
