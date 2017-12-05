package kssr13.org.projektgrupowy.beacon.model;

import io.realm.RealmObject;

public class BeaconRouteInfoRealm extends RealmObject {

    private int routeId;
    private String info;

    public BeaconRouteInfoRealm() { }

    public BeaconRouteInfoRealm(int routeId, String info) {
        this.routeId = routeId;
        this.info = info;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
