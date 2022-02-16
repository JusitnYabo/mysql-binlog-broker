package com.xpp.mysql.binlog.broker.listener;

import com.xpp.mysql.binlog.broker.model.MetaEvent;
import com.xpp.mysql.binlog.broker.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrintMetaEventListener implements MetaEventListener {
    @Override
    public boolean subscribe(String database, String table) {
        return true;
    }

    @Override
    public void onEvent(MetaEvent event) {
        event.getValues().forEach(s -> log.info(" event {}", JsonUtils.toJson(s)));
    }
}
