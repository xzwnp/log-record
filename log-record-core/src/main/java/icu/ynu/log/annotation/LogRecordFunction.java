package icu.ynu.log.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * icu.ynu.log.annotation
 *
 * @author xiaozhiwei
 * 2023/3/11
 * 18:18
 * 自定义函数,标注在静态方法上.value为函数名,缺省时为方法名
 * 需要把该注解标注在类上,才能保证方法被扫描到!
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogRecordFunction {
    String value() default "";
}
