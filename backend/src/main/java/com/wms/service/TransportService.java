package com.wms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Driver;
import com.wms.entity.TransportTask;
import com.wms.entity.Vehicle;

import java.util.List;

/**
 * 运输服务接口
 *
 * @author WMS
 */
public interface TransportService extends IService<TransportTask> {

    // 运输任务
    Page<TransportTask> queryTaskPage(Page<TransportTask> page, String taskNo, Integer status);
    void createTask(TransportTask task);
    void updateTask(TransportTask task);
    void assignTask(Long taskId, Long driverId, Long vehicleId);
    void startTask(Long id);
    void completeTask(Long id);
    void cancelTask(Long id);

    // 司机管理
    Page<Driver> queryDriverPage(Page<Driver> page, String driverName, Integer status);
    List<Driver> getAvailableDrivers();
    void addDriver(Driver driver);
    void updateDriver(Driver driver);
    void deleteDriver(Long id);

    // 车辆管理
    Page<Vehicle> queryVehiclePage(Page<Vehicle> page, String plateNo, Integer status);
    List<Vehicle> getAvailableVehicles();
    void addVehicle(Vehicle vehicle);
    void updateVehicle(Vehicle vehicle);
    void deleteVehicle(Long id);
}
