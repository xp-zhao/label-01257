package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.Product;
import com.wms.mapper.ProductMapper;
import com.wms.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Override
    public Page<Product> queryPage(Page<Product> page, String productCode, String productName, String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(productCode), Product::getProductCode, productCode)
                .like(StringUtils.hasText(productName), Product::getProductName, productName)
                .eq(StringUtils.hasText(category), Product::getCategory, category)
                .orderByDesc(Product::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public List<Product> getAllProducts() {
        return this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByAsc(Product::getProductCode));
    }

    @Override
    public void addProduct(Product product) {
        long count = this.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, product.getProductCode()));
        if (count > 0) {
            throw new BusinessException("商品编码已存在");
        }
        this.save(product);
        log.info("新增商品成功: {}", product.getProductName());
    }

    @Override
    public void updateProduct(Product product) {
        long count = this.count(new LambdaQueryWrapper<Product>()
                .eq(Product::getProductCode, product.getProductCode())
                .ne(Product::getId, product.getId()));
        if (count > 0) {
            throw new BusinessException("商品编码已存在");
        }
        this.updateById(product);
        log.info("更新商品成功: {}", product.getProductName());
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        this.removeById(id);
        log.info("删除商品成功: {}", product.getProductName());
    }
}
