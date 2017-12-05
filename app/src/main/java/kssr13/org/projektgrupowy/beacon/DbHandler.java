package kssr13.org.projektgrupowy.beacon;

import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import kssr13.org.projektgrupowy.beacon.model.BeaconRealm;
import kssr13.org.projektgrupowy.beacon.model.BeaconRouteInfoRealm;
import kssr13.org.projektgrupowy.beacon.model.RouteRealm;

public class DbHandler {
    private static final RealmConfiguration CONFIG = new RealmConfiguration.Builder()
            .name("beacons.realm")
            .build();
    private final String LOG_TAG = "[DbHandler]";
    private DbInitializer dbInitializer;
    private Realm realm;

    public DbHandler() {
        this.realm = Realm.getInstance(CONFIG);
        this.dbInitializer = new DbInitializer(this);
    }

    public void initalize(XmlResourceParser parser) {

        realm = Realm.getInstance(CONFIG);
        try {
            dbInitializer.initialize(parser);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    public BeaconRealm getBeacon(String beaconId) {
        return realm.where(BeaconRealm.class).equalTo("id", beaconId).findFirst();
    }

    public void putBeacon(final String id, final String text) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BeaconRealm b = getBeacon(id);
                if (b == null) {
                     b = realm.createObject(BeaconRealm.class);
                     b.setId(id);
                }
                b.setInfo(text);
            }
        });
    }

    public void putBeacon(final String id, final SparseArray<String> routes) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BeaconRealm b = getBeacon(id);
                if (b == null) {
                    b = realm.createObject(BeaconRealm.class);
                    b.setId(id);
                }
                saveToRealm(routes, b.getRouteList());
            }
        });
    }

    public void putBeacon(final String id, final String text, final SparseArray<String> routes) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BeaconRealm b = getBeacon(id);
                if (b == null) {
                    b = realm.createObject(BeaconRealm.class);
                    b.setId(id);
                }
                b.setInfo(text);
                saveToRealm(routes, b.getRouteList());

            }
        });
    }

    public void deleteBeacon(final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                BeaconRealm b = getBeacon(id);
                if (b != null) {
                    b.deleteFromRealm();
                }
            }
        });
    }

    public String getBeaconInfo(String beaconId) {
        BeaconRealm b = realm.where(BeaconRealm.class).equalTo("id", beaconId).findFirst();
        if (b != null) {
            return b.getInfo();
        } else {
            return null;
        }
    }

    public String getRouteForBeacon(String beaconId, int routeId) {
        BeaconRealm b = realm.where(BeaconRealm.class).equalTo("id", beaconId).findFirst();
        if (b == null) {
            return null;
        }

        BeaconRouteInfoRealm bri = b.getRouteList().where().equalTo("routeId", routeId).findFirst();
        if (bri == null) {
            return null;
        }

        return bri.getInfo();
    }

    public RouteRealm getRoute(int id) {
        return realm.where(RouteRealm.class).equalTo("routeId", id).findFirst();
    }

    public RouteRealm getRoute(String name) {
            return realm.where(RouteRealm.class).equalTo("name", name).findFirst();
    }

    public void putRoute(final String name, final int id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RouteRealm r = getRoute(id);
                if (r == null) {
                    r = realm.createObject(RouteRealm.class);
                }
                r.setRouteId(id);
                r.setName(name);
            }
        });
    }

    public void printBeacons() {
        final RealmResults<BeaconRealm> beacons = realm.where(BeaconRealm.class).findAll();
        if (beacons.isEmpty()) {
            Log.d(LOG_TAG, "Beacons database is empty");
            return;
        }

        Log.d(LOG_TAG, "Printing all beacons");
        Log.d(LOG_TAG, "ID    InfoText");
        for (BeaconRealm b : beacons) {
            Log.d(LOG_TAG, "# Beacon");
            Log.d(LOG_TAG, "  " + b.getId() + ", " + b.getInfo());

            final RealmList<BeaconRouteInfoRealm> routeList = b.getRouteList();
            for (BeaconRouteInfoRealm r : routeList) {
                Log.d(LOG_TAG, String.format("  %s -> %s", r.getRouteId(), r.getInfo()));
            }
        }
    }

    public void printRoutes() {
        final RealmResults<RouteRealm> routes = realm.where(RouteRealm.class).findAll();
        if (routes.isEmpty()) {
            Log.d(LOG_TAG, "No routes found in database");
            return;
        }

        Log.d(LOG_TAG, "Printing all routes");
        Log.d(LOG_TAG, "ID    Name");
        for (RouteRealm r : routes) {
            Log.d(LOG_TAG, String.format("%d     %s", r.getRouteId(), r.getName()));
        }
    }

    public String getRealmDbPath() {
        return realm.getPath();
    }

    public void purge() {
        realm.close();
        Realm.deleteRealm(CONFIG);
        Log.d(LOG_TAG, "Beacons database dropped");
    }

    public RealmConfiguration getConfig() {
        return CONFIG;
    }

    private void saveToRealm(SparseArray<String> routes, RealmList<BeaconRouteInfoRealm> routeList) {
        for (int i = 0; i < routes.size(); i++) {
            Integer routeId = routes.keyAt(i);
            String info = routes.valueAt(i);
            routeList.add(new BeaconRouteInfoRealm(routeId, info));
        }
    }

}
