package com.wms.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.common.BusinessException;
import com.wms.entity.Driver;
import com.wms.entity.TransportTask;
import com.wms.entity.Vehicle;
import com.wms.mapper.DriverMapper;
import com.wms.mapper.TransportTaskMapper;
import com.wms.mapper.VehicleMapper;
import com.wms.service.TransportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransportServiceImpl extends ServiceImpl<TransportTaskMapper, TransportTask> implements TransportService {

    private final DriverMapper driverMapper;
    private final VehicleMapper vehicleMapper;

    // ========== 运输任务 ==========
    @Override
    public Page<TransportTask> queryTaskPage(Page<TransportTask> page, String taskNo, Integer status) {
        LambdaQueryWrapper<TransportTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(taskNo), TransportTask::getTaskNo, taskNo)
                .eq(status != null, TransportTask::getStatus, status)
                .orderByDesc(TransportTask::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public void createTask(TransportTask task) {
        // 验证计划到达时间必须大于计划发车时间（仅当两者都填写时）
        if (task.getPlanStartTime() != null && task.getPlanEndTime() != null) {
            if (!task.getPlanEndTime().isAfter(task.getPlanStartTime())) {
                throw new BusinessException("计划到达时间必须大于计划发车时间");
            }
        }
        String taskNo = "TS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + IdUtil.getSnowflakeNextIdStr().substring(14);
        task.setTaskNo(taskNo);
        task.setStatus(0);
        this.save(task);
        log.info("创建运输任务: {}", taskNo);
    }

    @Override
    public void updateTask(TransportTask task) {
        TransportTask existing = this.getById(task.getId());
        if (existing == null) throw new BusinessException("任务不存在");
        if (existing.getStatus() > 1) throw new BusinessException("任务已开始，不能修改");
        // 验证计划到达时间必须大于计划发车时间（仅当两者都填写时）
        if (task.getPlanStartTime() != null && task.getPlanEndTime() != null) {
            if (!task.getPlanEndTime().isAfter(task.getPlanStartTime())) {
                throw new BusinessException("计划到达时间必须大于计划发车时间");
            }
        }
        this.updateById(task);
        log.info("更新运输任务: {}", task.getTaskNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignTask(Long taskId, Long driverId, Long vehicleId) {
        TransportTask task = this.getById(taskId);
        if (task == null) throw new BusinessException("任务不存在");
        if (task.getStatus() != 0) throw new BusinessException("只能分配待分配状态的任务");

        Driver driver = driverMapper.selectById(driverId);
        if (driver == null) throw new BusinessException("司机不存在");
        if (driver.getStatus() != 1) throw new BusinessException("司机不可用");

        Vehicle vehicle = vehicleMapper.selectById(vehicleId);
        if (vehicle == null) throw new BusinessException("车辆不存在");
        if (vehicle.getStatus() != 1) throw new BusinessException("车辆不可用");

        task.setDriverId(driverId);
        task.setDriverName(driver.getDriverName());
        task.setDriverPhone(driver.getPhone());
        task.setVehicleId(vehicleId);
        task.setPlateNo(vehicle.getPlateNo());
        task.setStatus(1);
        this.updateById(task);

        driver.setStatus(2);
        driverMapper.updateById(driver);
        vehicle.setStatus(2);
        vehicleMapper.updateById(vehicle);

        log.info("分配运输任务: {} -> 司机:{}, 车辆:{}", task.getTaskNo(), driver.getDriverName(), vehicle.getPlateNo());
    }

    @Override
    public void startTask(Long id) {
        TransportTask task = this.getById(id);
        if (task == null) throw new BusinessException("任务不存在");
        if (task.getStatus() != 1) throw new BusinessException("只能开始已分配的任务");
        task.setStatus(2);
        task.setActualStartTime(LocalDateTime.now());
        this.updateById(task);
        log.info("开始运输任务: {}", task.getTaskNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(Long id) {
        TransportTask task = this.getById(id);
        if (task == null) throw new BusinessException("任务不存在");
        if (task.getStatus() != 2) throw new BusinessException("只能完成运输中的任务");
        task.setStatus(3);
        task.setActualEndTime(LocalDateTime.now());
        this.updateById(task);

        // 释放司机和车辆
        if (task.getDriverId() != null) {
            Driver driver = driverMapper.selectById(task.getDriverId());
            if (driver != null) {
                driver.setStatus(1);
                driverMapper.updateById(driver);
            }
        }
        if (task.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(task.getVehicleId());
            if (vehicle != null) {
                vehicle.setStatus(1);
                vehicleMapper.updateById(vehicle);
            }
        }
        log.info("完成运输任务: {}", task.getTaskNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelTask(Long id) {
        TransportTask task = this.getById(id);
        if (task == null) throw new BusinessException("任务不存在");
        if (task.getStatus() == 3) throw new BusinessException("已完成的任务不能取消");

        // 释放司机和车辆
        if (task.getDriverId() != null) {
            Driver driver = driverMapper.selectById(task.getDriverId());
            if (driver != null && driver.getStatus() == 2) {
                driver.setStatus(1);
                driverMapper.updateById(driver);
            }
        }
        if (task.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(task.getVehicleId());
            if (vehicle != null && vehicle.getStatus() == 2) {
                vehicle.setStatus(1);
                vehicleMapper.updateById(vehicle);
            }
        }

        task.setStatus(4);
        this.updateById(task);
        log.info("取消运输任务: {}", task.getTaskNo());
    }

    // ========== 司机管理 ==========
    @Override
    public Page<Driver> queryDriverPage(Page<Driver> page, String driverName, Integer status) {
        LambdaQueryWrapper<Driver> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(driverName), Driver::getDriverName, driverName)
                .eq(status != null, Driver::getStatus, status)
                .orderByDesc(Driver::getCreateTime);
        return driverMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Driver> getAvailableDrivers() {
        return driverMapper.selectList(new LambdaQueryWrapper<Driver>()
                .eq(Driver::getStatus, 1)
                .orderByAsc(Driver::getDriverName));
    }

    @Override
    public void addDriver(Driver driver) {
        driverMapper.insert(driver);
        log.info("新增司机: {}", driver.getDriverName());
    }

    @Override
    public void updateDriver(Driver driver) {
        driverMapper.updateById(driver);
        log.info("更新司机: {}", driver.getDriverName());
    }

    @Override
    public void deleteDriver(Long id) {
        Driver driver = driverMapper.selectById(id);
        if (driver == null) throw new BusinessException("司机不存在");
        if (driver.getStatus() == 2) throw new BusinessException("司机任务中，不能删除");
        driverMapper.deleteById(id);
        log.info("删除司机: {}", driver.getDriverName());
    }

    // ========== 车辆管理 ==========
    @Override
    public Page<Vehicle> queryVehiclePage(Page<Vehicle> page, String plateNo, Integer status) {
        LambdaQueryWrapper<Vehicle> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(plateNo), Vehicle::getPlateNo, plateNo)
                .eq(status != null, Vehicle::getStatus, status)
                .orderByDesc(Vehicle::getCreateTime);
        return vehicleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Vehicle> getAvailableVehicles() {
        return vehicleMapper.selectList(new LambdaQueryWrapper<Vehicle>()
                .eq(Vehicle::getStatus, 1)
                .orderByAsc(Vehicle::getPlateNo));
    }

    @Override
    public void addVehicle(Vehicle vehicle) {
        vehicleMapper.insert(vehicle);
        log.info("新增车辆: {}", vehicle.getPlateNo());
    }

    @Override
    public void updateVehicle(Vehicle vehicle) {
        vehicleMapper.updateById(vehicle);
        log.info("更新车辆: {}", vehicle.getPlateNo());
    }

    @Override
    public void deleteVehicle(Long id) {
        Vehicle vehicle = vehicleMapper.selectById(id);
        if (vehicle == null) throw new BusinessException("车辆不存在");
        if (vehicle.getStatus() == 2) throw new BusinessException("车辆使用中，不能删除");
        vehicleMapper.deleteById(id);
        log.info("删除车辆: {}", vehicle.getPlateNo());
    }
}
