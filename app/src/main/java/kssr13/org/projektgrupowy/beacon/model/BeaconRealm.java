package kssr13.org.projektgrupowy.beacon.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class BeaconRealm extends RealmObject {

    private String id;
    private String info;
    private RealmList<BeaconRouteInfoRealm> routeList;

    @Ignore
    private int sessionId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public RealmList<BeaconRouteInfoRealm> getRouteList() {
        return routeList;
    }

    public void setRouteList(RealmList<BeaconRouteInfoRealm> routeList) {
        this.routeList = routeList;
    }
}
