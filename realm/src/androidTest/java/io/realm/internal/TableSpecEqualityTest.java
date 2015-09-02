package io.realm.internal;

import junit.framework.TestCase;

import io.realm.dynamic.RealmType;

public class TableSpecEqualityTest extends TestCase {

    public void testShouldMatchIdenticalSimpleSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmType.STRING, "foo");
        spec1.addColumn(RealmType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmType.STRING, "foo");
        spec2.addColumn(RealmType.BOOLEAN, "bar");

        assertTrue(spec1.equals(spec2));
    }

    public void testShouldntMatchSpecsWithDifferentColumnNames() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmType.STRING, "foo");
        spec1.addColumn(RealmType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmType.STRING, "foo");
        spec2.addColumn(RealmType.BOOLEAN, "bar2");

        assertFalse(spec1.equals(spec2));
    }

    public void testShouldntMatchSpecsWithDifferentColumnTypes() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmType.STRING, "foo");
        spec1.addColumn(RealmType.BOOLEAN, "bar");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmType.STRING, "foo");
        spec2.addColumn(RealmType.BINARY, "bar");

        assertFalse(spec1.equals(spec2));
    }

    public void testShouldMatchDeepRecursiveIdenticalSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmType.STRING, "foo");
        spec1.addColumn(RealmType.TABLE, "bar");
        spec1.getSubtableSpec(1).addColumn(RealmType.INTEGER, "x");
        spec1.getSubtableSpec(1).addColumn(RealmType.TABLE, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmType.BOOLEAN, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmType.STRING, "foo");
        spec2.addColumn(RealmType.TABLE, "bar");
        spec2.getSubtableSpec(1).addColumn(RealmType.INTEGER, "x");
        spec2.getSubtableSpec(1).addColumn(RealmType.TABLE, "sub");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmType.BOOLEAN, "b");

        assertTrue(spec1.equals(spec2));
    }

    public void testShouldNotMatchDeepRecursiveDifferentSpecs() {
        TableSpec spec1 = new TableSpec();
        spec1.addColumn(RealmType.STRING, "foo");
        spec1.addColumn(RealmType.TABLE, "bar");
        spec1.getSubtableSpec(1).addColumn(RealmType.INTEGER, "x");
        spec1.getSubtableSpec(1).addColumn(RealmType.TABLE, "sub");
        spec1.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmType.BOOLEAN, "b");

        TableSpec spec2 = new TableSpec();
        spec2.addColumn(RealmType.STRING, "foo");
        spec2.addColumn(RealmType.TABLE, "bar");
        spec2.getSubtableSpec(1).addColumn(RealmType.INTEGER, "x");
        spec2.getSubtableSpec(1).addColumn(RealmType.TABLE, "sub2");
        spec2.getSubtableSpec(1).getSubtableSpec(1).addColumn(RealmType.BOOLEAN, "b");

        assertFalse(spec1.equals(spec2));
    }

}
