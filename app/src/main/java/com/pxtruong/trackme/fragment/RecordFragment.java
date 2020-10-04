package com.pxtruong.trackme.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.pxtruong.trackme.MainActivity;
import com.pxtruong.trackme.R;
import com.pxtruong.trackme.data.AppDao;
import com.pxtruong.trackme.data.AppDatabase;
import com.pxtruong.trackme.data.Route;
import com.pxtruong.trackme.data.Session;
import com.pxtruong.trackme.model.MainViewModel;
import com.pxtruong.trackme.util.LocationBroadcastReceiver;
import com.pxtruong.trackme.util.LocationService;
import com.pxtruong.trackme.util.RadiusAnimation;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class RecordFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private MapView mMapView;
    private Context mContext;
    private TextView mTxtSpeed;
    private TextView mTxtDistance;
    private LinearLayout mBtnBar1;
    private LinearLayout mBtnBar2;
    private ImageButton mBtnPause;
    private ImageButton mBtnResume;
    private ImageButton mBtnStop;
    private String tmpImgUri;

    private final float mBaseZoomLevel = 15;

    private double mCurLatitude;
    private double mCurLongitude;

    private int mCurSession = -1;
    LocationBroadcastReceiver mBroadcastReceiver;
    private MainViewModel mMainViewModel;
    private Polyline mMapPolyline = null;
    private boolean mIsPause = true;

    private double mDistance = 0; //in km
    private double mAvgSpeed = 0;
    private double mCurSpeed = 0;
    private long mDuration = 0;

    private Chronometer mTimer;
    private long mTimeWhenStopped = 0;

    private RadiusAnimation groundAnimation;
    private AnimationSet breadingAnimations;
    private GroundOverlay mAnimateGroundOverlay;
    private Circle mPointGroundOverlay;

    public static RecordFragment newInstance() {
        return new RecordFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initiate UI
        mBtnBar1 = view.findViewById(R.id.btnBar1);
        mBtnBar2 = view.findViewById(R.id.btnBar2);

        mBtnPause = view.findViewById(R.id.btnPause);
        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });

        mBtnResume = view.findViewById(R.id.btnResume);
        mBtnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        mBtnStop = view.findViewById(R.id.btnStop);
        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
                FinishRecord();
            }
        });

        mTxtSpeed = view.findViewById(R.id.avgSpeedValue);
        mTxtSpeed.setText(String.format("%.2f km", 0.0f));
        mTxtDistance = view.findViewById(R.id.distanceValue);
        mTxtDistance.setText(String.format("%.2f km/h", 0.0f));

        // initiate timer
        mTimer = view.findViewById(R.id.durationValue);
        mTimer.setBase(SystemClock.elapsedRealtime());
        mTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer cArg) {
                long t = SystemClock.elapsedRealtime() - cArg.getBase();
                cArg.setText(String.format("%02d:%02d:%02d", t/3600000, t/60000, t/1000));
            }
        });
        mTimer.setText("00:00:00");
        mTimer.start();

        // initiate google map
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.setClickable(false);
        mMapView.getMapAsync(this);

        startService();

        // initiate session
        new Thread(new Runnable() {
            public void run() {
                AppDao dao = AppDatabase.getDatabase(mContext).appDao();
                dao.insertSession(new Session(0, 0, 0, ""));
                mCurSession = dao.getSessionCount();
            }
        }).start();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setAllGesturesEnabled(false);
        getCurrentLocation();
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
                    Bitmap bitmap;

                    @Override
                    public void onSnapshotReady(Bitmap snapshot) {
                        bitmap = snapshot;
                        ContextWrapper cw = new ContextWrapper(mContext);
                        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                        Long tsLong = System.currentTimeMillis();
                        String ts = tsLong.toString();
                        File imgPath = new File(directory, String.format("%s.jpg", ts));

                        try {
                            FileOutputStream out = new FileOutputStream(imgPath);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        tmpImgUri = Uri.fromFile(imgPath).toString();
                        mMainViewModel.updateSessionImage(mCurSession, tmpImgUri);
                    }
                };

                mMap.snapshot(callback);
            }
        });
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        stopService();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public void getCurrentLocation() {
        ((MainActivity)mContext).checkRunTimePermission();

        LocationServices.getFusedLocationProviderClient(mContext).getLastLocation().addOnSuccessListener((MainActivity)mContext, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mCurLatitude = location.getLatitude();
                    mCurLongitude = location.getLongitude();
                    mMap.addMarker(new MarkerOptions().position(new LatLng(mCurLatitude, mCurLongitude)));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurLatitude, mCurLongitude), mBaseZoomLevel));
                }
            }
        });
    }

    public void startService() {
        if(mIsPause) {
            mBroadcastReceiver = new LocationBroadcastReceiver();
            IntentFilter filter = new IntentFilter(LocationService.BROADCAST_ACTION);
            mContext.registerReceiver(mBroadcastReceiver, filter);

            Intent serviceIntent = new Intent(mContext, LocationService.class);
            serviceIntent.putExtra("inputExtra", "Workout recording ...");
            ContextCompat.startForegroundService(mContext, serviceIntent);

            mIsPause = false;
            mTimer.setBase(SystemClock.elapsedRealtime() + mTimeWhenStopped);
            mTimer.start();
        }

        mBtnBar2.setVisibility(View.VISIBLE);
        mBtnBar1.setVisibility(View.GONE);
    }

    public void stopService() {
        if(!mIsPause) {
            Intent serviceIntent = new Intent(mContext, LocationService.class);
            mContext.stopService(serviceIntent);
            mContext.unregisterReceiver(mBroadcastReceiver);

            mTimeWhenStopped = mTimer.getBase() - SystemClock.elapsedRealtime();
            mTimer.stop();
            mIsPause = true;
        }

        mBtnBar2.setVisibility(View.GONE);
        mBtnBar1.setVisibility(View.VISIBLE);
    }

    public void FinishRecord()
    {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                bitmap = snapshot;
                ContextWrapper cw = new ContextWrapper(mContext);
                File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();
                File imgPath = new File(directory, String.format("%s.jpg", ts));

                try {
                    FileOutputStream out = new FileOutputStream(imgPath);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 30, out);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                File file = new File(Uri.parse(tmpImgUri).getPath());
                file.delete();

                mMainViewModel.updateSession(mCurSession, Uri.fromFile(imgPath).toString(), mDistance, mAvgSpeed, mDuration);
                Log.d("TrackMe", String.format("FinishRecord %s, mDistance %s, mAvgSpeed %s", mDuration, mDistance, mAvgSpeed));
                ((MainActivity)mContext).transferToHistoryScreen();
            }
        };

        mMap.snapshot(callback);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        breadingAnimations = new AnimationSet(false);

        // observer database changed
        mMainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mMainViewModel.getAllRoute().observe(this, new Observer<List<Route>>() {
            @Override
            public void onChanged(@Nullable final List<Route> routes) {
                List<Route> routesFiltered = new ArrayList<Route>();
                for (Route route : routes) {
                    if(route.sessionId == mCurSession) {
                        routesFiltered.add(route);
                    }
                }

                if(routesFiltered.size() > 0) {
                    PolylineOptions options = new PolylineOptions();
                    LatLng startPos = new LatLng(mCurLatitude, mCurLongitude);

                    options.color(Color.parseColor("#FF0000"));
                    options.width(5);
                    options.visible(true);
                    options.zIndex(1f);
                    options.add(startPos);

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(startPos);

                    for (int i = 0; i < routesFiltered.size(); i++) {
                        LatLng newPos = new LatLng(routes.get(i).latitude, routesFiltered.get(i).longitude);
                        options.add(newPos);
                        builder.include(newPos);
                    }

                    if(mMapPolyline != null)
                        mMapPolyline.remove();

                    // draw polyline on map
                    mMapPolyline = mMap.addPolyline(options);

                    // update camera to fit with route
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 300);
                    mMap.animateCamera(cu);

                    if(mAnimateGroundOverlay != null)
                        mAnimateGroundOverlay.remove();

                    if(mPointGroundOverlay != null)
                        mPointGroundOverlay.remove();

                    final int widthOne = 80;
                    final int animationDurationOne = 1200;

                    // calculate point size depend on zoom level
                    double zoomLv = mMap.getCameraPosition().zoom;
                    int pointSize = (int)((mBaseZoomLevel - zoomLv)*500);
                    if(pointSize < 10)
                        pointSize = 10;

                    // Add blue point to map
                    CircleOptions circleOptions = new CircleOptions()
                            .center(mMapPolyline.getPoints().get(mMapPolyline.getPoints().size() - 1))
                            .fillColor(0xCC0000FF)
                            .strokeColor(0xFFFFFFFF)
                            .zIndex(2)
                            .radius(pointSize/10);
                    mPointGroundOverlay = mMap.addCircle(circleOptions);

                    // Add point radius animation
                    mAnimateGroundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
                            .image(BitmapDescriptorFactory.fromResource(R.drawable.dot_maker))
                            .position(mMapPolyline.getPoints().get(mMapPolyline.getPoints().size() - 1), widthOne));
                    groundAnimation = new RadiusAnimation(mAnimateGroundOverlay, pointSize);
                    groundAnimation.setRepeatCount(Animation.INFINITE);
                    groundAnimation.setRepeatMode(Animation.RESTART);
                    groundAnimation.setDuration(animationDurationOne);
                    breadingAnimations.addAnimation(groundAnimation);
                    mMapView.startAnimation(breadingAnimations);

                    // calculate current speed
                    long elapsedMillis = SystemClock.elapsedRealtime() - mTimer.getBase();
                    double curDistance = SphericalUtil.computeDistanceBetween(mMapPolyline.getPoints().get(mMapPolyline.getPoints().size() - 1), mMapPolyline.getPoints().get(mMapPolyline.getPoints().size() - 2));
                    double curDuration = (double)(elapsedMillis/1000f - mDuration)/3600f;

                    Log.d("TrackMe", String.format("curDistance %s, curDuration %s", curDistance, curDuration));

                    mDuration = elapsedMillis/1000; // in second
                    mDistance = getDistanceFromList(mMapPolyline.getPoints()); // in km
                    mAvgSpeed = mDistance/((double) elapsedMillis/3600000); // in km/h
                    mTxtSpeed.setText(String.format("%.2f km/h", (curDistance/1000)/((double)curDuration))); //in km/h
                    mTxtDistance.setText(String.format("%.2f km", mDistance));
                }
            }
        });
    }

    public double getDistanceFromList(List<LatLng> listPoints) {
        double distance = 0;

        for(int i = 0; i < listPoints.size() - 1; i++) {
            distance += SphericalUtil.computeDistanceBetween(listPoints.get(i), listPoints.get(i+1));
        }

        return  distance/1000; // convert to km
    }
}