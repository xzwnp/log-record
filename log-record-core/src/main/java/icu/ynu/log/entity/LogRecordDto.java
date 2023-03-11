package icu.ynu.log.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

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
public class LogRecordDto {
    private static ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 业务ID
     */
    @JsonProperty("业务ID")
    private String bizId;

    /**
     * 业务类型
     * 可选
     */
    @JsonProperty("业务类型")
    private String bizType;

    /**
     * 日志内容
     * 可选
     * 解析SpEL表达式后生成
     */
    @JsonProperty("业务内容")
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
     * 所有参数
     * 暂时不进行json化
     */
    @JsonIgnore
    private Map<String, Object> parameterMap;
    /**
     * 返回值
     */
    @JsonIgnore
    private Object returnValue;
    /**
     * 耗时（单位：毫秒）
     */
    @JsonProperty("耗时(毫秒)")
    private Long timeCost;

    /**
     * 是否成功
     */
    @JsonProperty("操作结果状态")
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

    @Override
    public String toString() {
        String errMessage = exception == null ? null : exception.getMessage();
        return "{\t" +
                "业务id:'" + bizId + '\'' +
                ", 业务类型:'" + bizType + '\'' +
                ", 操作描述:'" + content + '\'' +
                ", 操作员编号:'" + operatorId + '\'' +
                ", 操作员姓名:'" + operatorName + '\'' +
                ", 操作结果状态=" + (success ? "成功" : "失败") +
                ", 耗时=" + timeCost +
                "ms\t}";
    }

    public String toJsonString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
