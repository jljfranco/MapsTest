package com.example.mapstest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private TextView inputUsername;
    private TextView inputPassword;

    private static String ip = "192.168.43.237";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "DesDB";
    private static String username = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;
    private Connection connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SQL connection PERMISSION
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //SQL connection
        try {
            connection = DriverManager.getConnection(url,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        inputUsername = findViewById(R.id.inputUsername);
        inputPassword = findViewById(R.id.inputPassword);

        button = findViewById(R.id.btnLogin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMaps();
            }
        });
    }

    public void openMaps()
    {
        boolean isLogged = false;
        try {
            PreparedStatement sqlProc = connection.prepareStatement("EXEC spLogin @Username=\'"+inputUsername.getText()+"\', @Password=\'"+inputPassword.getText()+"\'");
            ResultSet rs = sqlProc.executeQuery();

            while (rs.next())
            {
                isLogged = Boolean.parseBoolean(rs.getString("Result"));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        if(isLogged) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);
        }
        else
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
    }
}