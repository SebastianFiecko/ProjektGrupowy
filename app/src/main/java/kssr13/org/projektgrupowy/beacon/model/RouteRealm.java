package kssr13.org.projektgrupowy.beacon.model;

import io.realm.RealmObject;

public class RouteRealm extends RealmObject {

    private int routeId;
    private String name;

    public RouteRealm() { }

    public RouteRealm(int routeId, String name) {
        this.routeId = routeId;
        this.name = name;
    }

    public int getRouteId() {
        return routeId;
    }

    public void setRouteId(int routeId) {
        this.routeId = routeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
