package com.wms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体
 *
 * @author WMS
 */
@Data
@TableName("wms_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类
     */
    private String category;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 单位
     */
    private String unit;

    /**
     * 规格
     */
    private String specification;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 最低库存
     */
    private Integer minStock;

    /**
     * 最高库存
     */
    private Integer maxStock;

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
}
