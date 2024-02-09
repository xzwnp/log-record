package com.example.xiao.log.autoconfigure;

import com.example.xiao.log.common.LogRecordThreadFactory;
import com.example.xiao.log.evaluation.CachedSpelExpressionParser;
import com.example.xiao.log.function.ParseFunctionFactory;
import com.example.xiao.log.operator.DefaultOperatorGetServiceImpl;
import com.example.xiao.log.operator.IOperatorGetService;
import com.example.xiao.log.persistence.ElkLogPersistenceStrategy;
import com.example.xiao.log.persistence.FileLogPersistenceStrategy;
import com.example.xiao.log.persistence.LogPersistenceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
@ComponentScan(basePackages = "com.example.xiao.log")
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
    @ConditionalOnProperty(prefix = "log-record.persistence.logstash", name = "enabled", havingValue = "true")
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