package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.material.textfield.TextInputEditText;

public class LoginFragment extends Fragment {

    TextInputEditText emailinput,passwordinput;
    Button loginbtn,createAccbtn,forgetPasswordBtn;
    FrameLayout parent_framelayout;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_login, container, false);

        emailinput = view.findViewById(R.id.emailinput);
        passwordinput = view.findViewById(R.id.passwordinput);
        loginbtn = view.findViewById(R.id.loginbtn);
        createAccbtn = view.findViewById(R.id.createAccbtn);
        forgetPasswordBtn = view.findViewById(R.id.forgetPasswordbtn);
        parent_framelayout = getActivity().findViewById(R.id.parent_framelayout);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        createAccbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setFragment(new SignUpFragment());
                Intent intent = new Intent(requireActivity(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireActivity(), HomeActivity.class);
                startActivity(intent);
            }
        });

    }


    private void setFragment (Fragment fragment){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parent_framelayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}

