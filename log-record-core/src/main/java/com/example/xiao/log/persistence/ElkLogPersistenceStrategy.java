package com.example.xiao.log.persistence;

import com.example.xiao.log.entity.LogRecordDto;
import com.example.xiao.log.util.DateUtils;
import com.example.xiao.log.util.JsonUtils;
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
		try {
			MDC.put("traceId", logRecordDto.getTraceId());
			MDC.put("operate", logRecordDto.getOperate());
			MDC.put("content", logRecordDto.getContent());
			MDC.put("operatorId", logRecordDto.getOperatorId());
			MDC.put("operatorName", logRecordDto.getOperatorName());
			MDC.put("operateTime", logRecordDto.getOperateTime().toString());
			MDC.put("parameters", JsonUtils.toJson(logRecordDto.getParameterMap()));
			MDC.put("returnValue", JsonUtils.toJson(logRecordDto.getReturnValue()));
			MDC.put("success", logRecordDto.isSuccess() ? "是" : "否");
			MDC.put("timeCost", String.valueOf(logRecordDto.getTimeCost()));
			MDC.put("exception", logRecordDto.getException() != null ? logRecordDto.getException().getMessage() : "");
			//根据是否抛异常来确定日志级别
			log.info("");
		} finally {
			MDC.clear();
		}

	}
}
