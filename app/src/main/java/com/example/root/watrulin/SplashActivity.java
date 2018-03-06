package com.example.root.watrulin;

import android.media.MediaPlayer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import java.util.Locale;


public class SplashActivity extends AppCompatActivity {

    private Handler handler;
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle splashBundle) {
        super.onCreate(splashBundle);

        try {
            View decor = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decor.setSystemUiVisibility(uiOption);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (NullPointerException e) {}



        setContentView(R.layout.activity_splash);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                //Toast.makeText(ProcessImage.this, "On init TTS now" + i, Toast.LENGTH_LONG).show();
                if (i != TextToSpeech.ERROR) {
                    int r = tts.setLanguage(Locale.UK);

                    if(r != TextToSpeech.LANG_MISSING_DATA || r != TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setPitch(1);

                        tts.speak("Welcome to Watru!", TextToSpeech.QUEUE_FLUSH, null, null);
                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {

                            }

                            @Override
                            public void onDone(String s) {
                                /*Intent intent = new Intent(getApplicationContext(), AppLauncher.class);
                                startActivity(intent);
                                finish();*/
                                tts.stop();
                                tts.shutdown();
                            }

                            @Override
                            public void onError(String s) {

                            }
                        });
                    } else {
                        Toast.makeText(SplashActivity.this, "Selected language is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(SplashActivity.this, "Text to speech is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        handler = new Handler();

        //MediaPlayer welcomeSound = MediaPlayer.create(SplashActivity.this, R.raw.welcome);
        //welcomeSound.start();
        handler.postDelayed(runnable, 6000) ;

    }

    @Override
    protected void onPause() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Intent intent = new Intent(getApplicationContext(), AppLauncher.class);
            startActivity(intent);
            finish();
        }
    };
}
