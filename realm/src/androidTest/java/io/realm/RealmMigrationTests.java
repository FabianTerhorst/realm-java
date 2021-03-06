package io.realm;

import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;

import io.realm.entities.AllTypes;
import io.realm.entities.FieldOrder;
import io.realm.entities.AnnotationTypes;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.internal.ColumnType;
import io.realm.internal.Table;

public class RealmMigrationTests extends AndroidTestCase {

    public Realm realm;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (realm != null) {
            realm.close();
        }
    }

    public void testRealmClosedAfterMigrationException() throws IOException {
        String REALM_NAME = "default0.realm";
        RealmConfiguration realmConfig = TestHelper.createConfiguration(getContext(), REALM_NAME);
        Realm.deleteRealm(realmConfig);
        TestHelper.copyRealmFromAssets(getContext(), REALM_NAME, REALM_NAME);
        try {
            Realm.getInstance(realmConfig);
            fail("A migration should be triggered");
        } catch (RealmMigrationNeededException expected) {
            Realm.deleteRealm(realmConfig); // Delete old realm
        }

        // This should recreate the Realm with proper schema
        Realm realm = Realm.getInstance(realmConfig);
        int result = realm.where(AllTypes.class).equalTo("columnString", "Foo").findAll().size();
        assertEquals(0, result);
    }

    // If a migration creates a different ordering of columns on Realm A, while another ordering is generated by
    // creating a new Realm B. Global column indices will not work. They must be calculated for each Realm.
    public void testLocalColumnIndices() throws IOException {
        String MIGRATED_REALM = "migrated.realm";
        String NEW_REALM = "new.realm";

        // Migrate old Realm to proper schema

        // V1 config
        RealmConfiguration v1Config = new RealmConfiguration.Builder(getContext())
                .name(MIGRATED_REALM)
                .schema(AllTypes.class)
                .schemaVersion(1)
                .build();
        Realm.deleteRealm(v1Config);
        Realm oldRealm = Realm.getInstance(v1Config);
        oldRealm.close();

        // V2 config
        RealmMigration migration = new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table languageTable = realm.getTable(FieldOrder.class);
                if (languageTable.getColumnCount() == 0) {
                    languageTable.addColumn(ColumnType.INTEGER, "field2");
                    languageTable.addColumn(ColumnType.BOOLEAN, "field1");
                }

                return version + 1;
            }
        };

        RealmConfiguration v2Config = new RealmConfiguration.Builder(getContext())
                .name(MIGRATED_REALM)
                .schema(AllTypes.class, FieldOrder.class)
                .schemaVersion(2)
                .migration(migration)
                .build();
        oldRealm = Realm.getInstance(v2Config);

        // Create new Realm which will cause column indices to be recalculated based on the order in the java file
        // instead of the migration
        RealmConfiguration newConfig = new RealmConfiguration.Builder(getContext())
                .name(NEW_REALM)
                .schemaVersion(2)
                .schema(AllTypes.class, FieldOrder.class)
                .build();
        Realm.deleteRealm(newConfig);
        Realm newRealm = Realm.getInstance(newConfig);
        newRealm.close();

        // Try to query migrated realm. With local column indices this will work. With global it will fail.
        assertEquals(0, oldRealm.where(FieldOrder.class).equalTo("field1", true).findAll().size());
        oldRealm.close();
    }

    public void testNotSettingIndexThrows() {
        RealmMigration migration = new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                long columnIndex = table.addColumn(ColumnType.INTEGER, "id");
                table.setPrimaryKey("id");
                // Primary key will be indexed automatically
                table.addSearchIndex(columnIndex);
                table.addColumn(ColumnType.STRING, "indexString");
                table.addColumn(ColumnType.STRING, "notIndexString");
                // Forget to set @Index
                return 1;
            }
        };
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
                .schemaVersion(1)
                .schema(AnnotationTypes.class)
                .migration(migration)
                .build();
        Realm.deleteRealm(realmConfig);
        Realm.migrateRealm(realmConfig);

        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }

    public void testNotSettingPrimaryKeyThrows() {
        RealmMigration migration = new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                if (table.getColumnCount() == 0) {
                    long columnIndex = table.addColumn(ColumnType.INTEGER, "id");
                    table.addSearchIndex(columnIndex);
                    // Forget to set @PrimaryKey
                    columnIndex = table.addColumn(ColumnType.STRING, "indexString");
                    table.addSearchIndex(columnIndex);
                    table.addColumn(ColumnType.STRING, "notIndexString");
                }
                return 1;
            }
        };
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
                .schemaVersion(1)
                .schema(AnnotationTypes.class)
                .migration(migration)
                .build();
        Realm.deleteRealm(realmConfig);
        Realm.migrateRealm(realmConfig);

        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException e) {
            if (!e.getMessage().equals("Primary key not defined for field 'id'")) {
                fail(e.getMessage());
            }
        }
    }

    public void testSetAnnotations() {
        RealmMigration migration = new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                long columnIndex = table.addColumn(ColumnType.INTEGER, "id");
                table.setPrimaryKey("id");
                // Primary key will be indexed automatically
                table.addSearchIndex(columnIndex);
                columnIndex = table.addColumn(ColumnType.STRING, "indexString");
                table.addSearchIndex(columnIndex);
                table.addColumn(ColumnType.STRING, "notIndexString");
                return 1;
            }
        };

        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
                .schemaVersion(1)
                .schema(AnnotationTypes.class)
                .migration(migration)
                .build();
        Realm.deleteRealm(realmConfig);
        Realm.migrateRealm(realmConfig);

        realm = Realm.getInstance(realmConfig);
        Table table = realm.getTable(AnnotationTypes.class);
        assertEquals(3, table.getColumnCount());
        assertTrue(table.hasPrimaryKey());
        assertTrue(table.hasSearchIndex(table.getColumnIndex("indexString")));
    }

    public void testNotSettingIndexForPrimaryKeyThrows() {
        RealmMigration migration = new RealmMigration() {
            @Override
            public long execute(Realm realm, long version) {
                Table table = realm.getTable(AnnotationTypes.class);
                table.addColumn(ColumnType.INTEGER, "id");
                table.setPrimaryKey("id");
                // Forget to add search index primary key
                long columnIndex = table.addColumn(ColumnType.STRING, "indexString");
                table.addSearchIndex(columnIndex);
                table.addColumn(ColumnType.STRING, "notIndexString");
                return 1;
            }
        };
        RealmConfiguration realmConfig = new RealmConfiguration.Builder(getContext())
                .schemaVersion(1)
                .schema(AnnotationTypes.class)
                .migration(migration)
                .build();
        Realm.deleteRealm(realmConfig);
        Realm.migrateRealm(realmConfig);

        try {
            realm = Realm.getInstance(realmConfig);
            fail();
        } catch (RealmMigrationNeededException expected) {
        }

    }

    public void testGetPathFromMigrationException() throws IOException {
        TestHelper.copyRealmFromAssets(getContext(), "default0.realm", Realm.DEFAULT_REALM_NAME);
        File realm = new File(getContext().getFilesDir(), Realm.DEFAULT_REALM_NAME);
        try {
            Realm.getInstance(getContext());
            fail();
        } catch (RealmMigrationNeededException expected) {
            assertEquals(expected.getPath(), realm.getCanonicalPath());
        }
    }

    // In default-before-migration.realm, CatOwner has a RealmList<Dog> field.
    // This is changed to RealmList<Cat> and getInstance() must throw an exception.
    public void testRealmListChanged() throws IOException {
        TestHelper.copyRealmFromAssets(getContext(), "default-before-migration.realm", Realm.DEFAULT_REALM_NAME);
        try {
            realm = Realm.getInstance(getContext());
            fail();
        } catch (RealmMigrationNeededException expected) {
        }
    }
}
