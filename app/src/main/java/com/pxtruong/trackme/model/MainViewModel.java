package com.pxtruong.trackme.model;

import android.app.Application;

import com.pxtruong.trackme.data.AppDao;
import com.pxtruong.trackme.data.AppDatabase;
import com.pxtruong.trackme.data.Route;
import com.pxtruong.trackme.data.Session;

import java.util.List;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends AndroidViewModel {
    private AppDao mAppDao;
    private MainModelRepo mRepository;

    public MainViewModel(Application application) {
        super(application);
        mRepository = new MainModelRepo(application);
        mAppDao = AppDatabase.getDatabase(application).appDao();
    }

    public LiveData<List<Route>> getAllRoute() {
        return mRepository.getAllRoutes();
    }

    public LiveData<List<Session>> getAllSession() {
        return mRepository.getAllSessions();
    }

    public void updateSessionImage(int sessionId, String imgUri) {
        mRepository.updateSessionImage(sessionId, imgUri);
    }

    public void updateSession(int sessionId, String imgUri, double distance, double avgSpeed, long duration) {
        mRepository.updateSession(sessionId, imgUri, distance, avgSpeed, duration);
    }

    public void insertSession(Session session) {
        mRepository.insertSession(session);
    }

    public int getSessionCount() {
        return mRepository.getSessionCount();
    }
}