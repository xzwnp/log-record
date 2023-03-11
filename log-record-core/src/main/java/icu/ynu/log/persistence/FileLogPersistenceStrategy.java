package icu.ynu.log.persistence;

import icu.ynu.log.entity.LogRecordDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * icu.ynu.log.persistence
 * 基于日志文件的持久化策略
 *
 * @author xzwnp
 * 2023/3/2
 * 21:27
 */
public class FileLogPersistenceStrategy implements LogPersistenceStrategy {
    private final Logger log = LoggerFactory.getLogger("FileOperationLog");


    @Override
    public boolean supports(LogRecordDto logRecordDto) {
        return true;
    }

    @Override
    public void doPersistence(LogRecordDto logRecordDto) {
        MDC.put("traceId", logRecordDto.getBizId());
        //根据是否抛异常来确定日志级别
        if (logRecordDto.getException() != null) {
            MDC.put("errorMsg", logRecordDto.getException().getMessage());
            log.error(logRecordDto.toString());
        } else {
            log.info(logRecordDto.toString());
        }
    }
}
