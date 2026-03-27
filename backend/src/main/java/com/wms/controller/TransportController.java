package com.wms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.PageResult;
import com.wms.common.Result;
import com.wms.entity.Driver;
import com.wms.entity.TransportTask;
import com.wms.entity.Vehicle;
import com.wms.service.TransportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 运输调度控制器
 *
 * @author WMS
 */
@RestController
@RequestMapping("/api/transport")
@RequiredArgsConstructor
public class TransportController {

    private final TransportService transportService;

    // ========== 运输任务 ==========
    @GetMapping("/tasks")
    public Result<PageResult<TransportTask>> taskList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String taskNo,
            @RequestParam(required = false) Integer status) {
        Page<TransportTask> page = transportService.queryTaskPage(new Page<>(pageNum, pageSize), taskNo, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/tasks/{id}")
    public Result<TransportTask> getTask(@PathVariable Long id) {
        return Result.success(transportService.getById(id));
    }

    @PostMapping("/tasks")
    public Result<Void> createTask(@Valid @RequestBody TransportTask task) {
        transportService.createTask(task);
        return Result.success();
    }

    @PutMapping("/tasks/{id}")
    public Result<Void> updateTask(@PathVariable Long id, @Valid @RequestBody TransportTask task) {
        task.setId(id);
        transportService.updateTask(task);
        return Result.success();
    }

    @PostMapping("/tasks/{id}/assign")
    public Result<Void> assignTask(@PathVariable Long id, @RequestBody Map<String, Long> params) {
        transportService.assignTask(id, params.get("driverId"), params.get("vehicleId"));
        return Result.success();
    }

    @PostMapping("/tasks/{id}/start")
    public Result<Void> startTask(@PathVariable Long id) {
        transportService.startTask(id);
        return Result.success();
    }

    @PostMapping("/tasks/{id}/complete")
    public Result<Void> completeTask(@PathVariable Long id) {
        transportService.completeTask(id);
        return Result.success();
    }

    @PostMapping("/tasks/{id}/cancel")
    public Result<Void> cancelTask(@PathVariable Long id) {
        transportService.cancelTask(id);
        return Result.success();
    }

    // ========== 司机管理 ==========
    @GetMapping("/drivers")
    public Result<PageResult<Driver>> driverList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String driverName,
            @RequestParam(required = false) Integer status) {
        Page<Driver> page = transportService.queryDriverPage(new Page<>(pageNum, pageSize), driverName, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/drivers/available")
    public Result<List<Driver>> getAvailableDrivers() {
        return Result.success(transportService.getAvailableDrivers());
    }

    @PostMapping("/drivers")
    public Result<Void> addDriver(@Valid @RequestBody Driver driver) {
        transportService.addDriver(driver);
        return Result.success();
    }

    @PutMapping("/drivers/{id}")
    public Result<Void> updateDriver(@PathVariable Long id, @Valid @RequestBody Driver driver) {
        driver.setId(id);
        transportService.updateDriver(driver);
        return Result.success();
    }

    @DeleteMapping("/drivers/{id}")
    public Result<Void> deleteDriver(@PathVariable Long id) {
        transportService.deleteDriver(id);
        return Result.success();
    }

    // ========== 车辆管理 ==========
    @GetMapping("/vehicles")
    public Result<PageResult<Vehicle>> vehicleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String plateNo,
            @RequestParam(required = false) Integer status) {
        Page<Vehicle> page = transportService.queryVehiclePage(new Page<>(pageNum, pageSize), plateNo, status);
        return Result.success(PageResult.from(page));
    }

    @GetMapping("/vehicles/available")
    public Result<List<Vehicle>> getAvailableVehicles() {
        return Result.success(transportService.getAvailableVehicles());
    }

    @PostMapping("/vehicles")
    public Result<Void> addVehicle(@Valid @RequestBody Vehicle vehicle) {
        transportService.addVehicle(vehicle);
        return Result.success();
    }

    @PutMapping("/vehicles/{id}")
    public Result<Void> updateVehicle(@PathVariable Long id, @Valid @RequestBody Vehicle vehicle) {
        vehicle.setId(id);
        transportService.updateVehicle(vehicle);
        return Result.success();
    }

    @DeleteMapping("/vehicles/{id}")
    public Result<Void> deleteVehicle(@PathVariable Long id) {
        transportService.deleteVehicle(id);
        return Result.success();
    }
}
