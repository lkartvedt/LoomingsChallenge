package com.lkartvedt.sada.loomings;

public class LinePair {
    private final String _key;
    private final Integer _value;

    public LinePair(String key, Integer value) {
        _key = key;
        _value = value;
    }

    public String key() {
        return _key;
    }

    public Integer value() {
        return _value;
    }
}
