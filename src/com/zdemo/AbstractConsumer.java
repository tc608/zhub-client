package com.zdemo;

import org.redkale.convert.json.JsonConvert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Liang
 * @data 2020-09-05 23:18
 */
public abstract class AbstractConsumer implements IConsumer {

    public Logger logger = Logger.getLogger(this.getClass().getSimpleName());

    public final Map<String, EventType> eventMap = new HashMap<>();

    public abstract String getGroupid();

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
    public final Set<String> getTopics() {
        if (!eventMap.isEmpty()) {
            return eventMap.keySet();
        }

        return Set.of("-");
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
