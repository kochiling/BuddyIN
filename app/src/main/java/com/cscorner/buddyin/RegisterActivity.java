package com.cscorner.buddyin;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import android.content.DialogInterface;
import java.util.Collections;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText nameinput,usernameinput,ageinput,seniorinput,hobbiesinput;
    AutoCompleteTextView genderinput,courseinput,nationalityinput;
    MultiAutoCompleteTextView personalitiesinput;
    CheckBox checkBox;
    Button registerbtn;
    ImageView upload_pics;

    boolean[] selectedhobbies;
    ArrayList<Integer> hobbiesList = new ArrayList<>();
    String[] hobbiesArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameinput = findViewById(R.id.nameinput);
        usernameinput = findViewById(R.id.usernameinput);
        ageinput = findViewById(R.id.ageinput);
        seniorinput = findViewById(R.id.seniorinput);
        genderinput = findViewById(R.id.genderinput);
        courseinput = findViewById(R.id.courseinput);
        nationalityinput = findViewById(R.id.nationalityinput);
        hobbiesinput = findViewById(R.id.hobbiesinput);
        personalitiesinput = findViewById(R.id.personalitiesinput);
        registerbtn = findViewById(R.id.registerbtn);
        upload_pics = findViewById(R.id.upload_pics);
        checkBox = findViewById(R.id.terms_checkbox);

        //GenderType
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genderinput, R.layout.drop_down_item);
        adapter.setDropDownViewResource(R.layout.drop_down_item);
        genderinput.setAdapter(adapter);

        genderinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(RegisterActivity.this, genderinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //CourseType
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, R.array.coursesinput, R.layout.drop_down_item);
        adapter1.setDropDownViewResource(R.layout.drop_down_item);
        courseinput.setAdapter(adapter1);

        courseinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(RegisterActivity.this, courseinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //NationalType
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.nationality, R.layout.drop_down_item);
        adapter2.setDropDownViewResource(R.layout.drop_down_item);
        nationalityinput.setAdapter(adapter2);

        nationalityinput.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(RegisterActivity.this, nationalityinput.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        //HobbiesType
        hobbiesArray = getResources().getStringArray(R.array.hobbies);
        // initialize selected array
        selectedhobbies = new boolean[hobbiesArray.length];

        hobbiesinput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                // set title
                builder.setTitle("Select Hobbies");
                // set dialog non cancelable
                builder.setCancelable(false);

                builder.setMultiChoiceItems(hobbiesArray, selectedhobbies, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        // check condition
                        if (b) {
                            // when checkbox selected
                            // Add position in list
                            hobbiesList.add(i);
                            // Sort array list
                            Collections.sort(hobbiesList);
                        } else {
                            // when checkbox unselected
                            // Remove position from list
                            hobbiesList.remove(Integer.valueOf(i));
                        }
                    }
                });

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Initialize string builder
                        StringBuilder stringBuilder = new StringBuilder();
                        // use for loop
                        for (int j = 0; j < hobbiesList.size(); j++) {
                            // concat array value
                            stringBuilder.append(hobbiesArray[hobbiesList.get(j)]);
                            // check condition
                            if (j != hobbiesList.size() - 1) {
                                // When j value  not equal
                                // to list size - 1
                                // add comma
                                stringBuilder.append(", ");
                            }
                        }
                        // set text on textView
                        hobbiesinput.setText(stringBuilder.toString());
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                });
                builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // use for loop
                        for (int j = 0; j < selectedhobbies.length; j++) {
                            // remove all selection
                            selectedhobbies[j] = false;
                            // clear list
                            hobbiesList.clear();
                            // clear text view value
                            hobbiesinput.setText("");
                        }
                    }
                });
                // show dialog
                builder.show();
            }
        });

        registerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
            }
        });

    }


    private void saveInfo() {
        String hobbies = hobbiesinput.getText().toString().trim();

        // Remove trailing comma if it exists
        if (hobbies.endsWith(",")) {
            hobbies = hobbies.substring(0, hobbies.length() - 1);
        }

        // Do something with the text (for example, display it in a Toast)
        Toast.makeText(RegisterActivity.this, hobbies, Toast.LENGTH_LONG).show();
    }

}