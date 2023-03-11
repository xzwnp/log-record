package icu.ynu.log.autoconfigure;

import icu.ynu.log.common.LogRecordThreadFactory;
import icu.ynu.log.evaluation.CachedSpelExpressionParser;
import icu.ynu.log.function.ParseFunctionFactory;
import icu.ynu.log.operator.DefaultOperatorGetServiceImpl;
import icu.ynu.log.operator.IOperatorGetService;
import icu.ynu.log.persistence.ElkLogPersistenceStrategy;
import icu.ynu.log.persistence.FileLogPersistenceStrategy;
import icu.ynu.log.persistence.LogPersistenceStrategy;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.appender.LogstashTcpSocketAppender;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
@ComponentScan(basePackages = "icu.ynu.log") //不加这个扫描不到包下的Component
@EnableConfigurationProperties(LogRecordProperties.class)
public class LogRecordAutoConfiguration {
    @Autowired
    LogRecordProperties logRecordProperties;

    @Bean
    ExpressionParser expressionParser() {
        return new CachedSpelExpressionParser();
    }

    @Bean
    @ConditionalOnMissingBean(IOperatorGetService.class)
    IOperatorGetService defaultOperatorGetService() {
        return new DefaultOperatorGetServiceImpl();
    }

    @Bean
    ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @Bean
    @ConditionalOnProperty(prefix = "log-record.persistence.file", name = "enabled", havingValue = "true", matchIfMissing = true)
    LogPersistenceStrategy fileLogPersistenceStrategy() {
        return new FileLogPersistenceStrategy();
    }

    @Bean
    @ConditionalOnProperty(prefix = "log-record.persistence.elk", name = "enabled", havingValue = "true")
    ElkLogPersistenceStrategy eLkLogPersistenceStrategy() {
        return new ElkLogPersistenceStrategy();
    }

    @Bean
    ParseFunctionFactory parseFunctionFactory() {
        return new ParseFunctionFactory();
    }

    @Bean(name = "logExecutor")
    ThreadPoolExecutor logExecutor() {
        LogRecordProperties.LogThreadPoolProperties poolConfig = logRecordProperties.getPool();
        RejectedExecutionHandler rejectedExecutionHandler;
        try {
            rejectedExecutionHandler = poolConfig.getRejectedExecutionHandler().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
        }
        LogRecordThreadFactory threadFactory = new LogRecordThreadFactory(poolConfig.getName());
        ArrayBlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<>(10);
        return new ThreadPoolExecutor(poolConfig.getCoreSize(), poolConfig.getMaxSize(),
                poolConfig.getKeepAliveTime(), poolConfig.getTimeUnit(),
                blockingQueue, threadFactory, rejectedExecutionHandler);
    }


}