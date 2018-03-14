package com.example.root.inmap5;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class GetMarker implements LocationListener{

    public ConnectionClass connectionClass;
    public ArrayList<Integer> info_id;
    public ArrayList<String> fnamePoints;
    public ArrayList<String> lnamePoints;
    public ArrayList<Double> latPoints;
    public ArrayList<Double> lonPoints;
    public  ArrayList<Integer> user;
    public ArrayList<String> pinDate;
    public ArrayList<String> users_Name;
    public ArrayList<Integer> users_ID;


    LocationManager locationManager;

    public static double CURRENT_LATITUDE;
    public static double CURRENT_LONGITUDE;

    Context mContext;
    public GetMarker(Context mContext){
        this.mContext = mContext;
    }

    public String connectToDB(){

        connectionClass = new ConnectionClass();
        info_id = new ArrayList<>();
        fnamePoints = new ArrayList<>();
        lnamePoints = new ArrayList<>();
        latPoints = new ArrayList<>();
        lonPoints = new ArrayList<>();
        user = new ArrayList<>();
        pinDate = new ArrayList<>();

        String z = "";
        boolean isSuccess=false;
        try {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Please check your internet connection";
            } else {

                String query=" select * from map_info where relocated != 'YES'";

                Statement stmt = con.createStatement();

                ResultSet rs=stmt.executeQuery(query);

                while (rs.next())
                {
                    info_id.add(rs.getInt(1));
                    fnamePoints.add(rs.getString(2));
                    lnamePoints.add(rs.getString(4));
                    latPoints.add(rs.getDouble(5));
                    lonPoints.add(rs.getDouble(6));
                    user.add(rs.getInt(7));
                    pinDate.add(rs.getString(8));
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
            getUsersName();
        }
        return z;
    }

    private void getUsersName(){
        connectionClass = new ConnectionClass();
        users_Name = new ArrayList<>();
        users_ID = new ArrayList<>();

        String z = "";
        boolean isSuccess=false;
        try {
            Connection con = connectionClass.CONN();
            if (con == null) {
                z = "Cannot get users info";
            } else {

                String query=" select * from users_table";

                Statement stmt = con.createStatement();

                ResultSet rs=stmt.executeQuery(query);

                while (rs.next())
                {
                    users_ID.add(rs.getInt(1));
                   users_Name.add(rs.getString(2));
                }

            }
        }catch (Exception ex){
            isSuccess = false;
            z = "Exceptions"+ex;
        }

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

    public boolean relocated = false;
    public String relocateMarker(Integer id){

        String z = "";
       relocated = false;
        try {
            Connection con = connectionClass.CONN();

            if (con == null) {
                z = "Please check your internet connection";
            } else {
                String query="update map_info SET relocated='YES' where info_id="+id+"";
                Statement stmt = con.createStatement();
                stmt.executeUpdate(query);
                con.close();
                z = "Information Updated";
                relocated=true;
            }
        }
        catch (Exception ex)
        {
           relocated = false;
            z = "Exceptions"+ex;
        }
        return  z;
    }

}


