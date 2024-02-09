package com.example.xiao.log.context;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.HashMap;
import java.util.Map;

/**
 * 维护一个可以手动设置SpEL变量的上下文
 * SpEL表达式中的部分变量可以通过在业务代码中手动调用putVariable()设置
 * AOP中通过调用putVariable来设置返回值和错误信息
 * bug: 日志线程只有第一次能输出订单号,后面输出的订单号和第一次一样???
 * 原因:InheritableThreadLocal只是在线程Create的时候复制一份父线程数据,使用线程池的话不会再创建线程了,
 * 也就导致不会重新复制一遍父线程的数据
 * 解决方案:使用阿里的TransmittableThreadLocal
 */
public class LogRecordContext {
    /**
     * Map<String, Object>是为了保存方法的所有变量
     * 子线程创建时复制父线程的ThreadLocal -> 使用InheritThreadLocal
     * 线程池环境下,子线程往往会被重复利用,此时跳过了线程的创建,自然也不会复制父线程的上下文
     * 因此,使用阿里的TransmittableThreadLocal来解决这一问题
     * TransmittableThreadLocal+TtlExecutorService,调用Thread.run之前会复制一份父线程的数据
     */
    private static final TransmittableThreadLocal<Map<String, Object>> variableMaps = new TransmittableThreadLocal<>();
    public static final String RETURN_VALUE_KEY = "_ret";
    public static final String ERROR_MESSAGE_KEY = "_msg";

    public static void putVariables(Map<String, Object> variables) {
        variableMaps.set(variables);
    }

    public static void putVariable(String key, Object value) {
        Map<String, Object> map = variableMaps.get();
        if (map == null) {
            map = new HashMap<>();
            variableMaps.set(map);
        }
        map.put(key, value);

    }

    public static Map<String, Object> getVariables() {
        return variableMaps.get();
    }

    public static void clear() {
        variableMaps.remove();
    }
}