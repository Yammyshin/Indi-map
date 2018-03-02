package com.example.root.inmap5;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback{

    public GoogleMap mGoogleMap;
    MapView mapView;
    View myView;

    FloatingActionButton fab, fabSearch;
    View.OnClickListener snackLister;

    ArrayList<LatLng> listPoints;
    ArrayList<String> markerList;
    ArrayList<LatLng> markerLatLngList;
    List<Marker> markers = new ArrayList<Marker>();

    public GetMarker getMarker;
    public LatLng latLng;

    //use to hold the route that is being displayed so we can clear the old and display the new
    Polyline polyline;
    boolean poly_draw = false;

    String search = "";

    public ProgressDialog progressDialog;
   // public ConnectionClass connectionClass;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.map_layout,container,false);
        return myView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) myView.findViewById(R.id.map_fragment);

        if(mapView != null){
            mapView.onCreate(null);
            mapView.onResume();
            mapView.getMapAsync(this);

            listPoints = new ArrayList<>();
            markerList = new ArrayList<>();
            markerLatLngList = new ArrayList<>();

            getMarker = new GetMarker(getActivity());

            fab = (FloatingActionButton)myView.findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //check if there is already drawn route if true then remove the old and replace the new route
                if (poly_draw == true) {
                    polyline.remove();
                }
                String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
                TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                taskRequestDirections.execute(url);

                //set the variable to true for us to know that there is already a route drawn in the map
                poly_draw = true;
            }
        });
        }

        fabSearch = (FloatingActionButton)myView.findViewById(R.id.fabSearch);
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Input Last Name");

                // Set up the input
                final EditText input = new EditText(getActivity());
                // Specify the type of input expected;
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean found = false;
                        search = input.getText().toString();

                        for(int i=0; i<markers.size(); i++){
                            if(markers.get(i).getTitle().contains(search)){
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLngList.get(i),18.0f));
                                markers.get(i).showInfoWindow();

                                //Add the marker automatically to listpoint so we can create a line if the fab is clicked
                                listPoints.add(1,markers.get(i).getPosition());
                                fab.show();
                                found = true;
                                break;
                            }
                            if(!found){
                                Toast.makeText(getActivity(),"Lastname not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        //Hide the FAB if the user did not click any marker
        fab.hide();
    }

    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getActivity());

        //connectionClass = new ConnectionClass();
        progressDialog=new ProgressDialog(getActivity());

        mGoogleMap = googleMap;

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(fab.getVisibility() != fab.VISIBLE){
                    fab.show();
                }
                //add the markers position to index 1 so so we can create a route from your
                //position going to this marker
                listPoints.add(1, marker.getPosition());
                return false;
            }
        });

//        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//            @Override
//            public void onInfoWindowClick(Marker marker) {
//
//                //Display the FAB because the marker was tapped
//                fab.show();
//
//                //add the markers position to index 1 so so we can create a route from your
//                //position going to this marker
//                listPoints.add(1, marker.getPosition());
//
////                //check if there is already drawn route if true then remove the old and replace the new route
////                if (poly_draw == true) {
////                polyline.remove();
////                }
////                    //add the markers position to index 1 so so we can create a route from your
////                    //position going to this marker
////                    listPoints.add(1, marker.getPosition());
////
////                    String url = getRequestUrl(listPoints.get(0), listPoints.get(1));
////                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
////                    taskRequestDirections.execute(url);
////
////                //set the variable to true for us to know that there is already a route drawn in the map
////                poly_draw = true;
//            }
//        });

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //Disable the default button of google map
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        //Disable the zoom button of google map
        mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
        //Set the max zoom of the map
        mGoogleMap.setMaxZoomPreference(18.0f);
        //Set the minimum zoom of the map
        mGoogleMap.setMinZoomPreference(15.0f);


        getMarker.connectToDB();
        for(int i=0; i<getMarker.lnamePoints.size(); i++){
            latLng = new LatLng(getMarker.latPoints.get(i), getMarker.lonPoints.get(i));

            Marker marker = mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                    .title(getMarker.lnamePoints.get(i)));

            //Add the new marker to the list
            markers.add(marker);
            //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18.0f));

        //Add the name of the markers to the list so we can query this array in our search
        markerList.add(i,getMarker.lnamePoints.get(i));
        //Add the latitude and longitude so we can move the camera if the search is found in this list
        markerLatLngList.add(latLng);
        }

        //Your position in the map
        getMarker.getLocation();
        latLng = new LatLng(getMarker.CURRENT_LATITUDE, getMarker.CURRENT_LONGITUDE);
        mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                .title("You are Here").icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN))).showInfoWindow();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18.0f));

        listPoints.add(0,latLng);

    }

    private void getMyLocation() {

    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude +","+origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude+","+dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org +"&" + str_dest + "&" +sensor+"&" +mode;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return  responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>> > {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(10);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions!=null) {
                //save the polyline so we can easily remove them if
                //a new route should be drawn
                polyline = mGoogleMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getActivity(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }


}
