package kssr13.org.projektgrupowy;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import kssr13.org.projektgrupowy.beacon.DbHandler;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Button speakOut;
    private EditText txtText;
    private Button navigationButton;
    private Button informationButton;
    private TextView capturedSpeechText;
    private ImageButton speechToTextButton;
    private Button fillDbButton;
    private Button deleteDbButton;
    private Button printDbButton;
    private DbHandler dbHandler;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    public int device_nr = 0; /*<! Liczba wykrytych beaconów */
    List<String> device_names = new ArrayList<String>(); /*<! Lista zawierająca nazwy wykrytych beaconów */
    List<Short> device_rssi = new ArrayList<Short>(); /*<! Lista zawierająca moc sygnału wykrytych beaconów */

    BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter(); //dodanie obiektu BluetoothAdapter o nazwie 'ba'
    String final_device_names = new String();
    public short final_device_rssi = 0;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this,this);

        // Initialize Beacon database
        Realm.init(this);
        dbHandler = new DbHandler();
        Realm.setDefaultConfiguration(dbHandler.getConfig());

        capturedSpeechText = (TextView) findViewById(R.id.txtSpeechInput);
        speechToTextButton = (ImageButton) findViewById(R.id.btnSpeak);
        informationButton = (Button)findViewById(R.id.informationButton);
        navigationButton = (Button)findViewById(R.id.navigationButton);
        fillDbButton = (Button) findViewById(R.id.fillDbButton);
        deleteDbButton = (Button) findViewById(R.id.deleteDbButton);
        printDbButton = (Button) findViewById(R.id.printDbButton);

        wykryjInne();


        informationButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view){
                speakOut(informationButton);
            }
        });
        navigationButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view){
                speakOut(navigationButton);
            }
        });

        speechToTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        // For beacons database testing purposes
        fillDbButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                XmlResourceParser parser = getResources().getXml(R.xml.db_initial_data);
                dbHandler.initalize(parser);
            }
        });

        deleteDbButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                dbHandler.purge();
            }
        });

        printDbButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                dbHandler.printBeacons();
                dbHandler.printRoutes();


                /* Przykładowe zapytania do bazy */
                try {

                    /* przykład 1
                     * Pobierz info text na podstawie ID beacona
                     * dla "eti_1" powinno wypisać "Library"
                     */
                    String beaconId = "eti_1";
                    Log.d("[DbTest]", String.format("Beacon %s infoText: \"%s\"",
                            beaconId, dbHandler.getBeaconInfo(beaconId)));

                    /* przykład 2
                     * Pobierz ID trasy na podstawie celu trasy
                     * dla celu trasy "Toilet" powinno wypisać ID "5"
                     */
                    String route = "Toilet";
                    Log.d("[DbTest]", String.format("%s routeId: %d",
                            route, dbHandler.getRoute(route).getRouteId()));

                    /* przykład 3
                     * Pobierz cel trasy na podstawie ID trasy
                     * dla trasy "5" powinno wypisać cel "Toilet"
                     */
                    int routeId = 5;
                    Log.d("[DbTest]", String.format("RouteId %d name: \"%s\"",
                            routeId, dbHandler.getRoute(routeId).getName()));

                    /* przykład 4
                     * Pobierz komunikat z trybu nawigacji na podstawie ID beacona i ID trasy
                     * dla "eti_2" i trasy o ID "2" powinno wypisać "Go straight on for five metres and turn right"
                     */
                    beaconId = "eti_2";
                    routeId = 2;
                    Log.d("[DbTest]", String.format("Beacon %s routeId %d info: \"%s\"",
                            beaconId, routeId, dbHandler.getRouteForBeacon(beaconId, routeId)));

                    /* przykład 5
                     * Pobierz komunikat z trybu nawigacji na podstawie ID beacona i celu trasy
                     * dla "eti_3" i trasy o celu "Dean's office" powinno wypisać "Elevator – come on in, choose first floor"
                     */
                    beaconId = "eti_3";
                    route = "Dean's office";
                    routeId = dbHandler.getRoute(route).getRouteId();
                    Log.d("[DbTest]", String.format("Beacon %s route %s info: %s",
                            beaconId, route, dbHandler.getRouteForBeacon(beaconId, routeId)));
                } catch (NullPointerException ignored) {
                }

            }
        });
    }

    /**
     * Funkcja uruchamia metodę startDiscovery obiektu BluetoothAdapter oraz definiuje odbiorców podanych akcji
     */
    public void wykryjInne(){
        Log.d("INFO","Szukam innych urządzeń (ok 12s)");
        device_nr=0; //wyzerowanie licznika znalezionych beaconów przed rozpoczęciem kolejnego wyszukiwania
        IntentFilter filtr = new IntentFilter(BluetoothDevice.ACTION_FOUND); //określnie akcji na jaką ma odbyć się reakcja. ACTION_FOUND - znaleziono urządzenie
        this.registerReceiver(odbiorca, filtr); //określnie odbiorcy zdarzenia opisanego w 'filtr' - tzn. jeśli nastąpi akcja opisana w 'filtr' wykona się to co jest w 'odbiorca'
        IntentFilter filtr2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //określnie akcji na jaką ma odbyć się reakcja. ACTION_DISCOVERY_FINISHED - zakończenie wyszukiwania
        this.registerReceiver(odbiorca2, filtr2); //określenie odbiorcy zdarzenia opisanego w 'filtr2'
        ba.startDiscovery(); // uruchomienie metody startDiscovery obiektu BluetoothAdapter o nazwie 'ba' - rozpoczęcie skanowania urządzeń
    }

    /**
     * Definicja obiektu BroadcastReceiver o nazwie 'odbiorca'
     */
    private final BroadcastReceiver odbiorca= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            String akcja = 	i.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(akcja)){ //jeśli wykryte zdarzenie to ACTION_FOUND
                BluetoothDevice device = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); //deklaracja nowego obiektu BluetoothDevice o nazwie 'device' - jeśli zostanie wykryte urządzenia to zostanie ono przypisane do tego obiektu
                String device_name=device.getName(); //przypisanie nazwy urządzenia BT do zmiennej 'device_name'
                short rssi = i.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE); //zapisanie mocy odebranego sygnału od urządzenia 'device' do zmiennej 'rssi'
                Log.d("INFO","Znaleziono urządzenie: NR: "+device_nr+" Nazwa: "+device_name+" Siła sygnału: "+rssi);
                //String check = device_name.substring(0,3); // wycięcie pierwszych trzech znaków z nazwy urządzenia
                if(device_name.startsWith("eti") && (device_name != "null")){ //sprawdzenie czy nazwa urządzenia zaczyna się od "eti"
                    device_names.add(device_nr,device_name); //dodanie nazwy urządzenia (beacona) do listy
                    device_rssi.add(device_nr,rssi); //dodanie mocy sygnalu odb. do listy. indeks listy zaczyna się od "0"

                    Log.d("INFO","Znaleziono beacona:  Nazwa: "+device_names.get(device_nr)+" Siła sygnału: "+device_rssi.get(device_nr) +" size "+device_names.size());
                    //Wybór beacona o największej mocy
                    if(device_nr==0) {
                        final_device_names = device_names.get(device_nr);
                        final_device_rssi = device_rssi.get(device_nr);
                    }
                    else{
                        if (device_rssi.get(device_nr)>final_device_rssi){
                            final_device_names = device_names.get(device_nr);
                            final_device_rssi = device_rssi.get(device_nr);
                        }
                    }
                    device_nr=device_nr+1; // zwiększenie licznika wykrytych urządzeń
                }
            }
        }
    };

    /**
     * Definicja obiektu BroadcastReceiver o nazwie 'odbiorca2'
     */
    private final BroadcastReceiver odbiorca2= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent i) {
            String akcja = 	i.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(akcja)){ //jeśli wykryte zdarzenie to ACTION_DISCOVERY_FINISHED
                Log.d("INFO","Koniec skanowania. Urządzenia: ");

                for(int j=0; j<device_nr; j++){
                    Log.d("INFO","Nazwa: "+device_names.get(j)+" RSSI: "+device_rssi.get(j));
                } //wyświetlenie wszystkich znalezionych urządzeń w tym cyklu
                Log.d("INFO","FINAL BEACON :  Nazwa: "+final_device_names+" Siła sygnału: "+final_device_rssi);
                device_nr=0; // wyzerowanie znalezionych urządzeń
                device_names.clear();
                device_rssi.clear();
                Log.d("INFO","Rozpoczynam skanowanie");
                ba.startDiscovery(); //rozpoczęcie kolejnego skanowaniu w poszukiwaniu beaconów
            }
        }
    };

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    capturedSpeechText.setText(result.get(0));
                    //tutaj łapany jest tekst po konwersji go z powiedzianych bzdur, na dole
                    //jest prosty przykład jak to sprawdzić - co zostało uchwycone
                    /*
                    if(capturedSpeechText.getText().equals("you"))
                        capturedSpeechText.setTextColor(Color.RED);
                    else
                        capturedSpeechText.setTextColor(Color.BLUE);*/
                }
                break;
            }
        }
    }

    /*
    Checks if the library was initialized correctly and process with the Text-To-Speech
    operation -> calls the speakOut()
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(Locale.US);
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TTS","This language is not supported");
            }else{
                //btspk.setEnabled(true);
                speakOut();
            }
        }else{ Log.e("TTS","Initialization failed");}
    }

    /*
    Reads the editText field and process the speech generation process
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut(){
        // CharSequence text2 = editText.getText();
        // tts.speak(text2,TextToSpeech.QUEUE_FLUSH,null,"id1");
    }

    /*
Reads the editText field and process the speech generation process
 */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void speakOut(Button button){
        CharSequence text2 = button.getText();
        tts.speak(text2,TextToSpeech.QUEUE_FLUSH,null,"id1");
    }
}

