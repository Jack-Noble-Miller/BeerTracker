package com.janomil.beertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.material.button.MaterialButton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * NewBeerFragment - Allows a user to create a new beer
 */
public class NewBeerFragment extends Fragment {

    Spinner dropdown;
    EditText beerName;
    EditText beerHex;
    SharedPreferences sp;

    public NewBeerFragment() {
        // Required empty public constructor
    }

    public static NewBeerFragment newInstance(String param1, String param2) {
        NewBeerFragment fragment = new NewBeerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_beer, container, false);

        MaterialButton button = view.findViewById(R.id.button1);
        button.setOnClickListener(v -> onSubmitButtonClick(v));
        return view;
    }

    private void onSubmitButtonClick(View v) {
        MainActivity mainActivity = (MainActivity) getActivity();
        String beerNameStr = String.valueOf(beerName.getText());
        String beerHexStr = String.valueOf(beerHex.getText());
        String beerTypeStr = dropdown.getSelectedItem().toString();

        if(!beerNameStr.isEmpty()){
            if(!beerHexStr.isEmpty() && beerHexStr.matches("^#([A-Fa-f0-9]{6})$")){
                new Thread(()->{
                    try(Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)){
                        sp = getActivity().getSharedPreferences("userID", Context.MODE_PRIVATE);
                        String stmtStr = "INSERT INTO BeerData(BeerName, BeerColour, BeerType) VALUES (?, ?, ?)";
                        PreparedStatement stmt = con.prepareStatement(stmtStr);
                        stmt.setString(1, beerNameStr);
                        stmt.setString(2,beerHexStr);
                        stmt.setString(3, beerTypeStr);
                        stmt.execute();
                        getActivity().runOnUiThread(() -> {
                            beerHex.setText("");
                            beerName.setText("");
                        });
                        if(mainActivity != null){
                            mainActivity.showToast("Successfully added drink to DB");
                        }

                    } catch (Exception e) {
                        Log.e("ERROR", e.getMessage());
                        if(mainActivity != null) {
                            mainActivity.showToast("Error occured adding drink to DB");
                        }
                    }
                }).start();
            }
            else{

                if(mainActivity != null){
                    mainActivity.showToast("Invalid size");
                }
            }
        }
        else{

            if(mainActivity != null){
                mainActivity.showToast("Drink name cannot be empty");
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        dropdown = (Spinner) getView().findViewById(R.id.drinkTypeDropDown);
        String[] items = new String[]{"Beer", "Spirit", "Cider", "Cocktail", "Wine"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        beerName = (EditText) getView().findViewById(R.id.drinkNameInputBox2);

        beerHex = (EditText) getView().findViewById((R.id.drinkHexInputBox));


    }
}