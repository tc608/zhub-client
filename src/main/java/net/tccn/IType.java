package net.tccn;

import org.redkale.util.TypeToken;

import java.util.List;
import java.util.Map;

public interface IType {

    TypeToken<String> STRING = new TypeToken<String>() {
    };

    TypeToken<Integer> INT = new TypeToken<Integer>() {
    };

    TypeToken<Short> SHORT = new TypeToken<Short>() {
    };

    TypeToken<Long> LONG = new TypeToken<Long>() {
    };

    TypeToken<Map<String, String>> MAP = new TypeToken<Map<String, String>>() {
    };

    TypeToken<List<Map<String, String>>> LMAP = new TypeToken<List<Map<String, String>>>() {
    };

    TypeToken<List<String>> LSTRING = new TypeToken<List<String>>() {
    };

    TypeToken<List<Integer>> LINT = new TypeToken<List<Integer>>() {
    };
}
