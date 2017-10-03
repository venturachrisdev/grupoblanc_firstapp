package com.blancgrupo.apps.tripguide.presentation.ui.fragment;


import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blancgrupo.apps.tripguide.MyApplication;
import com.blancgrupo.apps.tripguide.R;
import com.blancgrupo.apps.tripguide.data.entity.api.Place;
import com.blancgrupo.apps.tripguide.data.entity.api.PlaceCover;
import com.blancgrupo.apps.tripguide.data.entity.api.PlacesWrapper;
import com.blancgrupo.apps.tripguide.data.persistence.repository.PlaceDBRepository;
import com.blancgrupo.apps.tripguide.domain.mapper.ApiPlaceMapper;
import com.blancgrupo.apps.tripguide.domain.model.PlaceModel;
import com.blancgrupo.apps.tripguide.domain.model.PlaceWithReviews;
import com.blancgrupo.apps.tripguide.domain.model.mapper.PlaceModelMapper;
import com.blancgrupo.apps.tripguide.presentation.di.component.DaggerActivityComponent;
import com.blancgrupo.apps.tripguide.presentation.di.module.ActivityModule;
import com.blancgrupo.apps.tripguide.presentation.ui.activity.PlaceDetailActivity;
import com.blancgrupo.apps.tripguide.presentation.ui.adapter.PlaceAdapter;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.PlaceVMFactory;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.PlaceViewModel;
import com.blancgrupo.apps.tripguide.utils.Constants;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.rockerhieu.rvadapter.states.StatesRecyclerViewAdapter;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends LifecycleFragment
        implements PlaceAdapter.PlaceAdapterListener {
    @BindView(R.id.favorites_rv)
    ShimmerRecyclerView recyclerView;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout swipeRefreshLayout;
    StatesRecyclerViewAdapter statesRecyclerViewAdapter;
    PlaceAdapter adapter;

    @Inject
    PlaceVMFactory placeVMFactory;
    PlaceViewModel placeViewModel;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    PlaceDBRepository placeDBRepository;

    public FavoritesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);
        ButterKnife.bind(this, v);
        DaggerActivityComponent.builder()
                .netComponent(((MyApplication) getActivity().getApplication()).getNetComponent())
                .activityModule(new ActivityModule())
                .build()
                .inject(this);
        setHasOptionsMenu(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        adapter = new PlaceAdapter(this, PlaceAdapter.PLACE_VERTICAL_ADAPTER, getActivity().getApplication());
        View emptyView = getLayoutInflater().inflate(R.layout.favorites_empty_layout, recyclerView, false);
        statesRecyclerViewAdapter = new StatesRecyclerViewAdapter(adapter, null, emptyView, null);
        recyclerView.setAdapter(statesRecyclerViewAdapter);
        statesRecyclerViewAdapter.setState(StatesRecyclerViewAdapter.STATE_EMPTY);
        recyclerView.showShimmerAdapter();
        placeViewModel = ViewModelProviders.of(this, placeVMFactory)
                .get(PlaceViewModel.class);
        final String tokenId = sharedPreferences.getString(Constants.USER_LOGGED_API_TOKEN_SP, null);

        final Observer<PlacesWrapper> observer = new Observer<PlacesWrapper>() {
            @Override
            public void onChanged(@Nullable PlacesWrapper placesWrapper) {
                recyclerView.hideShimmerAdapter();
                swipeRefreshLayout.setRefreshing(false);
                if (placesWrapper != null) {
                    List<Place> places = placesWrapper.getPlaces();
                    if (places != null && places.size() > 0) {
                        List<PlaceModel> placeModels = PlaceModelMapper.transformAll(places);
                        placeDBRepository.insertPlace(placeModels);
                        adapter.updateData(placeModels);
                        statesRecyclerViewAdapter.setState(StatesRecyclerViewAdapter.STATE_NORMAL);
                    } else {
                        statesRecyclerViewAdapter.setState(StatesRecyclerViewAdapter.STATE_EMPTY);
                    }
                } else {
                    statesRecyclerViewAdapter.setState(StatesRecyclerViewAdapter.STATE_ERROR);
                }
            }
        };

        placeDBRepository.getPlacesFavorite().observe(this, new Observer<List<PlaceModel>>() {
            @Override
            public void onChanged(@Nullable List<PlaceModel> placeModels) {
                if (placeModels != null && placeModels.size() > 0) {
                    adapter.updateData(placeModels);
                    statesRecyclerViewAdapter.setState(StatesRecyclerViewAdapter.STATE_NORMAL);
                } else {
                    placeViewModel.getMyFavorites(tokenId).observe(FavoritesFragment.this, observer);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String tokenId = sharedPreferences.getString(Constants.USER_LOGGED_API_TOKEN_SP, null);
                placeViewModel.getMyFavorites(tokenId).observe(FavoritesFragment.this, observer);
            }
        });

        return v;
    }

    @Override
    public void onPlaceClick(PlaceModel place) {
        Intent intent = new Intent(getActivity(), PlaceDetailActivity.class);
        intent.putExtra(Constants.EXTRA_PLACE_ID, place.get_id());
        startActivity(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.favorites, menu);
    }

    @Override
    public void onStop() {
        super.onStop();
        placeDBRepository.onStop();
    }
}
