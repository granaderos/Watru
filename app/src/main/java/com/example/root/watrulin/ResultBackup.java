package com.example.root.watrulin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
//import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ResultBackup extends AppCompatActivity {

    //private TextView lblResult;
    private ImageView imageView;
    private Button btnRecapture;

    private TextToSpeech tts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            View decor = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_FULLSCREEN;
            //int uiOption = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decor.setSystemUiVisibility(uiOption);
            // SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN, SYSTEM_UI_FLAG_LAYOUT_STABLE

            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (NullPointerException e) {}
        setContentView(R.layout.activity_result);

        //lblResult = (TextView) findViewById(R.id.lblResult);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnRecapture = (Button) findViewById(R.id.btnRecapture);

        final int objectsNum = (int) (Math.random() * (6-2) + 2);

        final ObjectDefinitions od = new ObjectDefinitions();
        final ArrayList<Integer> randomResults = new ArrayList<Integer>();
        int r;
        for(int i = 0; i < objectsNum; i++) {
            r = (int) (Math.random() * (od.objects.length-1));
            if(randomResults.size() > 0) {
                if(randomResults.contains(r)) {
                    continue;
                } else {
                    randomResults.add(r);
                }
            } else {
                randomResults.add(r);
            }
        }

        //Toast.makeText(Result.this, "objectsNum = " + objectsNum + " randomResults = " + randomResults.size(), Toast.LENGTH_LONG).show();

        try {

            Bitmap bitmap0 = BitmapFactory.decodeFile(getIntent().getStringExtra("imgDecodableString"));

            int height = bitmap0.getHeight();
            int width = bitmap0.getWidth();

            //Toast.makeText(Result.this, "Height =  " + height + " width = " + width, Toast.LENGTH_LONG).show();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap0, 0, 0, null);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            //paint.setTextSize(35);
            paint.setStrokeWidth(7);

            Paint labelPaint = new Paint();
            labelPaint.setStyle(Paint.Style.FILL);
            labelPaint.setTextSize(100);
            labelPaint.setStrokeWidth(3);

            int[] colors = {Color.RED,
                    Color.BLUE,
                    Color.CYAN,
                    Color.MAGENTA,
                    Color.GREEN,
                    Color.GRAY,
                    Color.YELLOW};

            //canvas.drawRect(left, top, right, bottom, pain);
            ArrayList<Integer> bounds = new ArrayList<Integer>();
            int left, top, right, bottom;
            for(int i = 0; i < objectsNum; i++) {
                left = (int) (Math.random() * width);
                top = (int) (Math.random() * height);

                right = (int) (Math.random() * (width-(left+30)) + left+30);
                bottom = (int) (Math.random() * (height-(top+30)) + top+30);

                bounds.add(left);
                bounds.add(top);
                bounds.add(right);
                bounds.add(bottom);
            }

            for(int i = 0; i < objectsNum; i++) {
                paint.setColor(colors[i]);
                labelPaint.setColor(colors[i]);

                canvas.drawRect(bounds.get(4*i+0), bounds.get(4*i+1), bounds.get(4*i+2), bounds.get(4*i+3), paint);
                canvas.drawText(od.objects[randomResults.get(i)], bounds.get(4*i+0)+30, bounds.get(4*i+1)+100, labelPaint);
            }

            imageView.setImageBitmap(bitmap);

        } catch (Exception e) {
            Toast.makeText(ResultBackup.this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //lblResult.setText(getIntent().getStringExtra("result"));

        btnRecapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recaptureIntent = new Intent(getApplicationContext(), AppLauncher.class);
                startActivity(recaptureIntent);

                /*Intent test = new Intent(getApplicationContext(), ImageClickableParts.class);
                startActivity(test);*/
                finish();

            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                //Toast.makeText(ProcessImage.this, "On init TTS now" + i, Toast.LENGTH_LONG).show();
                if (i != TextToSpeech.ERROR) {
                    int r = tts.setLanguage(Locale.UK);

                    if(r != TextToSpeech.LANG_MISSING_DATA || r != TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setPitch(1);
                        String text = "";
                        for(int j = 0; j < objectsNum; j++) {
                            String obj = od.objects[randomResults.get(j)];

                            text +=  obj + ". " +
                                    od.spellings.get(obj) + ". " +
                                    obj + ". " +
                                    od.definitions.get(obj) + ". ";
                            if(j < objectsNum-2) {
                                text += " Next is, ";
                            }
                        }

                        /*final String[] objs = {od.objects[randomResults.get(0)],
                                od.objects[randomResults.get(1)],
                                od.objects[randomResults.get(2)],
                                od.objects[randomResults.get(3)]};

                        final String[] spellings = {od.spellings.get(objs[0]),
                                od.spellings.get(objs[1]),
                                od.spellings.get(objs[2]),
                                od.spellings.get(objs[3])};

                        final String[] definitions = {od.definitions.get(objs[0]),
                                od.definitions.get(objs[1]),
                                od.definitions.get(objs[2]),
                                od.definitions.get(objs[3])};

                        tts.speak("Result are: " +
                                objs[0] + ". " + spellings[0] + ", " + objs[0] + ". " + definitions[0] + ". Next: " +
                                objs[1] + ". " + spellings[1] + ", " + objs[1] + ". " + definitions[1] + ". Next: " +
                                objs[2] + ". " + spellings[2] + ", " + objs[2] + ". " + definitions[2] + ". Lastly: " +
                                objs[3] + ". " + spellings[3] + ", " + objs[3] + ". " + definitions[3] + ".", TextToSpeech.QUEUE_FLUSH, null, null);
*/
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {

                            }

                            @Override
                            public void onDone(String s) {
                                tts.stop();
                                tts.shutdown();

                            }

                            @Override
                            public void onError(String s) {

                            }
                        });
                    } else {
                        Toast.makeText(ResultBackup.this, "Selected language is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResultBackup.this, "Text to speech is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
}
