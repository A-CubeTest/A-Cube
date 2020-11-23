/*
KeyDetails: this activity allows to show the information about a device key
 */
package com.ewlab.a_cube.external_devices;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;

public class KeyDetails extends AppCompatActivity {
    private static final String TAG = KeyDetails.class.getName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_details);

        final String actionName = getIntent().getStringExtra("action_name");
        final Intent previousActivity = new Intent(this, MainActivity.class);

        TextView acName = findViewById(R.id.action_name);
        TextView acType = findViewById(R.id.action_type);
        TextView devId = findViewById(R.id.detail_id_dev);
        TextView keyId = findViewById(R.id.detail_key_c);
        Button deleteKey = findViewById(R.id.deleteKey);

        //check that the action is a button type
        Action action =  MainModel.getInstance().getAction(actionName);

        if(!(action instanceof ActionButton)) {

            Log.e(TAG, "The selected action is not a Button type as expected.");
            startActivity(previousActivity);

        }
        else {

            ActionButton actionItemButton = (ActionButton) action;

            //it updates the view of the activity with the informations of device key
            acName.setText(actionItemButton.getName());
            acType.setText(R.string.action_type_button);
            devId.setText(actionItemButton.getDeviceId());
            keyId.setText(actionItemButton.getKeyId());

            //at the press of the button I eliminate this action
            final String finalName = actionName;
            deleteKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Action removed = MainModel.getInstance().removeAction(finalName);

                    if (removed != null) {

                        MainModel.getInstance().writeActionsJson();
                        MainModel.getInstance().writeConfigurationsJson();

                        Toast.makeText(view.getContext(), R.string.button_deleted, Toast.LENGTH_SHORT).show();

                        previousActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(previousActivity);
                    }
                }
            });
        }
    }
}
