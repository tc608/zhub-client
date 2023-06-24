package net.tccn;

import org.redkale.convert.json.JsonConvert;
import org.redkale.util.Resourcable;
import org.redkale.util.TypeToken;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author Liang
 * @data 2020-09-05 23:18
 */
public abstract class AbstractConsumer extends ZhubAgentProvider implements IConsumer, Resourcable {

    protected JsonConvert convert = JsonConvert.root();

    protected static String APP_NAME = "";

    private Map<String, EventType> eventMap = new ConcurrentHashMap<>();

    protected abstract String getGroupid();

    protected final Set<String> getTopics() {
        if (!eventMap.isEmpty()) {
            return eventMap.keySet();
        }

        return Set.of("-");
    }

    protected void accept(String topic, String value) {
        EventType eventType = eventMap.get(topic);

        Object data = null;
        if ("java.lang.String".equals(eventType.typeToken.getType().getTypeName())) {
            data = value;
        } else {
            data = convert.convertFrom(eventType.typeToken.getType(), value);
        }

        eventType.accept(data);
    }

    protected final void removeEventType(String topic) {
        eventMap.remove(topic);
    }

    /**
     * 不同组件的订阅事件 发送
     *
     * @param topic
     */
    protected abstract void subscribe(String topic);

    public void subscribe(String topic, Consumer<String> consumer) {
        subscribe(topic, IType.STRING, consumer);
    }

    @Override
    public <T> void subscribe(String topic, TypeToken<T> typeToken, Consumer<T> consumer) {
        if (topic.contains(",")) {
            for (String x : topic.split(",")) {
                subscribe(x, typeToken, consumer);
            }
        } else {
            eventMap.put(topic, EventType.of(topic, typeToken, consumer));
            subscribe(topic);
        }
    }

    // --------------

    @Override
    public String resourceName() {
        return super.getName();
    }
}
