package com.zdemo;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public interface IType {

    TypeToken<String> STRING = new TypeToken<>() {
    };

    TypeToken<Integer> INT = new TypeToken<>() {
    };

    TypeToken<Map<String, String>> MAP = new TypeToken<>() {
    };

    TypeToken<List<Map<String, String>>> LMAP = new TypeToken<>() {
    };
}
