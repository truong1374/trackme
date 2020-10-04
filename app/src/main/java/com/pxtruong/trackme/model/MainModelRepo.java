package com.pxtruong.trackme.model;

import android.app.Application;

import com.pxtruong.trackme.data.AppDao;
import com.pxtruong.trackme.data.AppDatabase;
import com.pxtruong.trackme.data.Route;
import com.pxtruong.trackme.data.Session;

import java.util.List;

import androidx.lifecycle.LiveData;

public class MainModelRepo {
    private AppDao mAppDao;
    private LiveData<List<Session>> mAllSession;
    private LiveData<List<Route>> mAllRoute;

    MainModelRepo(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        mAppDao = db.appDao();
        mAllSession = mAppDao.getAllSessions();
        mAllRoute = mAppDao.getAllRoutes();
    }

    LiveData<List<Session>> getAllSessions() {
        return mAllSession;
    }

    LiveData<List<Route>> getAllRoutes() {
        return mAllRoute;
    }

    int getSessionCount() {
        return mAppDao.getSessionCount();
    }

    List<Route> getAllRoutesBySession(int sessionId) {
        return mAppDao.getAllRouteBySession(sessionId);
    }

    void insertSession(Session session) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAppDao.insertSession(session);
        });
    }

    void insertRoute(Route route) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAppDao.insertRoute(route);
        });
    }

    void updateSessionImage(int sessionId, String imgUri) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAppDao.setSessionImageUri(sessionId, imgUri);
        });
    }

    void updateSession(int sessionId, String imgUri, double distance, double avgSpeed, long duration) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            mAppDao.updateSession(sessionId, imgUri, distance, avgSpeed, duration);
        });
    }
}
