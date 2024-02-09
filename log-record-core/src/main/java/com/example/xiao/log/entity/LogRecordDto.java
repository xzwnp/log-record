package com.example.xiao.log.entity;

import com.example.xiao.log.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * icu.ynu.log.bean
 * 封装操作信息,便于持久化后以报表形式查看
 *
 * @author xzwnp
 * 2023/3/1
 * 21:00
 */
@Data
@Accessors(chain = true)
@Slf4j
public class LogRecordDto {
	private static ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 操作名称
	 * 可选,默认为方法名称
	 */
	@JsonProperty("操作")
	private String operate;

	/**
	 * traceId
	 */
	private String traceId;

	/**
	 * 日志内容
	 * 可选
	 * 解析SpEL表达式后生成
	 */
	@JsonProperty("描述")
	String content;


	/**
	 * 操作人ID
	 * 如果为空,会采取默认的方式尝试获取操作人
	 */
	@JsonProperty("操作人ID")
	private String operatorId;

	/**
	 * 操作人姓名/昵称
	 */
	@JsonProperty("操作人姓名")
	private String operatorName;

	/**
	 * 操作时间
	 */
	@JsonProperty("操作时间")
	private LocalDateTime operateTime;

	/**
	 * 所有参数
	 */
	@JsonProperty("参数")
	private Map<String, Object> parameterMap;
	/**
	 * 返回值
	 */
	@JsonProperty("返回值")
	private Object returnValue;
	/**
	 * 耗时（单位：毫秒）
	 */
	@JsonProperty("耗时(毫秒)")
	private Long timeCost;

	/**
	 * 是否成功
	 */
	@JsonProperty("是否成功")
	@JsonSerialize(using = OperatorSuccessSerializer.class)
	private boolean success;

	/**
	 * 异常信息
	 */
	@JsonProperty("错误信息")
	@JsonSerialize(using = ErrorMessageSerializer.class)
	private Exception exception;

	/**
	 * 自定义操作成功状态的序列化方式
	 */
	static class OperatorSuccessSerializer extends JsonSerializer<Boolean> {
		@Override
		public void serialize(Boolean b, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			jsonGenerator.writeString(b ? "操作成功" : "操作失败");
		}

	}

	static class ErrorMessageSerializer extends JsonSerializer<Exception> {
		@Override
		public void serialize(Exception e, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
			if (e != null && StringUtils.hasLength(e.getMessage())) {
				jsonGenerator.writeString(e.getMessage());
			} else {
				jsonGenerator.writeString("无");
			}
		}

	}


	public String toJsonString() {
		String returnValueString = null;
		String paramsString = null;
		try {
			returnValueString = objectMapper.writeValueAsString(returnValue);
			paramsString = objectMapper.writeValueAsString(paramsString);
		} catch (JsonProcessingException e) {
			log.warn("toJsonString error", e);
			returnValueString = "";
		}
		String success = isSuccess() ? "是" : "否";
		String errMsg = exception != null ? exception.getMessage() : "";
		String message = String.format("\"traceId\":\"%s\"," +
				"\"操作\":\"%s\",\"描述\":\"%s\"," +
				"\"操作人ID\":\"%s\"," +
				"\"操作人姓名\":\"%s\"," +
				"\"操作时间\":\"%s\"," +
				"\"参数\":%s," +
				"\"返回值\":%s," +
				"\"耗时(毫秒)\":%d," +
				"\"是否成功\":\"%s\"," +
				"\"错误信息\":\"%s\"",
			traceId, operate, content,
			operatorId, operatorName, DateUtils.format(operateTime),
			paramsString, returnValueString, timeCost,
			success, errMsg);
		return message;
	}


}
