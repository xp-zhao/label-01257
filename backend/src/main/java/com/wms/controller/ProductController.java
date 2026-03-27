package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Product;
import com.wms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 商品管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String category) {
        Page<Product> page = productService.queryPage(new Page<>(pageNum, pageSize), productCode, productName, category);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/all")
    public Result<List<Product>> getAll() {
        return Result.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Product product) {
        productService.addProduct(product);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Product product) {
        product.setId(id);
        productService.updateProduct(product);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success();
    }
}
