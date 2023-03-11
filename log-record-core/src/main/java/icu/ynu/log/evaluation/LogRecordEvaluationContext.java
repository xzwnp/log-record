package icu.ynu.log.evaluation;

import icu.ynu.log.context.LogRecordContext;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 根据业务需求,自定义日志评估器上下文
 */
public class LogRecordEvaluationContext extends MethodBasedEvaluationContext {

    public LogRecordEvaluationContext(Object rootObject, Method method, Object[] arguments,
									  ParameterNameDiscoverer parameterNameDiscoverer) {
       //调用MethodBasedEvaluationContext的构造方法,把方法的参数都放到SpEL解析的上下文中
       super(rootObject, method, arguments, parameterNameDiscoverer);
       //把LogRecordContext中的变量都放到SpEL解析的上下文中
        Map<String, Object> variables = LogRecordContext.getVariables();
        if (variables != null && variables.size() > 0) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                setVariable(entry.getKey(), entry.getValue());
            }
        }
    }

}