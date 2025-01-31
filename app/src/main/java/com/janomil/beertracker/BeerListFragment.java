package com.janomil.beertracker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * BeerListFragment - Creates a list of available drinks, then when a user clicks on one it is
 * returned to the AddNewFragment
 *
 * Intended as an easier way to add a users drinks to UserBeerLink
 */
public class BeerListFragment extends Fragment {

    public BeerListFragment() {
        // Required empty public constructor
    }

    public static BeerListFragment newInstance(String param1, String param2) {
        BeerListFragment fragment = new BeerListFragment();
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
        return inflater.inflate(R.layout.fragment_beer_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        ((MainActivity) requireActivity()).startLoad();
        ScrollView scrollView = (ScrollView) view.findViewById(R.id.scrollView1);
        LinearLayout linearLayout = (LinearLayout) scrollView.findViewById(R.id.linearLayout1);
        LayoutInflater inflater = LayoutInflater.from(getContext());


        new Thread(()->{
            try(Connection con = DriverManager.getConnection(MainActivity.DB_URL, MainActivity.DB_USER, MainActivity.DB_PASSWORD)){
                String queryStr = "SELECT b.BeerName, b.BeerType, b.BeerColour From BeerData b ORDER BY b.BeerName";

                PreparedStatement stmt = con.prepareStatement(queryStr);
                ResultSet rs = stmt.executeQuery();
                while(rs.next()){
                    String beerName = rs.getString("BeerName");
                    String beerType = rs.getString("BeerType");
                    String beerColour = rs.getString("BeerColour");
                    Log.d("DB_DEBUG", "BeerName: " + beerName + ", BeerType: " + beerType);
                    getActivity().runOnUiThread(() -> {
                        View customView = inflater.inflate(R.layout.drinkcardview, linearLayout, false);

                        customView.setBackgroundColor(Color.parseColor(beerColour));
                        // Modify the inflated view
                        TextView tvTitle = customView.findViewById(R.id.textView14);

                        tvTitle.setText(beerName+" - "+ beerType);

                        // Add the custom view to the parent layout
                        linearLayout.addView(customView);
                        customView.setOnClickListener(this::onClickBeer);
                        Space space = new Space(getContext());
                        space.setMinimumHeight(20);
                        linearLayout.addView(space);
                        Log.d("DB_DEBUG",String.valueOf(linearLayout.getChildCount()));
                    });



                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
            ((MainActivity) requireActivity()).endLoad();
        }).start();

    }

    public void onClickBeer(View view){
        FrameLayout frameLayout = (FrameLayout) getView().findViewById(R.id.frameLayout);
        Fragment fragment = new AddNewFragment();
        Bundle bundle = new Bundle();
        TextView textView = (TextView) view.findViewById(R.id.textView14);

        String[] parts = String.valueOf(textView.getText()).split(" - ");


        String drinkName = parts[0];
        String drinkType = parts[1];
        bundle.putString("BeerName", drinkName);
        bundle.putString("BeerType", drinkType);
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction().replace(R.id.frameLayout, fragment)
                .addToBackStack(null)
                .commit();
    }
}