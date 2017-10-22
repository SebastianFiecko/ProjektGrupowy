package kssr13.org.projektgrupowy.beacon;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import kssr13.org.projektgrupowy.beacon.model.BeaconRealm;

public class BeaconDatabaseHandler {
    private static final RealmConfiguration CONFIG = new RealmConfiguration.Builder()
            .name("beacons.realm")
            .build();
    private final String LOG_TAG = "[BeaconsDB]";

    public BeaconDatabaseHandler() {}

    public String getBeaconText(int id) {
        Realm realm = Realm.getInstance(CONFIG);
        BeaconRealm b = realm.where(BeaconRealm.class).equalTo("beaconId", id).findFirst();

        try {
            return b.getText();
        } catch (NullPointerException e) {
            return null;
        } finally {
            realm.close();
        }
    }

    public BeaconRealm getBeacon(int id) {
        Realm realm = Realm.getInstance(CONFIG);

        try {
            return realm.where(BeaconRealm.class)
                    .equalTo("id", id).findFirst();
        } finally {
            realm.close();
        }
    }

    public void putBeacon(final int id, final String text) {
        Realm realm = Realm.getInstance(CONFIG);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BeaconRealm b = realm.createObject(BeaconRealm.class);
                b.setId(id);
                b.setText(text);
            }
        });
        realm.close();
    }

    public void deleteBeacon(final int id) {
        Realm realm = Realm.getInstance(CONFIG);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BeaconRealm b = getBeacon(id);
                if (b != null) {
                    b.deleteFromRealm();
                }
            }
        });
        realm.close();
    }

    public void printBeacons() {
        Realm realm = Realm.getInstance(CONFIG);
        final RealmResults<BeaconRealm> beacons = realm.where(BeaconRealm.class).findAll();

        try {
            if (beacons.isEmpty()) {
                Log.d(LOG_TAG, "Beacons database is empty");
                return;
            }

            Log.d(LOG_TAG, "Printing all beacons");
            Log.d(LOG_TAG, "ID    Text");
            for (BeaconRealm b : beacons) {
                Log.d(LOG_TAG, String.format("%d  %s", b.getId(), b.getText()));
            }
        } finally {
            realm.close();
        }
    }

    public String getRealmDbPath() {
        Realm realm = Realm.getInstance(CONFIG);
        try {
            return realm.getPath();
        } finally {
            realm.close();
        }
    }

    public void fill() {
        putBeacon(1243, "XXX");
        putBeacon(5253, "YYY");
        putBeacon(9999, "ZZZ");
        putBeacon(6611, "RRR");
        putBeacon(4144, "GGG");
        Log.d(LOG_TAG, "New beacons added to database");
    }

    public void deleteAll() {
        Realm realm = Realm.getInstance(CONFIG);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                // Deleting all beacons
                realm.delete(BeaconRealm.class);
            }
        });
        realm.close();
        Log.d(LOG_TAG, "All beacons deleted from database");
    }

    public void purge() {
        Realm.deleteRealm(CONFIG);
        Log.d(LOG_TAG, "Beacons database dropped");
    }

    public RealmConfiguration getConfig() { return CONFIG; }
}
