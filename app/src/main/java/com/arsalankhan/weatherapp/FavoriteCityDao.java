package com.arsalankhan.weatherapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface FavoriteCityDao {
    @Query("SELECT * FROM favorite_cities ORDER BY addedTimestamp DESC")
    List<FavoriteCity> getAll();

    @Query("SELECT * FROM favorite_cities WHERE name = :name AND country = :country LIMIT 1")
    FavoriteCity findByNameAndCountry(String name, String country);

    @Insert
    void insert(FavoriteCity city);

    @Delete
    void delete(FavoriteCity city);

    @Query("DELETE FROM favorite_cities")
    void deleteAll();
}