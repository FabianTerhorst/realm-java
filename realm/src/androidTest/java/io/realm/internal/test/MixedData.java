package io.realm.internal.test;

import io.realm.dynamic.RealmType;

public class MixedData {

    public RealmType type;

    public Object value;

    public MixedData(RealmType type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MixedData [type=" + type + ", value=" + value + "]";
    }

}
