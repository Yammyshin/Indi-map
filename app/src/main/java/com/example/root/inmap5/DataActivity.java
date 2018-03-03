package com.example.root.inmap5;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.Statement;

public class DataActivity extends AppCompatActivity implements LocationListener{

    Button save_button;
    EditText lat_text, lon_text, fname_text, mname_text, lname_text;
    ProgressDialog progressDialog;
    ConnectionClass connectionClass;
    LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        save_button = (Button) findViewById(R.id.btnLocate);
        lat_text = (EditText) findViewById(R.id.txt_lat);
        lon_text = (EditText) findViewById(R.id.txt_lon);
        fname_text = (EditText) findViewById(R.id.fname);
        mname_text = (EditText) findViewById(R.id.mname);
        lname_text = (EditText) findViewById(R.id.lname);

        connectionClass = new ConnectionClass();
        progressDialog=new ProgressDialog(this);

        //higher versions of Android need in app permission so we will
        // request in app permission this way
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

        //check if the location service is enabled
        //if false display the message and finish the activity to return to the main activity
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //All location services are disabled
            Toast.makeText(getApplicationContext(),"Please enable the location service ", Toast.LENGTH_SHORT).show();
            finish();
        }

        //request for location updates get the latitude and longitude of the device
        getLocation();

        save_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveMapInfo saveMapInfo = new SaveMapInfo();
                saveMapInfo.execute("");
            }
        });
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 5, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        lat_text.setText("" + location.getLatitude());
        lon_text.setText("" + location.getLongitude());

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

    public class SaveMapInfo extends AsyncTask<String,String,String>
    {
        String fnamestr=fname_text.getText().toString();
        String mnamestr=mname_text.getText().toString();
        String lnamestr=lname_text.getText().toString();
        String lat = lat_text.getText().toString();
        String lon = lon_text.getText().toString();

        String z="";
        boolean isSuccess=false;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Saving ...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            if(fnamestr.trim().equals("")|| mnamestr.trim().equals("") ||lnamestr.trim().equals(""))
                z = "Please enter all fields....";
            else
            {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {
                        String query="insert into map_info(fname, mname, lname, lat, lon)values('"+fnamestr+"','"+mnamestr+"','"+lnamestr+"','"+lat+"','"+lon+"')";

                        Statement stmt = con.createStatement();
                        stmt.executeUpdate(query);
                        con.close();
                        z = "Location Saved";
                        isSuccess=true;
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions"+ex;
                }
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {

            Toast.makeText(getBaseContext(),""+z,Toast.LENGTH_LONG).show();

            if(isSuccess) {
               finish();
            }
            progressDialog.dismiss();
        }
    }
}
