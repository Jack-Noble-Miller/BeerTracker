package com.janomil.beertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.tabs.TabLayout;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MainActivity - Tab switching and user authentication is managed here
 */

public class MainActivity extends AppCompatActivity {
    boolean isDebugMode = true;
    boolean isAuthenticated = false;
    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;
    SharedPreferences sp;
    FrameLayout frameLayout;
    TabLayout tabLayout;
    TextView debugUserIDTV;
    public AtomicInteger userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DB_URL = getApplicationContext().getString(R.string.DB_URL);
        DB_USER = getApplicationContext().getString(R.string.DB_USERNAME);
        DB_PASSWORD = getApplicationContext().getString(R.string.DB_PASSWORD);
        debugUserIDTV = (TextView)findViewById(R.id.textView1);
        sp = getSharedPreferences("userID", Context.MODE_PRIVATE);
        authDeviceToDB();

        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        //tv1 = (TextView)findViewById(R.id.textView);

        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new OverviewFragment())
                .addToBackStack(null)
                .commit();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                Fragment fragment = null;
                int pos = tab.getPosition();
                //tv1.setText(String.valueOf(pos));
                switch (pos) {
                    case 0:
                        fragment = new OverviewFragment();
                        break;
                    case 1:
                        fragment = new AddNewFragment();
                        break;
                    case 2:
                        fragment = new NewBeerFragment();
                        break;
                    case 3:
                        fragment = new HistoryFragment();
                        break;


                }
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }


            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                onTabSelected(tab);
            }
        });
    }

    private void authDeviceToDB() {
        // Check if the device already has an entry in the DB, otherwise create one.
        userID = new AtomicInteger(sp.getInt("userID", 0));

        new Thread(() -> {
            try {
                if (userID.get() == 0) {
                    userID.set(addDeviceToDB());
                    saveUserIDToPreferences(userID.get());
                }

                try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    String queryStr = "SELECT * FROM UserData WHERE UserID = ? AND UserDeviceName = ?";
                    try (PreparedStatement stmt = con.prepareStatement(queryStr)) {
                        stmt.setInt(1, userID.get());
                        stmt.setString(2, getDeviceName());

                        try (ResultSet rs = stmt.executeQuery()) {
                            if (!rs.next()) {
                                userID.set(addDeviceToDB());
                                saveUserIDToPreferences(userID.get());
                            } else {
                                isAuthenticated = true;
                                showToast("Successfully authenticated with UserID: " + userID.get());
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                Log.e("ERROR", "SQL Exception: " + e.getMessage(), e);
            } catch (Exception e) {
                Log.e("ERROR", "Unexpected Exception: " + e.getMessage(), e);
            } finally {
                updateDebugTextView(userID.get());
            }
        }).start();
    }

    private void saveUserIDToPreferences(int userID) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("userID", userID);
        editor.apply();
    }

    public void showToast(String message) {
        new Handler(getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show()
        );
    }

    private void updateDebugTextView(int userID) {
        new Handler(getMainLooper()).post(() ->
                debugUserIDTV.setText(String.valueOf(userID))
        );
    }



    private int addDeviceToDB() {
        int userID = 0;

        try {
            // Ensure the database driver is loaded
            Class.forName("com.mysql.jdbc.Driver");

            // Establish a connection
            try (Connection con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String stmtStr = "INSERT INTO UserData(UserIsAdmin, UserDeviceName) VALUES (?, ?)";

                // Prepare the statement with auto-generated keys retrieval
                try (PreparedStatement stmt = con.prepareStatement(stmtStr, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setBoolean(1, false);
                    stmt.setString(2, getDeviceName());

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        // Retrieve the generated user ID
                        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                userID = generatedKeys.getInt(1);

                                // Save the user ID to shared preferences
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putInt("userID", userID);
                                editor.apply();
                            }
                        }
                    } else {
                        Log.e("ERROR", "Insert failed: No rows affected.");
                    }
                }
            }
        } catch (SQLException e) {
            Log.e("ERROR", "SQL Exception: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            Log.e("ERROR", "Class Not Found: " + e.getMessage(), e);
        } catch (Exception e) {
            Log.e("ERROR", "Unexpected Exception: " + e.getMessage(), e);
        }

        return userID;
    }


    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public int getUserID(){
        return userID.get();
    }


}