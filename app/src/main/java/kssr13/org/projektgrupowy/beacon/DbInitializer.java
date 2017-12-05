package kssr13.org.projektgrupowy.beacon;

import android.content.res.XmlResourceParser;
import android.util.Log;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

class DbInitializer {
    private final String LOG_TAG = "[DbHandler]";
    private DbHandler dbHandler;

    DbInitializer(DbHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    void initialize(XmlResourceParser parser) throws IOException, XmlPullParserException {
        int eventType = -1;
        while(eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String value = parser.getName();
                if (value.equals("Route")) {
                    String routeId = parser.getAttributeValue(null, "id");
                    String name = parser.getAttributeValue(null, "name");
                    dbHandler.putRoute(name, Integer.parseInt(routeId));

                } else if (value.equals("Beacon")) {
                    String beaconId = parser.getAttributeValue(null, "id");
                    String infoText = parser.getAttributeValue(null, "infoText");
                    SparseArray<String> routes = parseRouteList(parser);
                    dbHandler.putBeacon(beaconId, infoText, routes);
                }
            }
            eventType = parser.next();
        }

    }

    private SparseArray<String> parseRouteList(XmlResourceParser parser)
            throws IOException, XmlPullParserException {
        SparseArray<String> routes = new SparseArray<>();
        int eventType;
        String value;

        while(true) {
            eventType = parser.next();
            value = parser.getName();

            if (eventType == XmlResourceParser.START_TAG && value.equals("RouteInfo")) {
                String routeId = parser.getAttributeValue(null, "routeId");
                String info = parser.nextText();
                routes.append(Integer.parseInt(routeId), info);
            } else if (eventType == XmlResourceParser.END_TAG && value.equals("Beacon")) {
                break;
            } else {
                Log.e(LOG_TAG,"XML is not formatted properly");
                break;
            }
        }

        return routes;
    }
}
