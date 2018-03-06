package com.example.root.watrulin;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageClickableParts extends AppCompatActivity implements  View.OnTouchListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_clickable_parts);

        ImageView iv = (ImageView) findViewById(R.id.image);
        if(iv != null) {
            iv.setOnTouchListener(this);
        }

        Toast.makeText(ImageClickableParts.this, "Touch the screen to discover where the regions are.", Toast.LENGTH_SHORT).show();

    }

    public boolean onTouch(View v, MotionEvent ev) {
        boolean handlerHere = false;

        final int action = ev.getAction();

        final int evX = (int) ev.getX();
        final int evY = (int) ev.getY();
        int nextImage = -1;

        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        if(imageView == null) return false;

        Integer tagNum = (Integer) imageView.getTag();
        int currentResource = (tagNum == null) ? R.mipmap.p2_ship_default : tagNum.intValue();


        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if(currentResource == R.mipmap.p2_ship_default) {
                    nextImage = R.mipmap.p2_ship_pressed;
                }
                break;
            case MotionEvent.ACTION_UP:
                int touchColor = getHotspotColor(R.id.imageareas, evX, evY);
                int tolerance = 25;
                nextImage = R.mipmap.p2_ship_default;

                if(closeMatch(Color.RED, touchColor, tolerance)) {
                    nextImage = R.mipmap.p2_ship_alien;
                } else {

                }
                break;
            default:
                handlerHere = false;
        }

        if(handlerHere) {
            if(nextImage > 0) {
                imageView.setImageResource(nextImage);
                imageView.setTag(nextImage);
            }
        }

        return handlerHere;
    }

    @Override
    protected void onResume() {
        super.onResume();

        /*View v = findViewById(R.id.wglxy_bar);
        if(v != null) {
            Animation anim1 = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            //anim1.setAnimationListener(new StartA);
            v.startAnimation(anim1);
        }*/
    }

    /*public void onClickWglxy(View v) {
        Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse())
        startActivity(viewIntent);
    }*/

    public int getHotspotColor(int hotspotId, int x, int y) {

        ImageView img = (ImageView) findViewById(hotspotId);

        if(img == null) {
            Log.d("ImageAreasActivity", "Hot spot image not found");
            return 0;
        } else {
            img.setDrawingCacheEnabled(true);
            Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());
            if(hotspots == null) {
                Log.d("ImageAreasActivity", "Hot spot bitmap was not created");
                return 0;
            } else {
                img.setDrawingCacheEnabled(false);
                return hotspots.getPixel(x, y);
            }

        }
    }

    public boolean closeMatch(int color1, int color2, int tolerance) {
        if((int) Math.abs(Color.red(color1) - Color.red(color2)) > tolerance)
            return false;
        if((int) Math.abs(Color.green(color1) - Color.green(color2)) > tolerance)
            return false;
        if((int) Math.abs(Color.blue(color1) - Color.blue(color2)) > tolerance)
            return false;
        return true;
    }
}
