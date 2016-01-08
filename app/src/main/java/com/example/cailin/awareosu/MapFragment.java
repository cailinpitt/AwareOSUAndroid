package com.example.cailin.awareosu;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * A fragment that launches other parts of the demo application.
 */
public class MapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    public double[] locations;
    public String[] crimeInfo;
    public String[] offCampusCrimes;
    public String[] onCampusCrimes;
    public String[] offCampusCrimeLinks;
    public int offCrimeNum;
    public int onCrimeNum;
    public String date;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        locations = args.getDoubleArray("locations");
        crimeInfo = args.getStringArray("info");
        // Retrieve locations & crime info from main activity

        double latitude;
        double longitude;
        // Initialize variables

        // inflate and return the layout
        View v = inflater.inflate(R.layout.activity_map_fragment, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();

       // currentLocationIcon = BitmapDescriptorFactory.fromResource(R.drawable.map_icon);

        int j = 0;
        for (int i = 0; i < locations.length; i += 2) {
            // latitude and longitude
            latitude = locations[i];
            longitude = locations[i + 1];

            if (Double.compare(locations[i], -1.0) != 0)
            {
                // This means valid lat & long were found for address

                // create marker
                MarkerOptions marker = new MarkerOptions().position(
                        new LatLng(latitude, longitude)).title(crimeInfo[j]);

                BitmapDescriptorFactory.fromResource(R.drawable.map_icon);
                // Initialize BitmapDescriptorFactory

                // Changing marker icon
                marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

                // adding marker
                googleMap.addMarker(marker);
            }
            j++;
        }


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(40.006329, -83.016880)).zoom(12).build();
        // Zoom into campus area

        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        // Perform any camera updates here

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}