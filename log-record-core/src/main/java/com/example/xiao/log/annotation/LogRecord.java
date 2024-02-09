package com.example.xiao.log.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRecord {

	/**
	 * 操作名称
	 * 可选,默认为方法名称
	 */
	String operate() default "";


	/**
	 * 操作描述
	 * 可选
	 * SpEL表达式
	 */
	String content() default "";


	/**
	 * 操作人ID
	 * 可选
	 * 如果不填,会尝试采用系统默认的实现方式来获取操作人ID
	 */
	String operatorId() default "";

	/**
	 * 操作人名字
	 * 可选
	 */
	String operatorName() default "";

	/**
	 * 是否记录返回值
	 * true: 记录返回值
	 * false: 不记录返回值
	 */
	boolean recordReturnValue() default true;

}