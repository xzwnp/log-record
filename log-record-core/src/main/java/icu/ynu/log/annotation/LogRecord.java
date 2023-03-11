package icu.ynu.log.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogRecord {

	/**
	 * 业务ID
	 * 必填
	 * SpEL表达式
	 */
	String bizId();

	/**
	 * 日志内容
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
	boolean recordReturnValue() default false;

}