package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author WMS
 */
public interface ProductService extends IService<Product> {

    Page<Product> queryPage(Page<Product> page, String productCode, String productName, String category);

    List<Product> getAllProducts();

    void addProduct(Product product);

    void updateProduct(Product product);

    void deleteProduct(Long id);
}
