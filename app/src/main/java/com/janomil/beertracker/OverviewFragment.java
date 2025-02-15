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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

/**
 * OverviewFragment - show the user details about their top 3 drink types
 */
public class OverviewFragment extends Fragment {

    SharedPreferences sp;
    FrameLayout frameLayout;
    Boolean isAdmin;

    public OverviewFragment() {
        // Required empty public constructor
    }

    public static OverviewFragment newInstance(String param1, String param2) {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onClickLeaderboard(View view){
        frameLayout = (FrameLayout) getView().findViewById(R.id.frameLayout);

        getFragmentManager().beginTransaction().replace(R.id.frameLayout, new LeaderboardFragment())
                .addToBackStack(null)
                .commit();
    }

    public void onClickAdminMode(View view){
        frameLayout = (FrameLayout) getView().findViewById(R.id.frameLayout);
        if(isAdmin){
            getFragmentManager().beginTransaction().replace(R.id.frameLayout, new AdminFragment())
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        ImageButton button = view.findViewById(R.id.imageButton2);
        isAdmin = false;
        button.setOnClickListener(v -> onClickAdminMode(v));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        ImageButton leaderboardButton = (ImageButton) getView().findViewById(R.id.imageButton);
        leaderboardButton.setOnClickListener(this::onClickLeaderboard);

        TextView totalBeersTextView = (TextView) getView().findViewById(R.id.NumOfBeersValue);
        TextView totalBeerBrandTextView = (TextView) getView().findViewById(R.id.NumOfBeerBrandsValue);
        TextView beerTitle = (TextView) getView().findViewById(R.id.BeerTitle);

        TextView totalCidersTextView = (TextView) getView().findViewById(R.id.NumOfCidersValue);
        TextView totalCiderBrandsTextView = (TextView) getView().findViewById(R.id.NumOfCidersBrandsValue);
        TextView ciderTitle = (TextView) getView().findViewById(R.id.CiderTitle);

        TextView totalSpiritsTextView = (TextView) getView().findViewById(R.id.NumOfSpiritsValue);
        TextView totalSpiritBrandsTextView = (TextView) getView().findViewById(R.id.NumOfSpiritsBrandsValue);
        TextView spiritsTitle = (TextView) getView().findViewById(R.id.SpiritsTitle);

        ((MainActivity) requireActivity()).startLoad();

        new Thread(()-> {
            try(Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)) {
                String queryStr = "SELECT UserIsAdmin FROM UserData WHERE UserID = ?";
                PreparedStatement stmt = con.prepareStatement(queryStr);
                int userID =((MainActivity) getActivity()).getActingUserID();
                stmt.setInt(1,userID);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                isAdmin = rs.getBoolean("UserIsAdmin");
                ImageButton button = view.findViewById(R.id.imageButton2);
                if(isAdmin){
                    button.setVisibility(View.VISIBLE);

                }
                else{
                    button.setVisibility(View.GONE);
                }
            }
            catch (Exception e){
                Log.e("ERROR", e.getMessage());
            }
        }).start();

        new Thread(()->{
            try(Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)){
                SharedPreferences sp = getActivity().getSharedPreferences("userID", Context.MODE_PRIVATE);
                boolean rankByUnits = sp.getBoolean("OV_RankByUnits", true);
                String queryStr = "";
                if(rankByUnits){
                    queryStr = "SELECT b.BeerType, COUNT(*) AS EntryCount, COUNT(DISTINCT ubl.BeerID) AS DistinctBeerCount, ROUND(SUM(ubl.DrinkSizeMultiplier * COALESCE(b.BeerUnits, 1)), 2) AS Multiplier FROM UserBeerLink ubl JOIN BeerData b ON ubl.BeerID = b.BeerID WHERE ubl.UserID = ? GROUP BY b.BeerType ORDER BY Multiplier DESC LIMIT 3";
                }
                else{
                    queryStr = "SELECT b.BeerType, COUNT(*) AS EntryCount, COUNT(DISTINCT ubl.BeerID) AS DistinctBeerCount, SUM(ubl.DrinkSizeMultiplier) AS Multiplier FROM UserBeerLink ubl JOIN BeerData b ON ubl.BeerID = b.BeerID WHERE ubl.UserID = ? GROUP BY b.BeerType ORDER BY Multiplier DESC LIMIT 3";
                }

                PreparedStatement stmt = con.prepareStatement(queryStr);
                int userID =((MainActivity) getActivity()).getActingUserID();
                stmt.setInt(1,userID);
                ResultSet rs = stmt.executeQuery();
                int numOfIterations = 1;
                while(rs.next()){
                    String beerType = rs.getString("BeerType");
                    Double entryCount = rs.getDouble("Multiplier");
                    int distinctBeerCount = rs.getInt("DistinctBeerCount");

                    switch(numOfIterations){
                        case 1:
                            getActivity().runOnUiThread(() -> {
                                totalBeersTextView.setText(String.valueOf(entryCount));
                                totalBeerBrandTextView.setText(String.valueOf(distinctBeerCount));
                                beerTitle.setText(beerType);
                            });
                            break;
                        case 2:
                            getActivity().runOnUiThread(() -> {
                                totalSpiritsTextView.setText(String.valueOf(entryCount));
                                totalSpiritBrandsTextView.setText(String.valueOf(distinctBeerCount));
                                spiritsTitle.setText(beerType);


                            });

                            break;
                        case 3:
                            getActivity().runOnUiThread(() -> {
                                totalCidersTextView.setText(String.valueOf(entryCount));
                                totalCiderBrandsTextView.setText(String.valueOf(distinctBeerCount));
                                ciderTitle.setText(beerType);
                            });
                            break;
                    }
                    numOfIterations++;
                }


            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
            ((MainActivity) requireActivity()).endLoad();
        }).start();
    }
}