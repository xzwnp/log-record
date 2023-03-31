package icu.ynu.log.aop;

import com.alibaba.ttl.threadpool.TtlExecutors;
import icu.ynu.log.annotation.LogRecord;
import icu.ynu.log.common.LogRecordException;
import icu.ynu.log.entity.LogRecordDto;
import icu.ynu.log.entity.Operator;
import icu.ynu.log.context.LogRecordContext;
import icu.ynu.log.evaluation.LogRecordEvaluationContext;
import icu.ynu.log.function.ParseFunctionFactory;
import icu.ynu.log.operator.IOperatorGetService;
import icu.ynu.log.persistence.LogPersistenceStrategy;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;

/**
 * com.example.autoconfig.config.log
 *
 * @author xzwnp
 * 2023/1/10
 * 11:34
 */
@Component
@Aspect
@EnableAspectJAutoProxy
public class LogRecordAspect {
    private static final Logger log = LoggerFactory.getLogger("LogInnerError");
    private final ExecutorService logExecutor;


    private final ObjectMapper objectMapper;

    private IOperatorGetService operatorGetService;

    private ExpressionParser expressionParser;

    private ParameterNameDiscoverer parameterNameDiscoverer;

    @Autowired(required = false)
    private List<LogPersistenceStrategy> logPersistenceStrategies;

    private ParseFunctionFactory functionFactory;

    public LogRecordAspect(IOperatorGetService operatorGetService, ExpressionParser expressionParser, ParameterNameDiscoverer parameterNameDiscoverer,
                           @Autowired @Qualifier("logExecutor") ThreadPoolExecutor executor, ParseFunctionFactory functionFactory) {
        objectMapper = new ObjectMapper();
        //跳过序列化空值属性
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);

        //包装原有线程池
        logExecutor = TtlExecutors.getTtlExecutorService(executor);
        this.operatorGetService = operatorGetService;
        this.expressionParser = expressionParser;
        this.parameterNameDiscoverer = parameterNameDiscoverer;
        this.functionFactory = functionFactory;
    }

    /**
     * 定义切入点
     */
    @Pointcut("@annotation(icu.ynu.log.annotation.LogRecord)")
    public void pointCut() {
    }


    /**
     * 前置通知：目标方法执行之前执行以下方法体的内容。
     * value：绑定通知的切入点表达式。可以关联切入点声明，也可以直接设置切入点表达式
     * <br/>
     * * @param joinPoint：提供对连接点处可用状态和有关它的静态信息的反射访问<br/> <p>
     * * * Object[] getArgs()：返回此连接点处（目标方法）的参数，目标方法无参数时，返回空数组
     * * * Signature getSignature()：返回连接点处的签名。
     * * * Object getTarget()：返回目标对象
     * * * Object getThis()：返回当前正在执行的对象
     * * * StaticPart getStaticPart()：返回一个封装此连接点的静态部分的对象。
     * * * SourceLocation getSourceLocation()：返回与连接点对应的源位置
     * * * String toLongString()：返回连接点的扩展字符串表示形式。
     * * * String toShortString()：返回连接点的缩写字符串表示形式。
     * * * String getKind()：返回表示连接点类型的字符串
     * * * </p>
     */
//	@Before("pointCut()&&@annotation(logRecord)")
//	public void before(LogRecord logRecord) {
//
//	}
    @Around("pointCut() && @annotation(logRecord)")
    public Object afterReturning(ProceedingJoinPoint joinPoint, LogRecord logRecord) throws Throwable {
        Object result = null; //业务逻辑返回结果
        Exception exception = null;
        boolean success; //业务逻辑是否执行成功
        try {
            //目标方法执行前
            StopWatch stopWatch = new StopWatch();
            try {
                stopWatch.start();
                //目标方法执行
                result = joinPoint.proceed();
                stopWatch.stop();
                success = true;
            } catch (Exception e) {
                //如果目标方法执行出错
                success = false;
                exception = e;
            }

            //封装成dto,方便进行持久化
            LogRecordDto logRecordDto = new LogRecordDto();
            logRecordDto.setSuccess(success);

            final Object finalResult = result;
            final Exception finalEx = exception;
            //多线程打日志,降低性能影响
            logExecutor.execute(() -> executeLogServiceAsync(joinPoint, logRecord, stopWatch, logRecordDto, finalResult, finalEx));

        } catch (LogRecordException e) {
            log.error(e.getMessage());
            //日志出错也要返回正常执行的业务
            return result;
        } catch (Exception e) {
            log.error("日志系统出现内部错误:", e);
            //日志出错也要返回正常执行的业务
            return result;
        }
        //如果业务执行有异常,抛出去,交给ControllerAdvice处理
        if (exception != null) {
            throw exception;
        }
        return result;

    }

    /**
     * 异步地执行日志输出.包括解析SpEL,输出到控制台(默认输出),执行持久化
     */
    private void executeLogServiceAsync(ProceedingJoinPoint joinPoint, LogRecord logRecord, StopWatch stopWatch, LogRecordDto logRecordDto, Object finalResult, Exception finalEx) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            EvaluationContext evaluationContext = prepareLogRecordEvaluationContext(signature.getMethod(), joinPoint.getArgs());
            //初始化logRecordDto的必要属性
            Operator operator = getOperator(logRecord);
            long cost = stopWatch.getTotalTimeMillis(); //业务执行耗时
            //操作内容用SpEL解析器解析后输出
            String content = parseExpression(logRecord.content(), evaluationContext);
            String bizId = parseExpression(logRecord.bizId(), evaluationContext);
            String bizType = logRecord.bizType();
            //输出操作内容到控制台
            log.info("bizId:{},类型:{},描述:{},耗时:{}ms", bizId, bizType, content, cost);

            logRecordDto
                    .setBizId(bizId)
                    .setBizType(bizType)
                    .setOperatorId(operator.getId()).
                    setOperatorName(operator.getName())
                    .setContent(content)
                    .setTimeCost(cost);

            //是否输出返回值
            if (logRecord.recordReturnValue()) {
                logRecordDto.setReturnValue(finalResult)
                        .setException(finalEx);
                LogRecordContext.putVariable(LogRecordContext.RETURN_VALUE_KEY, finalResult);

                //返回值以json格式输出,只输出前50个字符
                //todo 返回值采用SpEl表达式格式,只输出想看到的部分
                String returnValueString = null;
                returnValueString = objectMapper.writeValueAsString(finalResult);
                //返回结果过长时会进行截断
                if (returnValueString.length() > 200) {
                    returnValueString = returnValueString.substring(0, 200) + "......";
                }
                log.info("返回结果:{}", returnValueString);
            }
            //日志持久化
            saveLog(logRecordDto);
        } catch (LogRecordException e) {
            log.error(e.getMessage());
        } catch (JsonProcessingException e) {
            log.error("Json解析异常,无法输出到控制台", e);
        } catch (Exception e) {
            log.error("日志系统出现内部错误.原因:" + e.getMessage(), e);
        } finally {
            //threadLocal手动清除,避免内存泄露
            LogRecordContext.clear();
        }
    }

    private String parseExpression(String expressionString, EvaluationContext evaluationContext) {
        try {
            return expressionParser.
                    parseExpression(expressionString).getValue(evaluationContext, String.class);
        } catch (SpelParseException | SpelEvaluationException e) {
            String message = new StringBuilder().append("SpEL表达式{").append(expressionString).append("}无法解析,")
                    .append("原因:").append(e.getMessage()).toString();
            throw new LogRecordException(message, e);
        }
    }

    /**
     * 获取操作员信息
     *
     * @param logRecord
     * @return
     */
    private Operator getOperator(LogRecord logRecord) {
        if (!StringUtils.hasLength(logRecord.operatorId())) {
            return operatorGetService.getOperator();
        } else {
            return new Operator(logRecord.operatorId(), logRecord.operatorName());
        }
    }

    /**
     * 配置日志解析上下文
     */
    private EvaluationContext prepareLogRecordEvaluationContext(Method method, Object[] arguments) {
        LogRecordEvaluationContext context = new LogRecordEvaluationContext(null, method, arguments, this.parameterNameDiscoverer);
        //注册自定义函数
        functionFactory.getAllFunction().
                forEach(entry -> context.registerFunction(entry.getKey(), entry.getValue()));
        return context;
    }

    private void saveLog(LogRecordDto logRecordDto) {
        if (logPersistenceStrategies == null) {
            return;
        }
        try {
            for (LogPersistenceStrategy strategy : logPersistenceStrategies) {
                if (strategy.supports(logRecordDto)) {
                    strategy.doPersistence(logRecordDto);
                }
            }
        } catch (Exception e) {
            throw new LogRecordException("日志持久化异常:\n", e);
        }

    }

}
