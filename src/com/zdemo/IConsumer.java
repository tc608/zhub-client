package com.zdemo;

import org.redkale.convert.json.JsonConvert;
import org.redkale.util.TypeToken;

import java.util.Collection;
import java.util.logging.Logger;

public interface IConsumer<T extends Event> {
    Logger logger = Logger.getLogger(IConsumer.class.getSimpleName());

    Collection<String> getSubscribes();

    TypeToken<T> getTypeToken();

    void accept(T t);

    default void accept(String value) {
        System.out.println(value);
        if ("com.zdemo.Event<java.lang.String>".equals(getTypeToken().getType().toString())) {
            String _value = value.split("\"value\":")[1];
            _value = _value.substring(0, _value.length() - 1);
            Event t = JsonConvert.root().convertFrom(getTypeToken().getType(), value.replace(_value, "’‘"));
            if (_value.startsWith("\"") && _value.endsWith("\"")) {
                _value = _value.substring(1, _value.length() -1);
            }
            t.setValue(_value);
            accept((T) t);
        } else {
            Event t = JsonConvert.root().convertFrom(getTypeToken().getType(), value);
            accept((T) t);
        }
    }
}
