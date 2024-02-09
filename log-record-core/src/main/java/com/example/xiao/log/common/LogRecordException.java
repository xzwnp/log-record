package com.example.xiao.log.common;

/**
 * icu.ynu.log.common
 *
 * @author xiaozhiwei
 * 2023/3/10
 * 16:25
 */
public class LogRecordException extends RuntimeException{
    public LogRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    public LogRecordException(String message) {
        super(message);
    }
}
