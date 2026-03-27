package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单实体
 *
 * @author WMS
 */
@Data
@TableName("wms_outbound_order")
public class OutboundOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 出库单号
     */
    private String orderNo;

    /**
     * 出库仓库ID
     */
    private Long warehouseId;

    /**
     * 出库仓库名称
     */
    private String warehouseName;

    /**
     * 出库类型：SALES销售/RETURN退货/TRANSFER调拨/OTHER其他
     */
    private String outboundType;

    /**
     * 客户
     */
    private String customer;

    /**
     * 送货地址
     */
    private String deliveryAddress;

    /**
     * 总数量
     */
    private Integer totalQuantity;

    /**
     * 实际出库数量
     */
    private Integer actualQuantity;

    /**
     * 状态：0草稿/1待审核/2已审核/3拣货中/4已出库/5已取消
     */
    private Integer status;

    /**
     * 审核人
     */
    private String auditorName;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 出库时间
     */
    private LocalDateTime outboundTime;

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

    /**
     * 明细列表
     */
    @TableField(exist = false)
    private List<OutboundOrderItem> items;
}
