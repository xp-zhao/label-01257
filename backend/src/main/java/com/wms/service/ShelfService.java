package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Shelf;

import java.util.List;

/**
 * 货架服务接口
 *
 * @author WMS
 */
public interface ShelfService extends IService<Shelf> {

    Page<Shelf> queryPage(Page<Shelf> page, Long warehouseId, Long areaId, String shelfCode);

    List<Shelf> getShelvesByAreaId(Long areaId);

    void addShelf(Shelf shelf);

    void updateShelf(Shelf shelf);

    void deleteShelf(Long id);
}
