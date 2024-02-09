package com.example.xiao.log.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * com.example.xiao.log.util
 *
 * @author xzwnp
 * 2024/1/7
 * 15:34
 */
public class DateUtils {
	//格式化LocalDateTime
	public static String format(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}
}
