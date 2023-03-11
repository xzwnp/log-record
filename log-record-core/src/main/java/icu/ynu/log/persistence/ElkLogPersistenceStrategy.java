package icu.ynu.log.persistence;

import icu.ynu.log.entity.LogRecordDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * icu.ynu.log.persistence
 *
 * @author xiaozhiwei
 * 2023/3/9
 * 15:59
 */

public class ElkLogPersistenceStrategy implements LogPersistenceStrategy {
    private final Logger log = LoggerFactory.getLogger("ElkOperationLog");

    @Override
    public boolean supports(LogRecordDto logRecordDto) {
        return true;
    }

    @Override
    public void doPersistence(LogRecordDto logRecordDto) {
        //根据是否抛异常来确定日志级别
        if (logRecordDto.getException() != null) {
            log.error(logRecordDto.toJsonString());
        } else {
            log.info(logRecordDto.toJsonString());
        }
    }
}
