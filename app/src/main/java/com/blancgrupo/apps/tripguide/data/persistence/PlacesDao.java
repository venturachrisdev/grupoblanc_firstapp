package com.blancgrupo.apps.tripguide.data.persistence;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.blancgrupo.apps.tripguide.domain.model.PlaceModel;

import java.util.List;

import io.reactivex.Flowable;
/**
 * Created by root on 8/15/17.
 */

@Dao
public interface PlacesDao {

    @Query("SELECT * FROM places ORDER BY createdAt DESC")
    LiveData<List<PlaceModel>> getPlaces();

    @Query("SELECT * FROM places WHERE placeId = :placeId LIMIT 1")
    LiveData<PlaceModel> getPlaceById(String placeId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlace(PlaceModel place);

    @Delete
    void deletePlace(PlaceModel place);

    @Query("DELETE FROM places")
    void deleteAllPlaces();

    @Update
    void updatePlace(PlaceModel place);
}

