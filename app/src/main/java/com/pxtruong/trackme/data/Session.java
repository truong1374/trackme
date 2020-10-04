package com.pxtruong.trackme.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions")
public class Session {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "distance")
    public double distance;

    @ColumnInfo(name = "avg_speed")
    public double avgSpeed;

    @ColumnInfo(name = "duration")
    public long duration;

    @ColumnInfo(name = "img_uri")
    public String imgUri;

    public Session(double distance, double avgSpeed, long duration, String imgUri) {
        this.distance = distance;
        this.avgSpeed = avgSpeed;
        this.duration = duration;
        this.imgUri = imgUri;
    }
}
