package com.zdemo;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

public interface IType {

    TypeToken<String> STRING = new TypeToken<String>() {
    };

    TypeToken<Integer> INT = new TypeToken<Integer>() {
    };

    TypeToken<Map<String, String>> MAP = new TypeToken<Map<String, String>>() {
    };

    TypeToken<List<Map<String, String>>> LMAP = new TypeToken<List<Map<String, String>>>() {
    };
}
