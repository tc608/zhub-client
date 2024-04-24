package dev.zhub;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public interface IType {

    TypeToken<String> STRING = new TypeToken<String>() {
    };

    TypeToken<Short> SHORT = new TypeToken<Short>() {
    };
    TypeToken<Integer> INT = new TypeToken<Integer>() {
    };
    TypeToken<Long> LONG = new TypeToken<Long>() {
    };
    TypeToken<Double> DOUBLE = new TypeToken<Double>() {
    };

    TypeToken<Map<String, String>> MAP = new TypeToken<Map<String, String>>() {
    };

    TypeToken<List<Map<String, String>>> LMAP = new TypeToken<List<Map<String, String>>>() {
    };
}
