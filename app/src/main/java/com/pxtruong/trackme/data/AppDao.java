package com.pxtruong.trackme.data;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSession(Session... sessions);

    @Query("SELECT * FROM sessions ORDER BY id DESC")
    LiveData<List<Session>> getAllSessions();

    @Query("SELECT COUNT(id) FROM sessions")
    int getSessionCount();

    @Query("UPDATE sessions SET img_uri = :imgUri WHERE id = :sessionId")
    void setSessionImageUri(int sessionId, String imgUri);

    @Query("UPDATE sessions SET img_uri = :imgUri, distance = :distance, avg_speed = :avgSpeed, duration = :duration WHERE id = :sessionId")
    void updateSession(int sessionId, String imgUri, double distance, double avgSpeed, long duration);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRoute(Route... routes);

    @Query("SELECT * FROM routes ORDER BY id ASC")
    LiveData<List<Route>> getAllRoutes();

    @Query("SELECT * FROM routes WHERE session_id = :sessionId ORDER BY id ASC")
    List<Route> getAllRouteBySession(int sessionId);
}
