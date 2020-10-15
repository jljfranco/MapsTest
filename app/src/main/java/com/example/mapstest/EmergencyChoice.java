package com.example.mapstest;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.lang.Double.parseDouble;

public class EmergencyChoice extends DialogFragment {

    private static String ip = "192.168.43.237";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "DesDB";
    private static String username = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;

    private Connection connection = null;

    int position1 = 0;
    boolean noEmergency;


    public interface EmergencyChoiceListener {
        void onPositiveButtonClicked1(String[] disabledPerson, int position, String[] contactPerson, String[] personalNumber, String[] emergencyPerson,
                                      String[] address, String[] longitude, String[] latitude, int size);
        void onNegativeButtonClicked1();
    }


    EmergencyChoiceListener eListener;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            eListener = (EmergencyChoice.EmergencyChoiceListener) context;
        }
        catch (Exception e)
        {
            throw new ClassCastException(getActivity().toString()+"SingleChoiceLister Must be implemented");

        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            connection = DriverManager.getConnection(url,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<String> DisabledPerson = new ArrayList<>();
        ArrayList<String> ContactPerson = new ArrayList<>();
        ArrayList<String> PersonalNumber = new ArrayList<>();
        ArrayList<String> EmergencyNumber = new ArrayList<>();
        ArrayList<String> Address = new ArrayList<>();
        ArrayList<String> Longitude = new ArrayList<>();
        ArrayList<String> Latitude = new ArrayList<>();

        if (connection!=null) {

            try {
                PreparedStatement sqlProc = connection.prepareStatement("EXEC spGetAllEmergencies");
                ResultSet rs = sqlProc.executeQuery();

                while (rs.next())
                {
                    DisabledPerson.add(rs.getString("Disabled Person"));
                    PersonalNumber.add(rs.getString("Personal Number"));
                    ContactPerson.add(rs.getString("Emergency Person"));
                    EmergencyNumber.add(rs.getString("Emergency Number"));
                    Address.add(rs.getString("Address"));
                    Longitude.add(rs.getString("Longitude"));
                    Latitude.add(rs.getString("Latitude"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        final String[] disabledPerson = DisabledPerson.toArray(new String[0]);
        final String[] contactPerson = ContactPerson.toArray(new String[0]);
        final String[] personalNumber = PersonalNumber.toArray(new String[0]);
        final String[] emergencyNumber = EmergencyNumber.toArray(new String[0]);
        final String[] address = Address.toArray(new String[0]);
        final String[] longitude = Longitude.toArray(new String[0]);
        final String[] latitude = Latitude.toArray(new String[0]);
        final int size = DisabledPerson.size();

        if (size>0)
            setNoEmergency(true);
        else
            setNoEmergency(false);

        final boolean tstEmergency = isNoEmergency();

        builder.setTitle("Select Emergency")
                .setSingleChoiceItems(disabledPerson, position1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        position1 = i;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        eListener.onPositiveButtonClicked1(disabledPerson,position1, contactPerson, personalNumber, emergencyNumber, address,
                                longitude, latitude, size);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        eListener.onNegativeButtonClicked1();
                    }
                });

        return builder.create();
    }


    public boolean isNoEmergency() {
        return noEmergency;
    }

    public void setNoEmergency(boolean noEmergency) {
        this.noEmergency = noEmergency;
    }
}
