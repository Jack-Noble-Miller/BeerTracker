package com.janomil.beertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * HistoryFragment - Show a user the drink history for their userID
 */
public class HistoryFragment extends Fragment {

    SharedPreferences sp;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        ((MainActivity) requireActivity()).startLoad();
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollView);
        LinearLayout linearLayout = (LinearLayout) scrollView.findViewById(R.id.linearLayout);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        sp = getActivity().getSharedPreferences("userID", Context.MODE_PRIVATE);
        new Thread(()->{
            try(Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)){
                int showLastNum = sp.getInt("HI_ShowLastNum", 0);
                String queryStr = "SELECT b.BeerName, b.BeerType, b.BeerColour From UserBeerLink ubl JOIN BeerData b ON ubl.BeerID = b.BeerID WHERE ubl.UserID = ? ORDER BY ubl.Timestamp DESC";
                if(showLastNum!=0 && showLastNum >= 1){
                    queryStr += " LIMIT ?";
                }
                PreparedStatement stmt = con.prepareStatement(queryStr);
                int userID =((MainActivity) getActivity()).getActingUserID();
                stmt.setInt(1,userID);
                if(showLastNum!=0 && showLastNum >= 1){
                    stmt.setInt(2, showLastNum);
                }
                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                    String beerName = rs.getString("BeerName");
                    String beerType = rs.getString("BeerType");
                    String beerColour = rs.getString("BeerColour");
                    Log.d("DB_DEBUG", "BeerName: " + beerName + ", BeerType: " + beerType);
                    getActivity().runOnUiThread(() -> {
                        View customView = inflater.inflate(R.layout.drinkcardview, linearLayout, false);

                        customView.setBackgroundColor(Color.parseColor(beerColour));
                        TextView tvTitle = customView.findViewById(R.id.textView14);

                        tvTitle.setText(beerName +" - "+ beerType);

                        linearLayout.addView(customView);
                        Space space = new Space(getContext());
                        space.setMinimumHeight(20);
                        linearLayout.addView(space);
                    });
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
            ((MainActivity) requireActivity()).endLoad();
        }).start();

    }
}