package com.example.xiao.log.function;

import com.example.xiao.log.annotation.LogRecordFunction;
import com.example.xiao.log.annotation.LogRecordFunctionBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 基于Spring的依赖注入,从容器中获取全部IParseFunction装入工厂,需要用的时候直接根据函数名获取即可
 */
@Slf4j
public class ParseFunctionFactory implements ApplicationContextAware {
    private final Map<String, Method> functionMap = new HashMap<>();
    private ApplicationContext applicationContext;

    public ParseFunctionFactory() {
    }

    public Method getFunction(String functionName) {
        return functionMap.get(functionName);
    }

    public Set<Map.Entry<String, Method>> getAllFunction() {
        return functionMap.entrySet();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        setFunctionMap();
    }

    private void setFunctionMap() {
        Map<String, Object> functionBean = applicationContext.getBeansWithAnnotation(LogRecordFunctionBean.class);
        for (Map.Entry<String, Object> entry : functionBean.entrySet()) {
            Object obj = entry.getValue();
            Method[] methods = obj.getClass().getDeclaredMethods();
            //拿到所有方法
            for (Method method : methods) {
                //静态方法+注解标注,说明是的
                if (!method.isAnnotationPresent(LogRecordFunction.class)) {
                    continue;
                }
                if (!Modifier.isStatic(method.getModifiers())) {
                    log.warn("LogRecordFunction{}#{}必须是一个静态方法!", method.getDeclaringClass().getName(), method.getName());
                    continue;
                }
                String functionName = Optional.of(method.getAnnotation(LogRecordFunction.class).value()).orElseGet(method::getName);
                functionMap.put(functionName, method);
            }
        }
    }
}