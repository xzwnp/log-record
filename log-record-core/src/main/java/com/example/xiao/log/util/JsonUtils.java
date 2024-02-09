package com.example.xiao.log.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * com.example.xiao.log.util
 *
 * @author xzwnp
 * 2024/1/7
 * 15:44
 */
@Slf4j
public class JsonUtils {
	public static String toJson(Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.error("toJson error", e);
		}
		return null;
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		try {
			return new ObjectMapper().readValue(json, clazz);
		} catch (Exception e) {
			log.error("readJson error", e);
		}
		return null;
	}
}
