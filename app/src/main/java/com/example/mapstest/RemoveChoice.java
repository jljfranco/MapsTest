package com.example.mapstest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RemoveChoice extends DialogFragment {
    private static String ip = "192.168.43.237";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "DesDB";
    private static String username = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;

    private Connection connection = null;

    int position1 = 0;

    public interface RemoveChoiceListener {
        void onPositiveButtonClicked2(String[] disabledPerson, int position);
        void onNegativeButtonClicked2();
    }

    RemoveChoiceListener rListener;

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        try {
            rListener = (RemoveChoice.RemoveChoiceListener) context;
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
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<String> DisabledPerson = new ArrayList<>();

        if (connection != null) {

            try {
                PreparedStatement sqlProc = connection.prepareStatement("EXEC spGetAllEmergencies");
                ResultSet rs = sqlProc.executeQuery();

                while (rs.next()) {
                    DisabledPerson.add(rs.getString("Disabled Person"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        final String[] disabledPerson = DisabledPerson.toArray(new String[0]);

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
                        rListener.onPositiveButtonClicked2(disabledPerson,position1);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        rListener.onNegativeButtonClicked2();
                    }
                });

        return builder.create();
    }
}
