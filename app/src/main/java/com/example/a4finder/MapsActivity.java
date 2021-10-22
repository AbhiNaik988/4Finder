package com.example.a4finder;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.lights.LightState;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.a4finder.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private static final int REQUEST_CODE = 1;
    private GoogleMap mMap;
    private double lat;
    private double lng;
    private Geocoder geocoder;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.a4finder.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


        geocoder = new Geocoder(this);

        binding.fab.setOnClickListener(view -> {
            mMap.clear();
            if(lat == 0.0 && lng == 0.0){
                Toast.makeText(this, "Getting Location From GPS..", Toast.LENGTH_SHORT).show();
            }
            onMapReady(mMap);
        });

        binding.mapTypesButton.setOnClickListener(view -> {
                    //Creating the instance of PopupMenu
                    PopupMenu popup = new PopupMenu(MapsActivity.this, binding.mapTypesButton);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.options_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(item -> {
                        switch (item.getItemId()) {
                            case R.id.normal_map:
                                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                return true;
                            case R.id.hybrid_map:
                                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                return true;
                            case R.id.satellite_map:
                                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                return true;
                            case R.id.terrain_map:
                                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                return true;
                            default:
                                return super.onOptionsItemSelected(item);
                        }
                    });

                    popup.show();
        });

        binding.searchButton.setOnClickListener(view -> {
            if(binding.searchBarEt.getVisibility() == View.GONE){
                binding.searchBarEt.setVisibility(View.VISIBLE);
                binding.mapTypesButton.setVisibility(View.GONE);
            }
            else {
                hideKeyboard(view);
                String text = binding.searchBarEt.getText().toString();
                if(!TextUtils.isEmpty(text)){
                    try {
                        List<Address> addressList = geocoder.getFromLocationName(text, 1);

                        if(!addressList.isEmpty()){
                            LatLng marker = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                            mMap.clear();
                            lat = marker.latitude;
                            lng = marker.longitude;
                            onMapReady(mMap);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                binding.searchBarEt.setText("");
                binding.searchBarEt.setVisibility(View.GONE);
                binding.mapTypesButton.setVisibility(View.VISIBLE);
            }
        });
    }

    public void hideKeyboard(View view){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //Log.d("TAG", "onRequestPermissionsResult: Access Granted!!");
            onMapReady(mMap);
        }
        else{
            Toast.makeText(this, "Permission Required!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng deviceLocation = new LatLng(lat, lng);

        //Log.d("abhi", "onMapReady: "+deviceLocation.toString());
        if(lat == 0.0 && lng == 0.0){
            Toast.makeText(this, "Getting Location From GPS..", Toast.LENGTH_SHORT).show();
        }

        getTitleSetMarker(deviceLocation);

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            getTitleSetMarker(latLng);
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),10));
        });

        mMap.setOnInfoWindowLongClickListener(marker -> {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),mMap.getMaxZoomLevel()-3));
        });
    }

    private void getTitleSetMarker(LatLng latLng) {
        //creating title for marker
        StringBuilder addressBuilder = getStringBuilder(latLng);

        //adding marker and title
        mMap.addMarker(new MarkerOptions().position(latLng).title(addressBuilder.toString()));

        //set camera and zoom level
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
    }

    @NonNull
    private StringBuilder getStringBuilder(LatLng latLng) {
        StringBuilder addressBuilder = new StringBuilder();
        
        if(geocoder != null){
            try {
                List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                if(addressList.size() > 0){
                    if(addressList.get(0).getLocality() != null){
                        addressBuilder.append(addressList.get(0).getLocality()).append(",");
                    }
                    if(addressList.get(0).getSubAdminArea() != null){
                        addressBuilder.append(addressList.get(0).getSubAdminArea()).append(",");
                    }
                    if(addressList.get(0).getAdminArea() != null){
                        addressBuilder.append(addressList.get(0).getAdminArea()).append("-");
                    }
                    if(addressList.get(0).getPostalCode() != null){
                        addressBuilder.append(addressList.get(0).getPostalCode()).append(",");
                    }
                    if(addressList.get(0).getCountryName() != null){
                        addressBuilder.append(addressList.get(0).getCountryName()).append(".");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }   
        }
        
        return addressBuilder;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(TextUtils.isEmpty(location.toString())){
            Toast.makeText(this, "Getting from GPS", Toast.LENGTH_SHORT).show();
        }
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(this, "Status Changed", Toast.LENGTH_SHORT).show();
    }

}