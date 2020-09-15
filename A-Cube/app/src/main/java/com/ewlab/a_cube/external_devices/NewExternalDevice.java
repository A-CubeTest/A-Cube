/*
NewExternalDevice: activity that allows the user to specify which device and key wants to save
 */

package com.ewlab.a_cube.external_devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;
import java.util.List;


public class NewExternalDevice extends AppCompatActivity {
    private static final String TAG = NewExternalDevice.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_external_device);

        Button newDevice = findViewById(R.id.new_device);
        final EditText actionName = findViewById(R.id.action_name);

        final Intent keyRecorder = new Intent(this,KeyFromDevice.class);

        newDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean alreadyExist = false;
                // control to verify that the user has inserted the necessary information
                if(TextUtils.isEmpty(actionName.getText())){
                    actionName.setError(getString(R.string.insert_action_error));
                }else {
                    // it saves the information typed by the user
                    String buttonName = actionName.getText().toString();
                    List<Action> allActions = MainModel.getInstance().getActions();
                    for(Action action : allActions){
                        if(action.getName().equals(buttonName)){
                            actionName.setError(getString(R.string.insert_action_error2));
                            alreadyExist = true;
                        }
                    }

                    if(!alreadyExist) {
                        keyRecorder.putExtra("action_name", actionName.getText().toString());
                        startActivity(keyRecorder);
                    }
                }
            }
        });

    }
}

