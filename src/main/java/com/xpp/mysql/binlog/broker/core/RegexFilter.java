package com.xpp.mysql.binlog.broker.core;

import lombok.Data;
import org.springframework.util.StringUtils;

/**
 * @author huyapeng
 * @date 2022/2/15
 * Email: yapeng@jspwork.cn
 */
@Data
public class RegexFilter {
    /**
     * 白名单正则
     */
    private String whiteRegex;
    /**
     * 黑名单正则
     */
    private String blackRegex;


    public boolean accept(String database, String table) {
        if (StringUtils.isEmpty(whiteRegex) && StringUtils.isEmpty(blackRegex)) {
            return true;
        }
        String name = database + "." + table;
        return name.matches(whiteRegex) && !name.matches(blackRegex);
    }

}
