package com.zomatosampleapp.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.zomatosampleapp.R;
import com.zomatosampleapp.adapters.RestaurantsAdapter;
import com.zomatosampleapp.compartors.RatingComparator;
import com.zomatosampleapp.interfaces.ServerCallback;
import com.zomatosampleapp.model.AllDetailsOnLocation;
import com.zomatosampleapp.model.BestRatedRestaurant;
import com.zomatosampleapp.model.LocationResponse;
import com.zomatosampleapp.network.Consts;
import com.zomatosampleapp.network.RequestManager;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import jp.wasabeef.recyclerview.animators.adapters.ScaleInAnimationAdapter;

public class MainActivity extends AppCompatActivity implements ServerCallback ,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    @InjectView(R.id.recycler_view)
    RecyclerView listView;
    @InjectView(R.id.snackBarLayout)
    View snackBarLayout;
    @InjectView(R.id.city_name)
    EditText cityName;
    @InjectView(R.id.progress_layout)
    RelativeLayout progressLayout;
    @InjectView(R.id.city_address)
    TextView currentAddress;
    @InjectView(R.id.search_layout)
    TextInputLayout searchLayout;
    @InjectView(R.id.restaurant_search)
    EditText searchRestaurant;


    private LocationManager locationManager;
    private String provider;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private List<BestRatedRestaurant> restaurantArrayList;
    private RestaurantsAdapter adapter;
    private List<BestRatedRestaurant> copyRestaurantArrList;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if(mLocationRequest == null) {
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(50000);
            mLocationRequest.setFastestInterval(50000);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            buildAlertMessageNoGps();
        }

        requestLocationPermission();

        cityName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        searchRestaurant.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchForRestaurantName(searchRestaurant.getText().toString());
            }
        });

        showKeyboard(this, cityName, false);
    }

    /**
     * Method to get restaurants based on the city name entered in cityname edit text
     * or the location got from current user location
     */
    private void performSearch() {
        progressLayout.setVisibility(View.VISIBLE);
        HashMap<String, String> params = new HashMap<>();
        params.put(Consts.QUERY, cityName.getText().toString());
        RequestManager.getInstance(this).placeRequest(Consts.LOCATIONS_API, LocationResponse.class, this, params, false);
    }


    /**
     * Method to alert the user if gps is not enabled and ask to enable
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your device GPS seems to be disabled, do you want to enable it ?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Requests the Location permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                requestPermission();
            } else {

            }
        }else{
            startLocationUpdates();
        }

    }

    /**
     * Method to start the location updates
     */
    protected void startLocationUpdates() {
        if(mGoogleApiClient !=null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     *
     * Method to check whether the permission is granted or not
     */
    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Method to request location permission
     */
    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Snackbar.make(snackBarLayout, "Location access is required to fetch best restaurants in your location.",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {
                }
                return;
            }

        }

    }

    /**
     * @param latitude
     * @param longitude
     * Method to get the userLocation on the latitude and longitude
     */
    private void getUserLocation(double latitude, double longitude){
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(latitude, longitude, 1);
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i=0; i<maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }
            String finalAddress = builder.toString(); //This is the complete address.
            Log.d("LOG", "getUserLocation Called : City Name = "+finalAddress);

            cityName.setText(finalAddress);

            performSearch();
            showKeyboard(this, cityName, false);

        } catch (IOException e) {

        }
        catch (NullPointerException e) {

        }

    }



    @Override
    public void complete(int code) {

    }

    @Override
    public void onAPIResponse(Object response, String apiMethod) {

        if(apiMethod.equals(Consts.LOCATIONS_API)){
            LocationResponse locationDeatils = (LocationResponse) response;
            if(locationDeatils.getLocationSuggestions().size() >0) {
                int entityID = locationDeatils.getLocationSuggestions().get(0).getEntityId();
                String entityType = locationDeatils.getLocationSuggestions().get(0).getEntityType();

                currentAddress.setText(locationDeatils.getLocationSuggestions().get(0).getTitle() + ", " + locationDeatils.getLocationSuggestions().get(0).getCountryName());

                HashMap<String, String> params = new HashMap<>();
                params.put(Consts.ENTITY_ID, "" + entityID);
                params.put(Consts.ENTITY_TYPE, entityType);
                RequestManager.getInstance(this).placeRequest(Consts.LOCATION_DETAILS, AllDetailsOnLocation.class, this, params, false);
            }else{
                Snackbar.make(snackBarLayout, "The location you typed is not found.", Snackbar.LENGTH_LONG).show();
            }
        }

        if(apiMethod.equals(Consts.LOCATION_DETAILS)){
            progressLayout.setVisibility(View.GONE);

            AllDetailsOnLocation details = (AllDetailsOnLocation) response;
            if(details.getBestRatedRestaurant().size() > 0) {
                searchLayout.setVisibility(View.VISIBLE);
                LinearLayoutManager llm = new LinearLayoutManager(this);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                listView.setVisibility(View.VISIBLE);
                listView.setLayoutManager(llm);
                listView.setHasFixedSize(true);
                listView.setItemAnimator(new LandingAnimator());

                restaurantArrayList = details.getBestRatedRestaurant();
                Collections.sort(restaurantArrayList, new RatingComparator());

                copyRestaurantArrList = new ArrayList<>();
                for (BestRatedRestaurant restaurant : restaurantArrayList) {
                    copyRestaurantArrList.add(restaurant);
                }
                currentAddress.setText("Night life index of : " +currentAddress.getText().toString() +" is "+details.getNightlifeIndex());

                adapter = new RestaurantsAdapter(this, copyRestaurantArrList);
                ScaleInAnimationAdapter scaleAdapter = new ScaleInAnimationAdapter(adapter);
                listView.setAdapter(scaleAdapter);
                adapter.notifyDataSetChanged();
                scaleAdapter.notifyDataSetChanged();


            }
        }

    }

    @Override
    public void onErrorResponse(VolleyError error, String apiMethod) {
        progressLayout.setVisibility(View.GONE);
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("LOG", "onConnected Called "+mLastLocation.getLongitude()+" "+mLastLocation.getLatitude());
            getUserLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        if(mGoogleApiClient!=null && mGoogleApiClient.isConnected() ) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {
            Log.d("LOG", "onLocationChanged Called "+location.getLongitude()+" "+location.getLatitude());
            getUserLocation(location.getLatitude(), location.getLongitude());
        }
    }


    /**
     * @param query
     * Method to search for the restaurant name on the query
     */
    private void searchForRestaurantName(String query){
        final List<BestRatedRestaurant> filteredRestaurantList = filter(copyRestaurantArrList, query);
        adapter.updateData(filteredRestaurantList);
        if(filteredRestaurantList.size()>1) {
            adapter.animateTo(filteredRestaurantList);
            listView.scrollToPosition(0);
        }else{
            listView.swapAdapter(adapter, true);
        }
    }

    /**
     * @param restaurants
     * @param query
     * @return List<BestRatedRestaurant>
     * Method to filtet the list with the query
     */
    private List<BestRatedRestaurant> filter(List<BestRatedRestaurant> restaurants, String query) {
        query = query.toLowerCase();

        final List<BestRatedRestaurant> filteredModelList = new ArrayList<>();
        for (BestRatedRestaurant restaurant : restaurants) {
            final String text = restaurant.getRestaurant().getName().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(restaurant);
            }
        }
        return filteredModelList;
    }

    /**
     * @param cntxt
     * @param text
     * @param show
     * Method to show of hide the keyboard
     */
    private void showKeyboard(Context cntxt, EditText text, boolean show) {
        InputMethodManager imm = (InputMethodManager) cntxt
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (show){
            imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);
        }else
            imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
    }



}
