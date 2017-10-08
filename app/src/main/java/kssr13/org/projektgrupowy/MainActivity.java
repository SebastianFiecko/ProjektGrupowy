package kssr13.org.projektgrupowy;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Button speakOut;
    private EditText txtText;
    private Button navigationButton;
    private Button informationButton;
    private TextView capturedSpeechText;
    private ImageButton speechToTextButton;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this,this);

        capturedSpeechText = (TextView) findViewById(R.id.txtSpeechInput);
        speechToTextButton = (ImageButton) findViewById(R.id.btnSpeak);
        informationButton = (Button)findViewById(R.id.informationButton);
        navigationButton = (Button)findViewById(R.id.navigationButton);

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
    }

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

