package com.example.root.inmap5;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {

    EditText username, user_pass;
    Button btn_login;
    ProgressDialog progressDialog;
    ConnectionClass connectionClass;

    String FULLNAME = "";
    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //For fullscreen
//        getSupportActionBar().hide();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        username =(EditText) findViewById(R.id.editTextName);
        user_pass = (EditText) findViewById(R.id.editTextPass);

        connectionClass = new ConnectionClass();
        progressDialog=new ProgressDialog(this);

        btn_login = (Button)findViewById(R.id.button);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dologin dologin=new Dologin();
                dologin.execute();
            }
        });

    }

    private class Dologin extends AsyncTask<String,String,String> {

        String namestr = username.getText().toString();
        String passstr = user_pass.getText().toString();
        String z="";
        boolean isSuccess=false;

        String uname,upass;

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Logging in ...");
            progressDialog.show();
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            if(namestr.trim().equals("")||passstr.trim().equals(""))
                z = "Please enter all fields ....";
            else
            {
                try {
                    Connection con = connectionClass.CONN();
                    if (con == null) {
                        z = "Please check your internet connection";
                    } else {

                        String query=" select * from users_table where user_name='"+namestr+"' and user_pass = '"+passstr+"'";

                        Statement stmt = con.createStatement();
                        // stmt.executeUpdate(query);

                        ResultSet rs=stmt.executeQuery(query);

                        while (rs.next())
                        {
                            id = rs.getInt(1);
                            FULLNAME = rs.getString(2);
                            uname = rs.getString(3);
                            upass = rs.getString(4);

                            if(uname.equals(namestr) && upass.equals(passstr))
                            {
                                isSuccess=true;
                                z = "Login Successful";
                            }
                            else{
                                isSuccess=false;
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    isSuccess = false;
                    z = "Exceptions " +ex;
                }
            }
            return z;
        }

        @Override
        protected void onPostExecute(String s) {

            if(isSuccess) {
                Toast.makeText(getBaseContext(),"" +z, Toast.LENGTH_LONG).show();
                Intent intent=new Intent(MainActivity.this, Main2Activity.class);
                intent.putExtra("fullname",FULLNAME);
                intent.putExtra("user",id);
                startActivity(intent);
            }else{
                if(z.equals("") || z == null)
                    Toast.makeText(getBaseContext(),"Login Failed", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getBaseContext(),""+z, Toast.LENGTH_LONG).show();
            }

            progressDialog.dismiss();
        }
    }
}
