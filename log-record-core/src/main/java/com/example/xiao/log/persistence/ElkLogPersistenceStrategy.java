package com.example.xiao.log.persistence;

import com.example.xiao.log.entity.LogRecordDto;
import com.example.xiao.log.util.DateUtils;
import com.example.xiao.log.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.format.DateTimeFormatter;

/**
 * com.example.xiao.log.persistence
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
			MDC.put("operateTime", logRecordDto.getOperateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
			MDC.put("parameters", JsonUtils.toJson(logRecordDto.getParameterMap()));
			MDC.put("returnValue", JsonUtils.toJson(logRecordDto.getReturnValue()));
			MDC.put("success", String.valueOf(logRecordDto.isSuccess()));
			MDC.put("timeCost", String.valueOf(logRecordDto.getTimeCost()));
			MDC.put("exception", logRecordDto.getException() != null ? logRecordDto.getException().getMessage() : "");
			log.info("");
		} finally {
			MDC.clear();
		}

	}
}
