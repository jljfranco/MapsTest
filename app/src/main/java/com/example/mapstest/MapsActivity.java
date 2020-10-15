package com.example.mapstest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SingleChoice.SingleChoiceListener, EmergencyChoice.EmergencyChoiceListener,
                RemoveChoice.RemoveChoiceListener {

    private GoogleMap mMap;
    ArrayList<LatLng> latLngs = new ArrayList<>();
    ArrayList<String> Titles = new ArrayList<>();
    ArrayList<String> Snippets = new ArrayList<>();

    AlertDialog dialog;
    AlertDialog.Builder builder;

    String locSource = "MAPUA University Manila";
    String locDestination = "";
    boolean selected=false;
    boolean emergencySelected = false;
    int personIndex = -1;
    int emergencyPersonIndex = -1;

    boolean addEmergency;

    ArrayList<String> DisabledPerson = new ArrayList<>();
    ArrayList<String> ContactPerson = new ArrayList<>();
    ArrayList<String> PersonalNumber = new ArrayList<>();
    ArrayList<String> EmergencyNumber = new ArrayList<>();
    ArrayList<String> Disability = new ArrayList<>();
    ArrayList<String> Address = new ArrayList<>();
    ArrayList<String> Longitude = new ArrayList<>();
    ArrayList<String> Latitude = new ArrayList<>();
    ArrayList<String> DeviceNumber = new ArrayList<>();

    private static String ip = "192.168.43.237";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "DesDB";
    private static String username = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;

    private TextView tvDisplayChoice;

    private Connection connection = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_MMS) !=
                PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 1000);
        }

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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tvDisplayChoice = findViewById(R.id.tvDisplayChoice);

        Button btnSelectChoice = findViewById(R.id.btnSelectChoice);
        btnSelectChoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmergency = false;
                tvDisplayChoice.setGravity(Gravity.CENTER);
                DialogFragment singleChoiceDialog = new SingleChoice();
                singleChoiceDialog.setCancelable(false);
                singleChoiceDialog.show(getSupportFragmentManager(),"Select Person");
            }
        });

        Button btnAddEmergency = findViewById(R.id.btnAddEmergency);
        btnAddEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addEmergency = true;
                tvDisplayChoice.setGravity(Gravity.CENTER);
                DialogFragment singleChoiceDialog = new SingleChoice();
                singleChoiceDialog.setCancelable(false);
                singleChoiceDialog.show(getSupportFragmentManager(),"Select Person");
            }
        });

        try {
            final Button btnEmergency = findViewById(R.id.btnEmergency);
            btnEmergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tvDisplayChoice.setGravity(Gravity.CENTER);
                    DialogFragment emergencyChoice = new EmergencyChoice();
                    emergencyChoice.setCancelable(false);
                    emergencyChoice.show(getSupportFragmentManager(), "Select Person");
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "No Emergencies",Toast.LENGTH_SHORT).show();
        }

        Button btnDirection = findViewById(R.id.btnDirection);
        btnDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDisplayChoice.setGravity(Gravity.CENTER);
                if(selected == true && personIndex != -1) {
                    locDestination = Address.get(personIndex);
                    DisplayTrack(locSource,locDestination);
                }
                else if (emergencySelected == true && emergencyPersonIndex != -1)
                {
                    DisplayTrack(locSource,locDestination);
                }
                else
                {
                    tvDisplayChoice.setText("Select Person to track using the button on " +
                            "the TOP CENTER");
                }
            }
        });

        try {
            Button btnRemoveEmergency = findViewById(R.id.btnRemoveEmergency);
            btnRemoveEmergency.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment removeChoice = new RemoveChoice();
                    removeChoice.setCancelable(false);
                    removeChoice.show(getSupportFragmentManager(), "Single Choice Dialog");
                    tvDisplayChoice.setGravity(Gravity.CENTER);
                }
            });
        }
        catch (Exception e)
        {
            Toast.makeText(this, "No Emergencies",Toast.LENGTH_SHORT).show();
        }

        Button btnMarkAll = findViewById(R.id.btnTrackAll);
        btnMarkAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                TextView textView = findViewById(R.id.tvDisplayChoice);
                textView.setText("Press Button above to track a Person");
                tvDisplayChoice.setGravity(Gravity.CENTER);
                for (int i = 0; i < latLngs.size(); i++) {
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title(Titles.get(i)).snippet(Snippets.get(i)));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16),5000,null);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngs.get(i)));
                }
            }
        });

        Button btnMarkAllEmergencies = findViewById(R.id.btnMarkEmergencies);
        btnMarkAllEmergencies.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.clear();
                TextView textView = findViewById(R.id.tvDisplayChoice);
                textView.setText("Activated Emergencies are Marked");
                tvDisplayChoice.setGravity(Gravity.CENTER);

                ArrayList<LatLng> ELatLng = new ArrayList<>();
                ArrayList<String> ELat = new ArrayList<>();
                ArrayList<String> ELng = new ArrayList<>();
                ArrayList<String> tmpLatLng = new ArrayList<>();
                ArrayList<String> disabledPerson = new ArrayList<>();
                ArrayList<String> contact = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                ArrayList<String> snippets = new ArrayList<>();

                LatLng tempLatLng;

                try {
                    PreparedStatement sqlProc = connection.prepareStatement("EXEC spGetAllEmergencies");
                    ResultSet rs = sqlProc.executeQuery();

                    while (rs.next())
                    {
                        disabledPerson.add(rs.getString("Disabled Person"));
                        contact.add(rs.getString("Personal Number"));
                        ELng.add(rs.getString("Longitude"));
                        ELat.add(rs.getString("Latitude"));
                    }
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }

                for (int i = 0; i < ELat.size(); i++)
                {
                    titles.add(disabledPerson.get(i));
                    snippets.add("Contact: " + contact.get(i));
                    tempLatLng = new LatLng(parseDouble(ELng.get(i)),parseDouble(ELat.get(i)));
                    ELatLng.add(tempLatLng);
                }

                for (int i = 0; i < ELatLng.size(); i++) {
                    mMap.getUiSettings().setZoomControlsEnabled(true);
                    mMap.addMarker(new MarkerOptions().position(ELatLng.get(i)).title(titles.get(i)).snippet(snippets.get(i)));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16),5000,null);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(ELatLng.get(i)));
                }
            }
        });

        ImageButton btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvDisplayChoice.setMovementMethod(new ScrollingMovementMethod());
                tvDisplayChoice.setText("1. Press \"Select Person to Track\" Button to Select and  View a Person's information\n\n"+
                        "2. Press \"Directions\" Button to set directions to the selected person using Google Maps.\n\n"+
                        "3. Press \"Mark All\" Button to mark all registered device users.\n\n"+
                        "4. Press \"Emergency List\" to view the device owners with emergency status.\n\n"+
                        "5. Press \"Add Person To Emergency\" to manually add a person to emergency list.\n\n"+
                        "6. Press \"Remove Emergencies\" to remove and deactivate each emergency status.\n\n"+
                        "7. Press \"Mark All Emergencies\" to Mark device owners with activated emergency status.");
                tvDisplayChoice.setGravity(Gravity.LEFT);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (connection!=null) {
            try {
                PreparedStatement sqlProc = connection.prepareStatement("EXEC spGetMarkers");
                ResultSet rs =sqlProc.executeQuery();

                while (rs.next())
                {
                    DisabledPerson.add(rs.getString("Disabled Person"));
                    ContactPerson.add(rs.getString("Contact Person"));
                    PersonalNumber.add(rs.getString("Personal Number"));
                    EmergencyNumber.add(rs.getString("Emergency Number"));
                    Disability.add(rs.getString("Disability"));
                    Address.add(rs.getString("Address"));
                    Longitude.add(rs.getString("Longitude"));
                    Latitude.add(rs.getString("Latitude"));
                    DeviceNumber.add(rs.getString("Device Number"));
                }

                LatLng tempLatLng;

                for (int i = 0; i < DisabledPerson.size(); i++)
                {
                    Titles.add(DisabledPerson.get(i));
                    Snippets.add("Disability: " + Disability.get(i));
                    tempLatLng = new LatLng(parseDouble(Longitude.get(i)),parseDouble(Latitude.get(i)));
                    latLngs.add(tempLatLng);
                }


            } catch (SQLException e) {
                e.printStackTrace();
            }

            for (int i = 0; i < latLngs.size(); i++) {
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.addMarker(new MarkerOptions().position(latLngs.get(i)).title(Titles.get(i)).snippet(Snippets.get(i)));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16),5000,null);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngs.get(i)));
            }
        }
    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position, String[] contactPerson, String[] personalNumber, String[] emergencyNumber,
                                        String[] disability, String[] address, String[] longitude, String[] latitude) {
        if(!addEmergency) {
            selected = true;
            emergencySelected = false;
            personIndex = position;
            mMap.clear();

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.addMarker(new MarkerOptions().position(latLngs.get(position)).title(Titles.get(position)).snippet(Snippets.get(position)));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 5000, null);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLngs.get(position)));

            tvDisplayChoice.setMovementMethod(new ScrollingMovementMethod());
            tvDisplayChoice.setText("Selected Person: " + list[position] + "\n" + "\n" +
                    "Personal Number: " + personalNumber[position] + "\n" + "\n" +
                    "Disability: " + disability[position] + "\n" + "\n" +
                    "Address: " + address[position] + "\n" + "\n" +
                    "Contact Person: " + contactPerson[position] + "\n" + "\n" +
                    "Contact Person Number: " + emergencyNumber[position]);
        }
        else
        {
            try {
                PreparedStatement sqlProc = connection.prepareStatement("EXEC spAddEmergency @DeviceNumber=\'"+DeviceNumber.get(position)+"\'");
                tvDisplayChoice.setText("Person Added to Emergency List.");
                sqlProc.executeQuery();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNegativeButtonClicked() {
        mMap.clear();
        tvDisplayChoice.setText("Press Button above to track a Person");
        selected = false;
        personIndex = -1;
    }

    @Override
    public void onPositiveButtonClicked1(String[] disabledPerson, int position, String[] contactPerson,
                                        String[] personalNumber, String[] emergencyNumber, String[] address, String[] longitude, String[] latitude, int size) {
        try {

            emergencySelected = true;
            selected = false;
            emergencyPersonIndex = position;
            mMap.clear();
            LatLng tempLatLng = new LatLng(parseDouble(longitude[position]), parseDouble(latitude[position]));

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.addMarker(new MarkerOptions().position(tempLatLng).title(disabledPerson[position]).snippet("Contact:"+personalNumber[position]));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 5000, null);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(tempLatLng));

            locDestination = address[position];

            tvDisplayChoice.setMovementMethod(new ScrollingMovementMethod());
            tvDisplayChoice.setText("Selected Person: " + disabledPerson[position] + "\n" + "\n" +
                    "Personal Number: " + personalNumber[position] + "\n" + "\n" +
                    "Address: " + address[position] + "\n" + "\n" +
                    "Contact Person: " + contactPerson[position] + "\n" + "\n" +
                    "Contact Person Number: " + emergencyNumber[position]);
        } catch (Exception e) {
            Toast.makeText(this, "No Emergencies", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onNegativeButtonClicked1() {
        mMap.clear();
        tvDisplayChoice.setText("No Emergency Selected");
        selected = false;
        personIndex = -1;
    }

    private void DisplayTrack(String sSource, String sDestination) {
        try {
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/"+sSource+"/"+sDestination);
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            intent.setPackage("com.google.android.apps.maps");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 1000)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Read SMS Permission Granted",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(this,"Permission Not Granted",Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }

    @Override
    public void onPositiveButtonClicked2(String[] disabledPerson, int position) {
        try {
            try {
                PreparedStatement sqlProc = connection.prepareStatement("EXEC spRemoveEmergency @Full_Name=\'" + disabledPerson[position] + "\'");
                Toast.makeText(this, "Emergency Removed",Toast.LENGTH_SHORT).show();
                mMap.clear();
                tvDisplayChoice.setText("Emergency Removed");
                sqlProc.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            Toast.makeText(this, "No Emergencies", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onNegativeButtonClicked2() {
        Toast.makeText(this, "No Emergency Removed",Toast.LENGTH_SHORT).show();
    }
}