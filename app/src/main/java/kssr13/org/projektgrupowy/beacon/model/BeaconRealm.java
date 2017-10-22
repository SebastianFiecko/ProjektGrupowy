package kssr13.org.projektgrupowy.beacon.model;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class BeaconRealm extends RealmObject {

    private int id;
    private String text;
    // Add here any number of attributes of any type

    @Ignore
    private int sessionId;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
}
