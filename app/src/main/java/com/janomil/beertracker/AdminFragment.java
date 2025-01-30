package com.janomil.beertracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

/**
 * A fragment for people with admin permissions to do admin actions
 */
public class AdminFragment extends Fragment {

    SharedPreferences sp;
    TextView commandTextBox;
    EditText actAsEditText;
    EditText HI_ShowLastNum;
    Switch LB_RankByUnitsSwitch;
    Switch OV_RankByUnitsSwitch;
    Switch APP_PromptUpdate;

    public AdminFragment() {
        // Required empty public constructor
    }

    public static AdminFragment newInstance(String param1, String param2) {
        AdminFragment fragment = new AdminFragment();
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
        View view =  inflater.inflate(R.layout.fragment_admin, container, false);
        ImageButton button = view.findViewById(R.id.button3);

        button.setOnClickListener(v -> onClickCommandExecute(v));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        sp = getActivity().getSharedPreferences("userID", Context.MODE_PRIVATE);
        actAsEditText = view.findViewById(R.id.editTextText2);
        HI_ShowLastNum = view.findViewById(R.id.editTextText);
        LB_RankByUnitsSwitch = view.findViewById(R.id.switch2);
        OV_RankByUnitsSwitch = view.findViewById(R.id.switch3);
        APP_PromptUpdate = view.findViewById(R.id.switch4);
        int userID =((MainActivity) getActivity()).getActingUserID();
        actAsEditText.setText(String.valueOf(userID));
        HI_ShowLastNum.setText(String.valueOf(sp.getInt("HI_ShowLastNum", 0)));
        LB_RankByUnitsSwitch.setChecked(sp.getBoolean("LB_RankByUnits", false));
        OV_RankByUnitsSwitch.setChecked(sp.getBoolean("OV_RankByUnits", false));
        APP_PromptUpdate.setChecked(sp.getBoolean("APP_PromptToUpdate", false));
        commandTextBox = view.findViewById(R.id.editTextText3);



        EditText userIDEditText = view.findViewById(R.id.editTextText2);
        userIDEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    int userID = Integer.valueOf(charSequence.toString());
                    actAs(userID);
                }
                catch(Exception e){

                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void onClickCommandExecute(View v){

        String commandText = commandTextBox.getText().toString().toLowerCase();

        String[] args = commandText.split(" ");

        switch(args[0]){
            case "actas":
                try{
                    int userID = Integer.valueOf(args[1]);
                    actAs(userID);
                } catch(Exception e) {
                    ((MainActivity) getActivity()).showToast("Invalid UserID");
                }
                break;
            case "":
                ((MainActivity) getActivity()).showToast("Nothing entered");
                break;
            default:
                ((MainActivity) getActivity()).showToast("Invalid command");
                break;
        }
    }

    private void actAs(int userID){
        if(userID != 0){
            ((MainActivity) getActivity()).setActingUserID(userID);
            commandTextBox.setText("");
        }

    }
}