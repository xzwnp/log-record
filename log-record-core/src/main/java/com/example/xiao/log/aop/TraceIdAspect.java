package com.example.xiao.log.aop;

import com.example.xiao.log.constant.LogRecordConstants;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
@Order(Integer.MAX_VALUE - 1)
public class TraceIdAspect {
    /**
     * 定义切入点
     */
    @Pointcut("@within(com.example.xiao.log.annotation.LogRecordTrace)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before() {
        String traceId = String.valueOf(System.nanoTime());
        MDC.put(LogRecordConstants.TRACE_ID_KEY, traceId);
    }

    @AfterReturning("pointCut()")
    public void afterReturning() {
        MDC.remove(LogRecordConstants.TRACE_ID_KEY);
    }

}
