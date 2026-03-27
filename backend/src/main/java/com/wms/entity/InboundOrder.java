package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单实体
 *
 * @author WMS
 */
@Data
@TableName("wms_inbound_order")
public class InboundOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 入库单号
     */
    private String orderNo;

    /**
     * 入库仓库ID
     */
    private Long warehouseId;

    /**
     * 入库仓库名称
     */
    private String warehouseName;

    /**
     * 入库类型：PURCHASE采购/RETURN退货/TRANSFER调拨/OTHER其他
     */
    private String inboundType;

    /**
     * 供应商
     */
    private String supplier;

    /**
     * 总数量
     */
    private Integer totalQuantity;

    /**
     * 实际入库数量
     */
    private Integer actualQuantity;

    /**
     * 状态：0草稿/1待入库/2入库中/3已完成/4已取消
     */
    private Integer status;

    /**
     * 入库时间
     */
    private LocalDateTime inboundTime;

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
    private List<InboundOrderItem> items;
}
