/*
NEWEXTERNALKEY: this activity allows to save the new device key
 */

package com.ewlab.a_cube.external_devices;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.tab_games.DialogTutorial;

public class KeyFromDevice extends AppCompatActivity {

    private static final String TAG = KeyFromDevice.class.getName();


    private static TextView action, extKey, device;

    private static Context context;

    private static String actionName,deviceID, keyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_from_device);

        context = getApplicationContext();

        actionName = getIntent().getStringExtra("action_name");

        extKey = findViewById(R.id.button_code);
        device = findViewById(R.id.device_code);

    }

    /*************************************************/
    /*************************************************/
    /*************************************************/

    //To be able to save all the informations of the device and key,
    //it needs to manage the keyDown and keyUp event
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        InputDevice device_in = event.getDevice();
        if(device_in.getName().equals("Virtual"))
            return false;
        else {
            extKey.setText((new Integer(keyCode)).toString());
            device.setText(device_in.getName());
            deviceID = device_in.getName();
            keyID = (String) extKey.getText();
            return true;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /*************************************************/
    /*************************************************/
    /*************************************************/

    // this method is defined in the xml file of layout that concerns this activity
    public void saveExtKey(View view){

        ActionButton newButton = new ActionButton(actionName, deviceID, keyID);
        Boolean keyAdded = MainModel.getInstance().addAction(newButton);

        if(keyAdded) {

            MainModel.getInstance().writeActionsJson();
            Toast.makeText(context, R.string.button_saved, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

        }
        else {

            if(device.getText().length()>0) {

                Toast.makeText(context, R.string.button_not_saved2, Toast.LENGTH_LONG).show();

            }
            else {

                Toast.makeText(context, R.string.button_not_saved, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_info:

                FragmentManager fm = getSupportFragmentManager();
                DialogTutorial alertDialog = new DialogTutorial();
                alertDialog.show(fm, "fragment_alert");
                alertDialog.getData(getString(R.string.new_button_tutorial));
        }

        return super.onOptionsItemSelected(item);
    }
}
