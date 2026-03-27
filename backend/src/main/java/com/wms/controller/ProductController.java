package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Product;
import com.wms.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    @GetMapping("/export")
    public void export(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String category,
            HttpServletResponse response) throws IOException {
        Page<Product> page = productService.queryPage(new Page<>(pageNum, pageSize), productCode, productName, category);
        List<Product> productList = page.getRecords();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品信息");

        String[] headers = {"商品编码", "商品名称", "分类", "品牌", "单位", "规格", "单价", "最低库存", "最高库存", "状态"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        for (int i = 0; i < productList.size(); i++) {
            Product product = productList.get(i);
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(product.getProductCode());
            row.createCell(1).setCellValue(product.getProductName());
            row.createCell(2).setCellValue(product.getCategory());
            row.createCell(3).setCellValue(product.getBrand());
            row.createCell(4).setCellValue(product.getUnit());
            row.createCell(5).setCellValue(product.getSpecification());
            row.createCell(6).setCellValue(product.getPrice() != null ? product.getPrice().doubleValue() : 0);
            row.createCell(7).setCellValue(product.getMinStock() != null ? product.getMinStock() : 0);
            row.createCell(8).setCellValue(product.getMaxStock() != null ? product.getMaxStock() : 0);
            row.createCell(9).setCellValue(product.getStatus() == 1 ? "启用" : "停用");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String fileName = "商品信息_" + sdf.format(new Date()) + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20"));

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
