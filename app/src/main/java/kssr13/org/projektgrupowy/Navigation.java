package kssr13.org.projektgrupowy;

import android.content.Context;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Locale;
import android.speech.tts.Voice;

public class Navigation extends Fragment{

    private static Application sApplication;

    public static Application getsApplication(){
        return sApplication;
    }

    public static Context getApplicationContext(){
        return getsApplication().getApplicationContext();
    }

    public Navigation() {
        // Required empty public constructor
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        /*
        TextToSpeech t;
        t = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) t.setLanguage(Locale.UK);
            }
        });
        String toSpeak = "Navigation";
        t.speak(toSpeak,TextToSpeech.QUEUE_FLUSH,null,null);
        */
        return inflater.inflate(R.layout.navigation_tab, container, false);
    }

}
