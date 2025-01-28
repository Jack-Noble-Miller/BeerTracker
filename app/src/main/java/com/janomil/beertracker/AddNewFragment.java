package com.janomil.beertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.w3c.dom.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AddNewFragment - Allows a user to add an entry to the UserBeerLink table of the database
 */
public class AddNewFragment extends Fragment {

    EditText beerName;
    EditText beerQuantity;
    SharedPreferences sp;
    FrameLayout frameLayout;
    String receivedName;
    String receivedType;




    public AddNewFragment() {
        // Required empty public constructor
    }

    public static AddNewFragment newInstance(String param1, String param2) {
        AddNewFragment fragment = new AddNewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            receivedName = getArguments().getString("BeerName");
            receivedType = getArguments().getString("BeerType");
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_new, container, false);

        MaterialButton button = view.findViewById(R.id.button2);
        button.setOnClickListener(v -> onSubmitButtonClick(v));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ImageButton beerListButton = (ImageButton) getView().findViewById(R.id.button);
        beerListButton.setOnClickListener(this::onClickBeerList);
        TextView measureTextView = (TextView)getView().findViewById(R.id.drinkSizeLabel2);
        TextView wineSizeGuide = (TextView)getView().findViewById(R.id.wineSizeGuide);

        beerName = (EditText) getView().findViewById(R.id.DrinkNameInputBox);
        if(receivedName != null && receivedType != null){
            beerName.setText(receivedName);
            switch (receivedType){
                case "Spirit":
                    measureTextView.setText("x 25ml");
                    break;
                case "Beer":
                    measureTextView.setText("x Pint");
                    break;
                case "Cider":
                    measureTextView.setText("x Pint");
                    break;
                case "Cocktail":
                    measureTextView.setText("x Pint");
                    break;
                case "Wine":
                    measureTextView.setText("x 125ml");
                    wineSizeGuide.setText("Wine Size Guide: \nBottle: 6 \nLarge: 2 \nMedium: 1.4 \nSmall:1");
                    break;
                default:
                    measureTextView.setText("x Measure");
                    break;
            }
        }

        beerQuantity = (EditText) getView().findViewById((R.id.DrinkSizeInputBox));
    }

    public void onSubmitButtonClick(View v) {
        ((MainActivity) requireActivity()).startLoad();
        MainActivity mainActivity = (MainActivity) getActivity();
        String beerNameStr = String.valueOf(beerName.getText());
        String beerQuantityStr = String.valueOf(beerQuantity.getText());
        double beerQuantityDou;

        if (beerQuantityStr.isEmpty()) {
            beerQuantityDou = 0;
        } else {
            beerQuantityDou = Double.parseDouble(beerQuantityStr);
        }

        if (!beerNameStr.isEmpty()) {
            if (beerQuantityDou > 0 && beerQuantityDou % 0.5 == 0) {
                new Thread(() -> {
                    try (Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)) {
                        sp = getActivity().getSharedPreferences("userID", Context.MODE_PRIVATE);

                        // Check if the beer exists
                        String checkBeerQuery = "SELECT BeerID FROM BeerData WHERE UPPER(BeerName) LIKE UPPER(?)";
                        PreparedStatement checkStmt = con.prepareStatement(checkBeerQuery);
                        checkStmt.setString(1, beerNameStr);
                        ResultSet rs = checkStmt.executeQuery();

                        if (rs.next()) {
                            // Beer exists, proceed with insertion
                            int userID =((MainActivity) getActivity()).getActingUserID();
                            int beerID = rs.getInt("BeerID");
                            String insertQuery = "INSERT INTO UserBeerLink(UserID, BeerID, Timestamp, DrinkSizeMultiplier) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
                            PreparedStatement insertStmt = con.prepareStatement(insertQuery);
                            insertStmt.setInt(1, userID);
                            insertStmt.setInt(2, beerID);
                            insertStmt.setDouble(3, beerQuantityDou);
                            insertStmt.execute();

                            // Clear input fields on the UI thread
                            getActivity().runOnUiThread(() -> {
                                beerQuantity.setText("");
                                beerName.setText("");
                            });

                            if (mainActivity != null) {
                                mainActivity.showToast("Successfully added beer to DB");
                            }
                        } else {
                            // Beer does not exist, show error
                            getActivity().runOnUiThread(() -> {
                                if (mainActivity != null) {
                                    mainActivity.showToast("Drink not found in the database, add it first!");
                                }
                            });
                        }

                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage());
                        if (mainActivity != null) {
                            mainActivity.showToast("Error occurred while adding drink to DB");
                        }
                    }
                }).start();
            } else {
                if (mainActivity != null) {
                    mainActivity.showToast("Invalid size");
                }
            }
        } else {
            if (mainActivity != null) {
                mainActivity.showToast("Drink name cannot be empty");
            }
        }
        ((MainActivity) requireActivity()).endLoad();
    }

    public void onClickBeerList(View view){
        frameLayout = (FrameLayout) getView().findViewById(R.id.frameLayout);

        getFragmentManager().beginTransaction().replace(R.id.frameLayout, new BeerListFragment())
                .addToBackStack(null)
                .commit();
    }
}