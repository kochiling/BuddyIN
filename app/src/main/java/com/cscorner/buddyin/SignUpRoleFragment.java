package com.cscorner.buddyin;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class SignUpRoleFragment extends Fragment {

    ImageButton lecturerbtn;
    ImageButton studentbtn;
    Button backToLoginbtn;
    FrameLayout parent_framelayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up_role, container, false);

        parent_framelayout = getActivity().findViewById(R.id.parent_framelayout);
        backToLoginbtn = view.findViewById(R.id.backToLoginbtn);
        lecturerbtn = view.findViewById(R.id.lecturerbtn);
        studentbtn = view.findViewById(R.id.studentbtn);

        studentbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        lecturerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LecturerSignUpActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        backToLoginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFragment(new LoginFragment());

            }
        });

        return view;
    }

    private void setFragment (Fragment fragment){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(parent_framelayout.getId(), fragment);
        fragmentTransaction.commit();
    }
}