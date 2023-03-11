package icu.ynu.log.autoconfigure;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaozhiwei
 * 2023/3/5
 * 20:40
 */
@ConfigurationProperties("log-record")
@Data
public class LogRecordProperties implements InitializingBean {
    /**
     * 是否开启 todo
     */
    private boolean enable;
    /**
     * 日志线程池配置
     */
    private LogThreadPoolProperties pool = new LogThreadPoolProperties();
    /**
     * 日志持久化配置
     */
    private LogPersistenceProperties persistence = new LogPersistenceProperties();

    @Override
    public void afterPropertiesSet() throws Exception {
//        System.out.println(this);
    }

    @Data
    static class LogThreadPoolProperties {
        private String name = "log-record-pool";
        private int coreSize = 1;
        private int maxSize = 10;
        private int keepAliveTime = 1;

        private TimeUnit timeUnit = TimeUnit.MINUTES;
        private Class<? extends RejectedExecutionHandler> rejectedExecutionHandler = ThreadPoolExecutor.AbortPolicy.class;
    }

    @Data
    static class LogPersistenceProperties {
        private FilePersistenceProperties file;
        private ElkPersistenceProperties elk;
        private MysqlPersistenceProperties mysql;

    }

    @Data
    static class FilePersistenceProperties {
        private boolean enabled = true;
        ///INFO级别日志文件格式
        private String infoLogFileName = "logs/operation-info.%d.%i.log";
        private String errorLogFileName = "logs/operation-error.%d.%i.log";

        //日志发生异常时,记录的文件路径
        private String logInnerErrorFileName = "logs/log-inner-error.%d.%i.log";
    }

    @Data
    static class ElkPersistenceProperties {
        private boolean enabled = false;
        private String host;
        private String projectName;
    }

    @Data
    static class MysqlPersistenceProperties {
        private boolean enable = false;
    }
}
