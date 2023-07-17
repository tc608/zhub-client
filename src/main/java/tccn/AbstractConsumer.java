package tccn;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import tccn.zhub.Rpc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Liang
 * @data 2020-09-05 23:18
 */
public abstract class AbstractConsumer implements IConsumer {

    public Gson gson = Rpc.gson;

    // @Resource(name = "APP_NAME")
    protected String APP_ID = "";

    private Map<String, EventType> eventMap = new HashMap<>();

    protected abstract String getGroupid();

    protected boolean preInit() {
        return true;
    }

    protected final Set<String> getTopics() {
        if (!eventMap.isEmpty()) {
            return eventMap.keySet();
        }
        HashSet<String> set = new HashSet<>();
        set.add("-");

        return set;
    }

    protected void accept(String topic, String value) {
        EventType eventType = eventMap.get(topic);

        Object data = null;
        if ("java.lang.String".equals(eventType.typeToken.getType().getTypeName())) {
            data = value;
        } else {
            data = gson.fromJson(value, eventType.typeToken.getType());
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
        subscribe(topic, TYPE_TOKEN_STRING, consumer);
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

}
