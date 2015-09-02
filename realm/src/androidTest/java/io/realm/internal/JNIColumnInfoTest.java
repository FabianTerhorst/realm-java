package io.realm.internal;

import junit.framework.TestCase;

import io.realm.dynamic.RealmType;

public class JNIColumnInfoTest extends TestCase {

    Table table;

    @Override
    public void setUp() {
        table = new Table();
        table.addColumn(RealmType.STRING, "firstName");
        table.addColumn(RealmType.STRING, "lastName");
    }

    public void testShouldGetColumnInformation() {

        assertEquals(2, table.getColumnCount());

        assertEquals("lastName", table.getColumnName(1));

        assertEquals(1, table.getColumnIndex("lastName"));

        assertEquals(RealmType.STRING, table.getColumnType(1));

    }

    public void testValidateColumnInfo() {

        TableView view = table.where().findAll();

        assertEquals(2, view.getColumnCount());

        assertEquals("lastName", view.getColumnName(1));

        assertEquals(1, view.getColumnIndex("lastName"));

        assertEquals(RealmType.STRING, view.getColumnType(1));

    }

}
