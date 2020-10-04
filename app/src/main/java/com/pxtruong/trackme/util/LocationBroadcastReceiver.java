package com.pxtruong.trackme.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.pxtruong.trackme.data.AppDatabase;
import com.pxtruong.trackme.data.Route;

public class LocationBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().matches(LocationService.BROADCAST_ACTION)) {
            final double latitude = intent.getDoubleExtra("Latitude", 0);
            final double longitude = intent.getDoubleExtra("Longitude", 0);
            Log.d("TrackMe", String.format("LocationBroadcastReceiver latitude: %s, longitude %s", latitude, longitude));

            new Thread(new Runnable() {
                public void run() {
                    AppDatabase db = AppDatabase.getDatabase(context);
                    int sessionId = db.appDao().getSessionCount();
                    Route newRoute = new Route(sessionId, latitude, longitude);
                    db.appDao().insertRoute(newRoute);
                }
            }).start();

        }
    }
}
