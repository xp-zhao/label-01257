package com.wms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wms.entity.Vehicle;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆Mapper
 *
 * @author WMS
 */
@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}
