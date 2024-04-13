package com.example.xiao.log.entity;

import com.example.xiao.log.util.DateUtils;
import com.example.xiao.log.util.JsonUtils;
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
 * com.example.xiao.log.bean
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
    private static ObjectMapper objectMapper = JsonUtils.getObjectMapper();

    /**
     * 操作名称
     * 可选,默认为方法名称
     */
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
    String content;


    /**
     * 操作人ID
     * 如果为空,会采取默认的方式尝试获取操作人
     */
    private String operatorId;

    /**
     * 操作人姓名/昵称
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

    /**
     * 所有参数
     */
    private Map<String, Object> parameterMap;
    /**
     * 返回值
     */
    private Object returnValue;
    /**
     * 耗时（单位：毫秒）
     */
    private Long timeCost;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 异常信息
     */
    @JsonSerialize(using = ErrorMessageSerializer.class)
    private Exception exception;

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

    @Override
    public String toString() {
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
