package com.example.root.inmap5;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class GetMarker implements LocationListener{

    public ConnectionClass connectionClass;
    public ArrayList<String> lnamePoints;
    public ArrayList<Double> latPoints;
    public ArrayList<Double> lonPoints;

    LocationManager locationManager;

    public static double CURRENT_LATITUDE;
    public static double CURRENT_LONGITUDE;

    Context mContext;
    public GetMarker(Context mContext){
        this.mContext = mContext;
    }

    public String connectToDB(){

        connectionClass = new ConnectionClass();

        lnamePoints = new ArrayList<>();
        latPoints = new ArrayList<>();
        lonPoints = new ArrayList<>();

        String z = "";
        boolean isSuccess=false;
        try {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Please check your internet connection";
            } else {

                String query=" select * from map_info";

                Statement stmt = con.createStatement();

                ResultSet rs=stmt.executeQuery(query);

                while (rs.next())
                {
                    lnamePoints.add(rs.getString(4));
                    latPoints.add(rs.getDouble(5));
                    lonPoints.add(rs.getDouble(6));
                }
                if(lnamePoints.isEmpty())
                    isSuccess = false;
                else
                    isSuccess = true;

            }
        }catch (Exception ex){
            isSuccess = false;
            z = "Exceptions"+ex;
        }


        if(isSuccess) {

        }
        return z;
    }

    @Override
    public void onLocationChanged(Location location) {
        CURRENT_LATITUDE = location.getLatitude();
        CURRENT_LONGITUDE = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public  void getLocation() {
        try {
            locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    //Location is off the map so make sure it is accurate
    //Don't display the map until the current location is acquired by the application
}
