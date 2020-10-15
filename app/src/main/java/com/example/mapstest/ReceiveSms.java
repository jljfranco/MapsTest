package com.example.mapstest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.SmsMessage;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mapstest.databinding.ActivityMainBinding;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.text.DateFormat;

public class ReceiveSms extends BroadcastReceiver {

    private static String ip = "192.168.43.237";
    private static String port = "1433";
    private static String Classes = "net.sourceforge.jtds.jdbc.Driver";
    private static String database = "DesDB";
    private static String username = "test";
    private static String password = "test";
    private static String url = "jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;
    private Connection connection = null;

    @Override
    public void onReceive(Context context, Intent intent) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            connection = DriverManager.getConnection(url,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            String msg_from;
            if(bundle != null)
            {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    msgs = new SmsMessage[pdus.length];

                    msgs[0]=SmsMessage.createFromPdu((byte[]) pdus[0]);
                    msg_from = msgs[0].getOriginatingAddress();
                    String msgBody = msgs[0].getMessageBody();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss a");
                    Calendar calendar = Calendar.getInstance();
                    String dateTime = simpleDateFormat.format(calendar.getTime());
                    if (connection!=null) {

                        try {
                            PreparedStatement sqlProc = connection.prepareStatement("EXEC spAddEmergency @DeviceNumber=\'"+msg_from+"\'");
                            sqlProc.executeQuery();

                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                        Toast.makeText(context, "From: " + msg_from + ", Body: " +
                                msgBody +"\n"+dateTime, Toast.LENGTH_SHORT).show();

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

        }
    }
}
