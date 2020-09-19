package com.zdemo;

import org.redkale.convert.json.JsonConvert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Liang
 * @data 2020-09-05 23:18
 */
public abstract class AbstractConsumer implements IConsumer {

    public final Map<String, EventType> eventMap = new HashMap<>();

    public boolean preInit() {
        return true;
    }

    public void addEventType(EventType... eventType) {
        for (EventType type : eventType) {
            String[] topics = type.topic.split(",");
            for (String topic : topics) {
                if (topic.isEmpty()) {
                    continue;
                }
                eventMap.put(topic, type);
            }
        }
    }

    @Override
    public final Collection<String> getSubscribes() {
        return eventMap.keySet();
    }

    @Override
    public final void accept(String topic, String value) {
        EventType eventType = eventMap.get(topic);

        Object data = null;
        if ("java.lang.String".equals(eventType.typeToken.getType().getTypeName())) {
            data = value;
        } else {
            data = JsonConvert.root().convertFrom(eventType.typeToken.getType(), value);
        }

        eventType.accept(data);
    }


}
