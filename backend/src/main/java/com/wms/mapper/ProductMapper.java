package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper
 *
 * @author WMS
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
