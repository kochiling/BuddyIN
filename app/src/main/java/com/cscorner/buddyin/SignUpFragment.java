package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputEditText;

public class SignUpFragment extends Fragment {

    TextInputEditText emailinput,passwordinput,confirmpassword;
    Button singupbtn,backToLoginbtn;
    FrameLayout parent_framelayout;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        emailinput = view.findViewById(R.id.emailinput);
        passwordinput = view.findViewById(R.id.passwordinput);
        confirmpassword = view.findViewById(R.id.confirminput);
        singupbtn = view.findViewById(R.id.singupbtn);
        backToLoginbtn = view.findViewById(R.id.backToLoginbtn);
        parent_framelayout = getActivity().findViewById(R.id.parent_framelayout);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backToLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new LoginFragment());

            }
        });

    }

    private void setFragment (Fragment fragment){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parent_framelayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}