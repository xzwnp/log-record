package com.example.xiao.log.persistence;

import com.example.xiao.log.entity.LogRecordDto;

/**
 * icu.ynu.log.persistence
 *
 * @author xzwnp
 * 2023/3/2
 * 21:26
 */
public interface LogPersistenceStrategy {
	boolean supports(LogRecordDto logRecordDto);
	void doPersistence(LogRecordDto logRecordDto);
}
