package com.example.root.watrulin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

public class ProcessImage extends AppCompatActivity {

    private TextView lblProcessing;
    private ImageView imgToProcess;
    String decodableString = "",
            imgDecodableBackup = "";

    Socket socket = null;
    DataOutputStream dataOutputStream = null;
    DataInputStream dataInputStream = null;

    byte[] dataToSend;


    ServerSocket serverSocket = null;
    String result = null;

    TextToSpeech tts = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            View decor = getWindow().getDecorView();
            int uiOption = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decor.setSystemUiVisibility(uiOption);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        } catch (NullPointerException e) {}
        setContentView(R.layout.activity_process_image);

        lblProcessing = (TextView) findViewById(R.id.lblProcessing);
        imgToProcess = (ImageView) findViewById(R.id.imgToProcess);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                //Toast.makeText(ProcessImage.this, "On init TTS now" + i, Toast.LENGTH_LONG).show();
                if (i != TextToSpeech.ERROR) {
                    int r = tts.setLanguage(Locale.UK);
                    if(r != TextToSpeech.LANG_MISSING_DATA || r != TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts.setPitch(1);
                        tts.speak("Processing image. Please wait.", TextToSpeech.QUEUE_FLUSH, null, null);

                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                            @Override
                            public void onStart(String s) {

                            }

                            @Override
                            public void onDone(String s) {
                                tts.stop();
                                tts.shutdown();

                                if(!decodableString.equals("")) {
                                    Toast.makeText(ProcessImage.this, "May content po and DecodableString", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ProcessImage.this, "DecodableString is empty? Oh no", Toast.LENGTH_LONG).show();
                                }
                            }
                            @Override
                            public void onError(String s) {

                            }
                        });
                    } else {
                        Toast.makeText(ProcessImage.this, "Selected language is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProcessImage.this, "Text to speech is not supported on your device. Sad.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(getIntent().getExtras() != null) {

            decodableString = getIntent().getStringExtra("imgDecodableString");
            imgDecodableBackup = decodableString;
            imgToProcess.setImageBitmap(BitmapFactory.decodeFile(decodableString));

            sendImage();
        } else {
            Toast.makeText(ProcessImage.this, "Something went wrong on extras. Sad.", Toast.LENGTH_SHORT).show();
        }
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

    public void sendImage() {
        new Thread() {
            @Override
            public void run() {
                //sleepThread();
                try {
                    try {
                        int port = 8888;
                        socket = new Socket("192.168.43.15", port); // Derwin's PC
                        //socket = new Socket("119.95.214.176", port); // Derwin's PC's public IP

                        dataOutputStream = new DataOutputStream(socket.getOutputStream());
                        dataInputStream = new DataInputStream(socket.getInputStream());

                        if(!decodableString.equals("")) {
                            Bitmap src = BitmapFactory.decodeFile(decodableString);
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                            src.compress(Bitmap.CompressFormat.PNG, 100, byteOut);

                            dataToSend = byteOut.toByteArray();

                            dataOutputStream.write(dataToSend);
                            dataOutputStream.flush();

                            decodableString = "";
                        }

                        socket.close();
                    } catch(UnknownHostException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProcessImage.this, "Sorry, the server is down.", Toast.LENGTH_SHORT).show();
                                tts.speak("Apologies for the inconvenience. The server is down at the moment.", TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        });

                        Thread.currentThread().interrupt();
                    }

                    serverSocket = new ServerSocket(1223);
                    Socket socket1 = null;

                    while(!Thread.currentThread().isInterrupted()) {
                        try {
                            socket1 = serverSocket.accept();

                            InputStream is = socket1.getInputStream();

                            int lockSeconds = 5*1000;
                            long lockThreadCheckPoint = System.currentTimeMillis();
                            int availableBytes = is.available();

                            while(availableBytes < 1 && (System.currentTimeMillis() < lockThreadCheckPoint + lockSeconds)) {
                                try {
                                    Thread.sleep(10);
                                } catch(InterruptedException e) {
                                    e.printStackTrace();
                                }

                                availableBytes = is.available();
                            }

                            byte[] buffer = new byte[availableBytes];

                            is.read(buffer, 0, availableBytes);



                            result = new String(buffer);
                            is.close();
                            socket1.close();

                            if(result != null) {


                                Thread.currentThread().interrupt();

                                Intent displayResultIntent = new Intent(getApplicationContext(), Result.class);
                                displayResultIntent.putExtra("result", result);
                                displayResultIntent.putExtra("imgDecodableString", imgDecodableBackup);

                                //result = null;

                                startActivity(displayResultIntent);
                                finish();

                            }



                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } catch (IOException e) {
                    Toast.makeText(ProcessImage.this, "Something went wrong on the thread. " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }.start();
    }

    public void sleepThread() {
        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

}


/*
* while (!goOut) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ProcessImage.this, "Inside while(!goOut) decodableString = " + decodableString, Toast.LENGTH_SHORT).show();
                            }
                        });

                        if (dataInputStream.available() > 0) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProcessImage.this, "dataInputStream.available() > 0 ", Toast.LENGTH_LONG).show();
                                }
                            });

                            receivedData += dataInputStream.readUTF();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProcessImage.this, "receivedData = " + receivedData, Toast.LENGTH_LONG).show();
                                }
                            });

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // things to do here with the received data

                                    try {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                                        builder.setMessage("Hi Mj! " + receivedData)
                                                .setTitle("For debugging purposes;");
                                        AlertDialog dialog = builder.create();
                                        dialog.show();

                                        goOut = true;
                                        socket.close();

                                        Intent displayResultIntent = new Intent(getApplicationContext(), Result.class);
                                        displayResultIntent.putExtra("result", receivedData);
                                        startActivity(displayResultIntent);
                                    } catch (IOException e) {

                                    }
                                }
                            });
                        }

//                if (dataToSend.length > 0) {
//                    dataOutputStream.write(dataToSend);
//                    dataOutputStream.flush();
//                    dataToSend = null;
//                }
                    }
*
* */