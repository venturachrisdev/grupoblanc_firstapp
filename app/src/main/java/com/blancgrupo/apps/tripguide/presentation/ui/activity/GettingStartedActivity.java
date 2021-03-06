package com.blancgrupo.apps.tripguide.presentation.ui.activity;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blancgrupo.apps.tripguide.MyApplication;
import com.blancgrupo.apps.tripguide.R;
import com.blancgrupo.apps.tripguide.presentation.di.component.DaggerActivityComponent;
import com.blancgrupo.apps.tripguide.presentation.di.module.ActivityModule;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.CityVMFactory;
import com.blancgrupo.apps.tripguide.presentation.ui.viewmodel.CityViewModel;
import com.blancgrupo.apps.tripguide.utils.ApiUtils;
import com.blancgrupo.apps.tripguide.utils.Constants;
import com.blancgrupo.apps.tripguide.utils.LocationUtils;
import com.robohorse.pagerbullet.PagerBullet;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GettingStartedActivity extends AppCompatActivity
        implements LifecycleRegistryOwner, LocationListener, ApiUtils.OnBoardingListener {
    private final LifecycleRegistry registry = new LifecycleRegistry(this);
    @Inject
    SharedPreferences sharedPreferences;
    @Inject
    CityVMFactory cityVMFactory;
    CityViewModel cityViewModel;
    @BindView(R.id.viewpager)
    PagerBullet viewPager;


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_getting_started);
        ButterKnife.bind(this);
        DaggerActivityComponent
                .builder()
                .activityModule(new ActivityModule())
                .netComponent(((MyApplication)
                        getApplication()).getNetComponent())
                .build()
                .inject(this);
//        cityViewModel = ViewModelProviders.of(this, cityVMFactory)
//                .get(CityViewModel.class);
        if (sharedPreferences.contains(Constants.GETTING_STARTED_SP)) {
            onSkipFragment();
        } else {
            viewPager.setAdapter(new OnBoardingViewPagerAdapter(getSupportFragmentManager()));
        }
    }

    public void onSkipFragment() {
        if (LocationUtils.checkForPermission(this)) {
            if (LocationUtils.isGpsEnabled(this)) {
                startCityActivityAndFinish();
            } else {
                if (sharedPreferences.contains(Constants.CURRENT_LOCATION_SP)) {
                    startCityActivityAndFinish();
                } else {
                    showGpsStepFragment();
                }
            }
        }
        else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new LocationPermissionFragment())
                    .commit();
        }
    }

    private void showGpsStepFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new FirstEnableGpsFragment())
                .commit();
    }

    @Override
    public void onBoardingFinished() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Constants.GETTING_STARTED_SP, true);
        editor.apply();
    }

    public static class OnboardingFragment extends Fragment {
        ApiUtils.OnBoardingListener listener;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View root;
            root = inflater.inflate(R.layout.onboarding_layout, container, false);
            Button skipBtn = root.findViewById(R.id.skip_btn);
            skipBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onBoardingFinished();
                    ((GettingStartedActivity) getActivity()).onSkipFragment();
                }
            });
            TextView title = root.findViewById(R.id.textView1);
            TextView message = root.findViewById(R.id.textView2);
            ImageView image = root.findViewById(R.id.imageView5);
            Bundle args = getArguments();
            int step = args.getInt("type", 1);
            switch (step) {
                case 1:
                    title.setText(R.string.explore_your_city);
                    message.setText(R.string.explore_your_city_content);
                    image.setImageResource(R.mipmap.city);
                    break;
                case 2:
                    title.setText(R.string.rate_visited_places);
                    message.setText(R.string.rated_visited_places_content);
                    image.setImageResource(R.mipmap.place);
                    break;
                case 3:
                    title.setText(R.string.make_a_tour);
                    message.setText(R.string.make_a_tour_content);
                    skipBtn.setText(R.string.finish);
                    image.setImageResource(R.mipmap.tours);
                    break;
            }
            return root;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            if (context instanceof ApiUtils.OnBoardingListener) {
                listener = (ApiUtils.OnBoardingListener) context;
            } else {
                throw new RuntimeException(context + " must implement OnBoardingFinished");
            }
        }

    }

    public static class LocationPermissionFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View root;
            root = inflater.inflate(R.layout.first_location_permission_layout, container, false);
            root.findViewById(R.id.location_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(LocationUtils.checkForPermissionOrRequest(getActivity())) {
                        ((GettingStartedActivity) getActivity()).afterPermission();
                    }

                }
            });
            root.findViewById(R.id.no_thanks_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GettingStartedActivity) getActivity()).startCityActivityAndFinish();
                }
            });
            return root;
        }
    }

    public static class FirstEnableGpsFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            View root;
            root = inflater.inflate(R.layout.first_gps_on_layout, container, false);
            root.findViewById(R.id.location_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LocationUtils.showEnableGpsActivity(getActivity());
                }
            });
            root.findViewById(R.id.no_thanks_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((GettingStartedActivity) getActivity()).startCityActivityAndFinish();
                }
            });
            return root;
        }
    }


    class OnBoardingViewPagerAdapter extends FragmentPagerAdapter {
        public OnBoardingViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 3;
        }


        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt("type", position + 1);
            OnboardingFragment fragment = new OnboardingFragment();
            fragment.setArguments(args);
            return fragment;

        }

    }


    void startCityActivityAndFinish() {
        Intent intent = new Intent(this, HomeActivity.class);
        Intent original = getIntent();
        if (original.getAction() != null && original.getAction().equals(Intent.ACTION_VIEW)) {
            intent.setAction(original.getAction());
            intent.setData(original.getData());
        }
        startActivity(intent);
        finish();
    }


    public void afterPermission() {
        if (LocationUtils.isGpsEnabled(this)) {
            startCityActivityAndFinish();
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                  showGpsStepFragment();
                }
            }, 400);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LocationUtils.PERMISSION_LOCATION_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            afterPermission();
        } else {
            LocationUtils.showAreYouSureLocation(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LocationUtils.PERMISSION_ENABLE_GPS_REQUEST_CODE
                && LocationUtils.isGpsEnabled(this)) {
            startCityActivityAndFinish();

        }
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return this.registry;
    }

    @Override
    public void onLocationChanged(Location location) {
//        goForIt(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(this, "Status: " + s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String s) {
//        showLoadingCityLayout();
//        goForLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
//        showGpsStepLayout();
    }

}
