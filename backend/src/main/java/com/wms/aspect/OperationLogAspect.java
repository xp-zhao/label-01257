package com.wms.aspect;

import cn.hutool.json.JSONUtil;
import com.wms.entity.SysOperationLog;
import com.wms.mapper.SysOperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 操作日志切面
 *
 * @author WMS
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final SysOperationLogMapper logMapper;

    /**
     * 切点：Controller层的所有增删改方法
     */
    @Pointcut("execution(* com.wms.controller.*.*(..)) && " +
            "(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping))")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        SysOperationLog operationLog = new SysOperationLog();
        
        try {
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                operationLog.setIp(getClientIp(request));
            }

            // 获取当前用户
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                operationLog.setUsername(userDetails.getUsername());
            }

            // 获取方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String methodName = method.getName();
            operationLog.setMethod(className + "." + methodName);
            operationLog.setOperation(getOperationDesc(className, methodName));

            // 获取参数
            Object[] args = joinPoint.getArgs();
            try {
                String params = JSONUtil.toJsonStr(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000);
                }
                operationLog.setParams(params);
            } catch (Exception e) {
                operationLog.setParams("参数解析失败");
            }

            // 执行目标方法
            Object result = joinPoint.proceed();

            // 记录成功
            operationLog.setStatus(1);
            operationLog.setDuration((int) (System.currentTimeMillis() - startTime));

            // 异步保存日志
            saveLog(operationLog);

            return result;
        } catch (Throwable e) {
            // 记录失败
            operationLog.setStatus(0);
            operationLog.setErrorMsg(e.getMessage());
            operationLog.setDuration((int) (System.currentTimeMillis() - startTime));
            saveLog(operationLog);
            throw e;
        }
    }

    /**
     * 保存日志
     */
    private void saveLog(SysOperationLog operationLog) {
        try {
            logMapper.insert(operationLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    /**
     * 获取操作描述
     */
    private String getOperationDesc(String className, String methodName) {
        String module = className.replace("Controller", "");
        String action;
        if (methodName.startsWith("add") || methodName.startsWith("create")) {
            action = "新增";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            action = "更新";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            action = "删除";
        } else if (methodName.startsWith("submit")) {
            action = "提交";
        } else if (methodName.startsWith("confirm")) {
            action = "确认";
        } else if (methodName.startsWith("cancel")) {
            action = "取消";
        } else if (methodName.startsWith("audit")) {
            action = "审核";
        } else if (methodName.startsWith("assign")) {
            action = "分配";
        } else if (methodName.startsWith("start")) {
            action = "开始";
        } else if (methodName.startsWith("complete")) {
            action = "完成";
        } else if (methodName.startsWith("reset")) {
            action = "重置";
        } else if (methodName.startsWith("adjust")) {
            action = "调整";
        } else {
            action = methodName;
        }
        return module + "-" + action;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
