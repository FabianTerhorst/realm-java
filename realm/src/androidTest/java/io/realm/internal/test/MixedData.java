package io.realm.internal.test;

import io.realm.dynamic.RealmFieldType;

public class MixedData {

    public RealmFieldType type;

    public Object value;

    public MixedData(RealmFieldType type, Object value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return "MixedData [type=" + type + ", value=" + value + "]";
    }

}
