package com.pxtruong.trackme.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routes")
public class Route {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "session_id")
    public int sessionId;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    public  Route(int sessionId, double latitude, double longitude) {
        this.sessionId = sessionId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
