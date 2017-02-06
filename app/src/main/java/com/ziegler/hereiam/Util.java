package com.ziegler.hereiam;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Gabriel on 23/01/2017.
 */

public class Util {

    public static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }


    public static BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public static void setMarkerIcon(String src, final Marker marker, Context context) {

        Glide.with(context)
                .load(src)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(Util.getCroppedBitmap(resource)));
                        return false;
                    }
                })
                .into(50, 50);
    }


    public static boolean isFirstRun(Context context) {

        SharedPreferences prefs = null;
        prefs = context.getSharedPreferences("com.ziegler.hereiam", MODE_PRIVATE);
        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).commit();
            return true;
        }
        return false;
    }


      /*private void calculateBoundary(GoogleMap map) {


        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (String k : marker.keySet()) {
            builder.include(marker.get(k).getPosition());
        }

        builder.include(map.getCameraPosition().target);
        // Creates a CameraPosition from the builder

        LatLngBounds bounds = builder.build();
        int padding = 100; // offset from edges of the map in pixels
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));

    }*/


    private boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

/*
    private void shareRoom(String keyRoom) {

        String dld = getString(R.string.DynamicLinkDomain);
        String link = "https://james-1b462.firebaseapp.com/room/?cod=";

        String pck = "&apn=com.ziegler.hereiam";
        String url = null;
        URL urla = null;
        try {
            url = dld + "?link=" + link + keyRoom.replace("-", "%2D") + pck;

            URI uri = new URI(url);
            urla = uri.toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, urla.toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, "Share with..."));
    }*/


    public static String getDifferenceMilli(long firstDate, long secondDate) {
        long diff = (firstDate - secondDate) / 1000;
        if (diff < 60) return "now";
        if (diff < 3600)
            return (diff / 60) + " minutes ago";

        if (diff < 7200)
            return ("1 hour ago");

        if (diff < 86400)
            return (diff / 3600) + " hours ago";

        if (diff > 86400)
            return (diff / 86400) + " days ago";

        return "";
    }


}

