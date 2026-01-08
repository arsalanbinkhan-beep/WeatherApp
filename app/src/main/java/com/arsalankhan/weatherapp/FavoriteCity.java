package com.arsalankhan.weatherapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_cities")
public class FavoriteCity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String country;
    public String state;
    public double lat;
    public double lon;
    public long addedTimestamp;

    public FavoriteCity(String name, String country, String state, double lat, double lon) {
        this.name = name;
        this.country = country;
        this.state = state;
        this.lat = lat;
        this.lon = lon;
        this.addedTimestamp = System.currentTimeMillis();
    }
}