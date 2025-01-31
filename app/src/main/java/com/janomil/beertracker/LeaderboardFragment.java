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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * LeaderboardFragment - Create a leaderboard to show the top 10 users
 */
public class LeaderboardFragment extends Fragment {
    Spinner dropdown;

    TextView pos1Text;
    TextView pos2Text;
    TextView pos3Text;
    TextView pos4Text;
    TextView pos5Text;
    TextView pos6Text ;
    TextView pos7Text;
    TextView pos8Text;
    TextView pos9Text;
    TextView pos10Text;
    RadioGroup radioGroup;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LeaderboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LeaderboardFragment newInstance(String param1, String param2) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        dropdown = (Spinner) getView().findViewById(R.id.drinkTypeDropDown2);
        String[] items = new String[]{"All","Beer", "Spirit", "Cider", "Cocktail", "Wine"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        pos1Text = (TextView) view.findViewById(R.id.pos1Text);
        pos2Text = (TextView) view.findViewById(R.id.pos2Text);
        pos3Text = (TextView) view.findViewById(R.id.pos3Text);
        pos4Text = (TextView) view.findViewById(R.id.pos4Text);
        pos5Text = (TextView) view.findViewById(R.id.pos5Text);
        pos6Text = (TextView) view.findViewById(R.id.pos6Text);
        pos7Text = (TextView) view.findViewById(R.id.pos7Text);
        pos8Text = (TextView) view.findViewById(R.id.pos8Text);
        pos9Text = (TextView) view.findViewById(R.id.pos9Text);
        pos10Text = (TextView) view.findViewById(R.id.pos10Text);
        radioGroup = (RadioGroup) view.findViewById(R.id.RadioGroup);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                refreshLeaderboard();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                refreshLeaderboard();
            }
        });
        refreshLeaderboard();
    }

    public void refreshLeaderboard(){
        ((MainActivity) requireActivity()).startLoad();
        clearLeaderboard();
        String selectedRbText = "Y";
        String beerTypeStr = dropdown.getSelectedItem().toString();
        int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = getActivity().findViewById(selectedRadioButtonId);
            selectedRbText = selectedRadioButton.getText().toString();
        }
        String finalSelectedRbText = selectedRbText;
        new Thread(()->{
            try(Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)){
                SharedPreferences sp = getActivity().getSharedPreferences("userID", Context.MODE_PRIVATE);
                boolean rankByUnits = sp.getBoolean("LB_RankByUnits", true);
                String queryStr = "SELECT ubl.UserID , u.UserNickname ,SUM(ubl.DrinkSizeMultiplier) as Multiplier, ROUND(SUM(ubl.DrinkSizeMultiplier * COALESCE(b.BeerUnits, 1)), 2) AS Units FROM UserBeerLink ubl JOIN UserData u ON ubl.userID = u.UserID JOIN BeerData b ON ubl.BeerID = b.BeerID WHERE ubl.Timestamp >= NOW() - INTERVAL 1";
                switch(finalSelectedRbText){
                    case "D":
                        queryStr += " DAY";
                        break;
                    case "W":
                        queryStr += " WEEK";
                        break;
                    case "M":
                        queryStr += " MONTH";
                        break;
                    default:
                        queryStr += " YEAR";
                        break;

                }
                if(beerTypeStr != "All"){
                    queryStr += " AND b.BeerType = ?";
                }
                if(rankByUnits){
                    queryStr +=  " GROUP BY UserID ORDER BY Units DESC";
                }
                else{
                    queryStr +=  " GROUP BY UserID ORDER BY Multiplier DESC";
                }


                PreparedStatement stmt = con.prepareStatement(queryStr);
                if(beerTypeStr != "All"){
                    stmt.setString(1, beerTypeStr);
                }
                ResultSet rs = stmt.executeQuery();
                int count = 1;
                while(rs.next()){
                    int userID = rs.getInt("UserID");
                    String userNickname = rs.getString("UserNickname");

                    Double multiplier = 0.0;
                    if(rankByUnits){
                        multiplier = rs.getDouble("Units");
                    }
                    else{
                        multiplier = rs.getDouble("Multiplier");
                    }

                    String formattedText = "";
                    if(userNickname != null  && !userNickname.isEmpty()){
                        formattedText = (userNickname+"[" + String.valueOf(userID)+"]" + " ("+ multiplier + ")");
                    }
                    else{
                        formattedText = ("User ID: " +String.valueOf(userID) +" ("+ multiplier + ")");
                    }
                    String finalFormattedText = formattedText;
                    switch(count){
                        case 1:

                            getActivity().runOnUiThread(() -> {
                                pos1Text.setText(finalFormattedText);
                            });
                            break;
                        case 2:
                            getActivity().runOnUiThread(() -> {
                                pos2Text.setText(finalFormattedText);
                            });
                            break;
                        case 3:
                            getActivity().runOnUiThread(() -> {
                                pos3Text.setText(finalFormattedText);
                            });
                            break;
                        case 4:
                            getActivity().runOnUiThread(() -> {
                                pos4Text.setText(finalFormattedText);
                            });
                            break;
                        case 5:
                            getActivity().runOnUiThread(() -> {
                                pos5Text.setText(finalFormattedText);
                            });
                            break;
                        case 6:
                            getActivity().runOnUiThread(() -> {
                                pos6Text.setText(finalFormattedText);
                            });
                            break;
                        case 7:
                            getActivity().runOnUiThread(() -> {
                                pos7Text.setText(finalFormattedText);
                            });
                            break;
                        case 8:
                            getActivity().runOnUiThread(() -> {
                                pos8Text.setText(finalFormattedText);
                            });
                            break;
                        case 9:
                            getActivity().runOnUiThread(() -> {
                                pos9Text.setText(finalFormattedText);
                            });
                            break;
                        case 10:
                            getActivity().runOnUiThread(() -> {
                                pos10Text.setText(finalFormattedText);
                            });
                            break;


                    }
                    count++;
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
            ((MainActivity) requireActivity()).endLoad();
        }).start();

    }

    private void clearLeaderboard(){
        pos1Text.setText("");
        pos2Text.setText("");
        pos3Text.setText("");
        pos4Text.setText("");
        pos5Text.setText("");
        pos6Text.setText("");
        pos7Text.setText("");
        pos8Text.setText("");
        pos9Text.setText("");
        pos10Text.setText("");

    }
}