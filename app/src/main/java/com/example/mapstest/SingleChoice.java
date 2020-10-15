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

public class SingleChoice extends DialogFragment {

    private static String ip = "192.168.43.237";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "DesDB";
    private static String username = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;

    private Connection connection = null;

    int position = 0;

    public interface  SingleChoiceListener {
        void onPositiveButtonClicked(String[] list, int position, String[] contactPerson, String[] personalNumber, String[] emergencyPerson,
                                     String[] disability, String[] address, String[] longitude, String[] latitude);
        void onNegativeButtonClicked();
    }

    SingleChoiceListener mListener;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            mListener = (SingleChoiceListener) context;
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
        ArrayList<String> Disability = new ArrayList<>();
        ArrayList<String> Address = new ArrayList<>();
        ArrayList<String> Longitude = new ArrayList<>();
        ArrayList<String> Latitude = new ArrayList<>();

        if (connection!=null) {

            try {
                PreparedStatement sqlProc = connection.prepareStatement("EXEC spGetMarkers");
                ResultSet rs = sqlProc.executeQuery();

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
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        final String[] list = DisabledPerson.toArray(new String[0]);
        final String[] contactPerson = ContactPerson.toArray(new String[0]);
        final String[] personalNumber = PersonalNumber.toArray(new String[0]);
        final String[] emergencyNumber = EmergencyNumber.toArray(new String[0]);
        final String[] disability = Disability.toArray(new String[0]);
        final String[] address = Address.toArray(new String[0]);
        final String[] longitude = Longitude.toArray(new String[0]);
        final String[] latitude = Latitude.toArray(new String[0]);

        builder.setTitle("Select Choice")
                .setSingleChoiceItems(list, position, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        position = i;
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onPositiveButtonClicked(list,position, contactPerson, personalNumber, emergencyNumber, disability, address,
                                longitude, latitude);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mListener.onNegativeButtonClicked();
                    }
                });

        return builder.create();
    }
}
