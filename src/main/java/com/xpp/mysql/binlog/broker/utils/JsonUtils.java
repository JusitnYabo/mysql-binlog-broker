package com.xpp.mysql.binlog.broker.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author huyapeng
 * @date 2019/9/5
 * Email: yapeng.hu@things-matrix.com
 */
public class JsonUtils {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        //  默认非空不输出，时间格式
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //  忽略未知字段
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 将 Java 对象转为 JSON 字符串
     */
    public static <T> String toJson(T obj) {
        String jsonStr = null;
        try {
            jsonStr = OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            logger.error("Java 转 JSON 出错！", e);
        }
        return jsonStr;
    }


    /**
     * 将 Map 转换成 Java 对象
     */
    public static <T> T fromObj(Object object, Class<T> typeOfClass) {
        T obj = null;
        if (object != null) {
            try {
                obj = OBJECT_MAPPER.convertValue(object, typeOfClass);
            } catch (Exception e) {
                logger.error("JSON 转 Java 出错！", e);
            }
        }
        return obj;
    }


    /**
     * 将 JSON 字符串转为 Java 对象
     */
    public static <T> T fromJson(String json, Class<T> typeOfClass) {
        T obj = null;
        if (json != null) {
            try {
                obj = OBJECT_MAPPER.readValue(json, typeOfClass);
            } catch (Exception e) {
                logger.error("JSON 转 Java 出错！", e);
            }
        }
        return obj;
    }

    /**
     * 将 JSON 字符串转为 Java 对象
     */
    public static <T> T fromJson(String json, TypeReference<T> valueTypeRef) {
        T obj = null;
        if (json != null) {
            try {
                obj = OBJECT_MAPPER.readValue(json, valueTypeRef);
            } catch (Exception e) {
                logger.error("JSON 转 Java 出错！", e);
            }
        }
        return obj;
    }


    /**
     * 将 JSONArray 字符串转为 Java 对象
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> typeOfClass) {
        List<T> objs = null;
        if (json != null) {
            try {
                objs = OBJECT_MAPPER.readValue(json, new TypeReference<List<T>>() {
                });
            } catch (Exception e) {
                logger.error("JSON 转 Java 出错！", e);
            }
        }

        return Objects.isNull(objs) ? Collections.emptyList() : objs;
    }


}
