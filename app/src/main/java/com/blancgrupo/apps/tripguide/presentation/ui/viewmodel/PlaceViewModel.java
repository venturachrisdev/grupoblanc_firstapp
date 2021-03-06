package com.blancgrupo.apps.tripguide.presentation.ui.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.blancgrupo.apps.tripguide.data.entity.api.PlaceDescriptionWrapper;
import com.blancgrupo.apps.tripguide.domain.model.PlaceModel;
import com.blancgrupo.apps.tripguide.domain.model.PlaceWithReviews;
import com.blancgrupo.apps.tripguide.domain.repository.PlaceRepository;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.livedata.PlaceDescriptionLiveData;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.livedata.PlaceLiveData;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.livedata.PlacesLiveData;

import java.util.List;
import java.util.Locale;

/**
 * Created by root on 8/18/17.
 */

public class PlaceViewModel extends ViewModel {
    private PlaceRepository placeRepository;
    private PlaceLiveData singlePlaceLiveData;
    private PlacesLiveData placesLiveData;
    private PlaceDescriptionLiveData placeDescriptionLiveData;
    private String placeId;
    private String apiToken;

    public PlaceViewModel(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public LiveData<PlaceWithReviews> getSinglePlace(String placeId, String apiToken) {
        if (singlePlaceLiveData == null) {
            singlePlaceLiveData = new PlaceLiveData();
            loadSinglePlace(placeId, apiToken);
            this.placeId = placeId;
            this.apiToken = apiToken;

        } else {
            if (this.placeId == null || !this.placeId.equals(placeId)) {
                this.placeId = placeId;
                loadSinglePlace(this.placeId, this.apiToken);
            }
        }
        return singlePlaceLiveData;
    }

    public LiveData<List<PlaceModel>> getMyFavorites(String apiToken) {
        if (placesLiveData == null) {
            placesLiveData = new PlacesLiveData();
        }
        loadMyFavorites(apiToken);
        return placesLiveData;
    }

    public boolean loadMyFavorites(String apiToken) {
        if (placesLiveData != null) {
            placesLiveData.loadPlaces(placeRepository.getMyFavorites(apiToken));
            return true;
        }
        return false;
    }

    public boolean isPlaceLoaded() {
        return this.placeId != null;
    }

    public boolean loadLoadedPlace(String apiToken) {
        if (singlePlaceLiveData != null) {
            singlePlaceLiveData.loadSinglePlace(
                    placeRepository.getSinglePlace(placeId, apiToken)
            );
            return true;
        }
        return false;
    }


    public boolean loadSinglePlace(String placeId, String apiToken) {
        if (singlePlaceLiveData != null) {
                singlePlaceLiveData.loadSinglePlace(
                        placeRepository.getSinglePlace(placeId, apiToken)
                );
            return true;
        }
        return false;
    }

    public LiveData<List<PlaceModel>> getPlaces() {
        if (placesLiveData == null) {
            placesLiveData = new PlacesLiveData();
            loadPlaces();
        }
        return placesLiveData;
    }

    public void loadPlaces() {
        if (placesLiveData != null) {
            placesLiveData.loadPlaces(placeRepository.getPlaces());
        }
    }

    public LiveData<PlaceWithReviews> getLoadedSinglePlace() {
        return getSinglePlace(this.placeId, this.apiToken);
    }

    public LiveData<PlaceDescriptionWrapper> getPlaceDescription(String placeId) {
        if (placeDescriptionLiveData == null) {
            placeDescriptionLiveData = new PlaceDescriptionLiveData();
            loadPlaceDescription(placeId);
        }
        return placeDescriptionLiveData;
    }

    public boolean loadPlaceDescription(String placeId) {
        if (placeDescriptionLiveData != null) {
            placeDescriptionLiveData.loadPlaceDescription(placeRepository.getPlaceDescription(
                    placeId, Locale.getDefault().getLanguage()
            ));
            return true;
        }
        return false;
    }
}
