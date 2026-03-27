package com.wms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * WMS 仓库管理系统启动类
 *
 * @author WMS
 */
@SpringBootApplication
@MapperScan("com.wms.mapper")
@EnableTransactionManagement
public class WmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(WmsApplication.class, args);
        System.out.println("=====================================");
        System.out.println("  WMS 物流仓库管理系统启动成功！");
        System.out.println("  后端地址: http://localhost:8080");
        System.out.println("=====================================");
    }
}
