package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Shelf;
import com.wms.service.ShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 货架管理控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/shelves")
@RequiredArgsConstructor
public class ShelfController {

    private final ShelfService shelfService;

    @GetMapping
    public Result<PageResult<Shelf>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long areaId,
            @RequestParam(required = false) String shelfCode) {
        Page<Shelf> page = shelfService.queryPage(new Page<>(pageNum, pageSize), warehouseId, areaId, shelfCode);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/area/{areaId}")
    public Result<List<Shelf>> getByArea(@PathVariable Long areaId) {
        return Result.success(shelfService.getShelvesByAreaId(areaId));
    }

    @GetMapping("/{id}")
    public Result<Shelf> getById(@PathVariable Long id) {
        return Result.success(shelfService.getById(id));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody Shelf shelf) {
        shelfService.addShelf(shelf);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Shelf shelf) {
        shelf.setId(id);
        shelfService.updateShelf(shelf);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        shelfService.deleteShelf(id);
        return Result.success();
    }
}
