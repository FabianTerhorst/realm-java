package io.realm.internal.test;

import java.util.Date;

import io.realm.Realm;
import io.realm.dynamic.RealmType;
import io.realm.entities.AllTypes;
import io.realm.internal.Table;


/**
 * Class holds helper methods for the test cases
 *
 */
public class TestHelper {


    /**
     * Returns the corresponding column type for an object.
     * @param o
     * @return
     */
    public static RealmType getColumnType(Object o){

        if (o instanceof Boolean)
            return RealmType.BOOLEAN;
        if (o instanceof String)
            return RealmType.STRING;
        if (o instanceof Long)
            return RealmType.INTEGER;
        if (o instanceof Float)
            return RealmType.FLOAT;
        if (o instanceof Double)
            return RealmType.DOUBLE;
        if (o instanceof Date)
            return RealmType.DATE;
        if (o instanceof byte[])
            return RealmType.BINARY;

        return RealmType.MIXED;
    }


    /**
     * Creates an empty table with 1 column of all our supported column types, currently 9 columns
     * @return
     */
    public static Table getTableWithAllColumnTypes(){

        Table t = new Table();

        t.addColumn(RealmType.BINARY, "binary");
        t.addColumn(RealmType.BOOLEAN, "boolean");
        t.addColumn(RealmType.DATE, "date");
        t.addColumn(RealmType.DOUBLE, "double");
        t.addColumn(RealmType.FLOAT, "float");
        t.addColumn(RealmType.INTEGER, "long");
        t.addColumn(RealmType.MIXED, "mixed");
        t.addColumn(RealmType.STRING, "string");
        t.addColumn(RealmType.TABLE, "table");

        return t;
    }

    public static void populateForMultiSort(Realm testRealm) {
        testRealm.beginTransaction();
        testRealm.clear(AllTypes.class);
        AllTypes object1 = testRealm.createObject(AllTypes.class);
        object1.setColumnLong(5);
        object1.setColumnString("Adam");

        AllTypes object2 = testRealm.createObject(AllTypes.class);
        object2.setColumnLong(4);
        object2.setColumnString("Brian");

        AllTypes object3 = testRealm.createObject(AllTypes.class);
        object3.setColumnLong(4);
        object3.setColumnString("Adam");
        testRealm.commitTransaction();
    }
}
