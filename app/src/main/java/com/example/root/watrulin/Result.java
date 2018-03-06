package com.example.root.watrulin;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
//import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.lang.*;
import java.lang.Object;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.lukle.clickableareasimage.ClickableArea;
import at.lukle.clickableareasimage.ClickableAreasImage;
import at.lukle.clickableareasimage.OnClickableAreaClickedListener;
import uk.co.senab.photoview.PhotoViewAttacher;

public class Result extends AppCompatActivity implements OnClickableAreaClickedListener {

    //private TextView lblResult;
    private ImageView imageView;
    private Button btnRecapture;

    private TextToSpeech tts = null;
    private boolean ttsOkay = false;
    final ObjectDefinitions od = new ObjectDefinitions();

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

        // text to speech



        final int objectsNum = (int) (Math.random() * (6-2) + 2);

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

                //Toast.makeText(this, "width = " + (right-left) + " height = " + (bottom-top), Toast.LENGTH_LONG).show();
                //Toast.makeText(this, "image width = " + width + " image height = " + height, Toast.LENGTH_LONG).show();
            }

            for(int i = 0; i < objectsNum; i++) {
                paint.setColor(colors[i]);
                labelPaint.setColor(colors[i]);

                canvas.drawRect(bounds.get(4*i+0), bounds.get(4*i+1), bounds.get(4*i+2), bounds.get(4*i+3), paint);
                canvas.drawText(od.objects[randomResults.get(i)], bounds.get(4*i+0)+30, bounds.get(4*i+1)+100, labelPaint);
            }
            imageView.setImageBitmap(bitmap);

            ClickableAreasImage clickableAreasImage = new ClickableAreasImage(new PhotoViewAttacher(imageView), this);
            List<ClickableArea> clickableAreas = new ArrayList<ClickableArea>();
            for(int i = 0; i < objectsNum; i++) {
                clickableAreas.add(new ClickableArea(pxToDp(bounds.get(4*i+0)), pxToDp(bounds.get(4*i+1)),
                        pxToDp(bounds.get(4*i+2)-bounds.get(4*i+0)), pxToDp(bounds.get(4*i+3)-bounds.get(4*i+1)),
                                    new ObjectData(od.objects[randomResults.get(i)])));
            }

            clickableAreasImage.setClickableAreas(clickableAreas);
            //Toast.makeText(this," size = " + clickableAreasImage.getClickableAreas().size(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(Result.this, "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //lblResult.setText(getIntent().getStringExtra("result"));
        if(objectsNum > 1) {
            speakPlease("Results are available. Please tap on the bounded object which you are curious of.");
        } else {
            speakPlease("Result is available. Please tap on the bounded object which you are curious of.");
        }
        btnRecapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recaptureIntent = new Intent(getApplicationContext(), AppLauncher.class);
                startActivity(recaptureIntent);

                finish();
            }
        });
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public int pxToDp(int px) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    // Listen for touches on your images:
    @Override
    public void onClickableAreaTouched(Object item) {
        //Toast.makeText(this, "Clickable areas touched " + item, Toast.LENGTH_SHORT).show();
        if (item instanceof ObjectData) {
            final String objectName = ((ObjectData) item).getObjectName();
            //Toast.makeText(this, objectName, Toast.LENGTH_SHORT).show();
            final String text = objectName + ". " + od.spellings.get(objectName) + ". " +
                                objectName + ". " + od.definitions.get(objectName) + ". ";

            speakPlease(text);
            //setMessage(od.spellings.get(objectName) + "\n" + od.definitions.get(objectName)
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (!isFinishing()){
                        Drawable d = new ColorDrawable(Color.BLACK);
                        d.setAlpha(50);

                        new AlertDialog.Builder(Result.this)
                                .setTitle(objectName)
                                .setMessage("\n" + od.definitions.get(objectName) + ".")
                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Whatever...
                                        dialog.cancel();
                                    }
                                })
                                .show();
                    }
                }
            });
            //Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "At else. But why?", Toast.LENGTH_SHORT).show();
        }
    }

    public void speakPlease(final String textToSpeech) {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                //Toast.makeText(ProcessImage.this, "On init TTS now" + i, Toast.LENGTH_LONG).show();
                if (i != TextToSpeech.ERROR) {
                    int r = tts.setLanguage(Locale.UK);
                    if(r != TextToSpeech.LANG_MISSING_DATA || r != TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setPitch(1);
                        tts.speak(textToSpeech, TextToSpeech.QUEUE_FLUSH, null, null);
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
                        Toast.makeText(Result.this, "Selected language is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Result.this, "Text to speech is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
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
