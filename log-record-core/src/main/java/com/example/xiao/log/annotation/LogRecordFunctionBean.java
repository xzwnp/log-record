package com.example.xiao.log.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * icu.ynu.log.annotation
 *
 * @author xiaozhiwei
 * 2023/3/11
 * 20:06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface LogRecordFunctionBean {
}
