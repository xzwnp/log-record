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
	 * 业务类型
	 * 可选(建议填)
	 */
	String bizType();

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

	/**
	 * 是否在业务代码执行之前执行日志操作逻辑
	 * 实际上,只会在业务代码执行之前去解析SpEL表达式
	 * 举例:进行更新操作,需要记录旧值,这时候就需要在业务代码执行之前去执行日志操作,从数据库查询旧值
	 */
	boolean executeBefore() default false;

}