package com.ewlab.a_cube.tab_games;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;

public class FragmentAddConfiguration extends Fragment {

    EditText editText_conf;
    TextView title_fragment_conf;
    Button back_btn, save_btn;
    public String game_name, confName, operation, cName;

    public FragmentAddConfiguration(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFE_CYCLE","OnCreateView FragmentAddConfiguration");
        View v = inflater.inflate(R.layout.fragment_add_configuration, container, false);
        title_fragment_conf = v.findViewById(R.id.title_fragment_conf);
        editText_conf = v.findViewById(R.id.editText_conf);
        back_btn = v.findViewById(R.id.back_btn);
        back_btn.setOnClickListener(buttonListener);
        save_btn = v.findViewById(R.id.save_btn);
        save_btn.setOnClickListener(buttonListener);

        Bundle bundle = this.getArguments();
        if (bundle != null) {

            game_name = bundle.getString("gameName");
            operation = bundle.getString("operation");
            cName = bundle.getString("cName");
        }
        //IN BASE ALL'OPERAZIONE CHE VOGLIO FARE CAMBIO IL TITOLO
        assert operation != null;
        if(operation.equals("editConf"))
            title_fragment_conf.setText(R.string.fragment_edit_config_title);
        else
            title_fragment_conf.setText(R.string.fragment_add_config_title);
        //SE STO MODIFICANDO IL NOME DI UNA CONF SETTO L'HINT DELL'EDITTEXT
        if(! cName.equals(""))
            editText_conf.setHint(cName);


        return v;
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            assert getFragmentManager() != null;
            Fragment fragment = getFragmentManager().findFragmentByTag("fragmentAddConf");
            FragmentTransaction transaction2 = getFragmentManager().beginTransaction();

            switch(view.getId()) {

                case R.id.back_btn:

                    transaction2.remove(fragment).commit();

                    break;

                case R.id.save_btn:

                    confName = editText_conf.getText().toString();
                    ArrayList<Configuration> configurations = MainModel.getInstance().getConfigurationsFromGame(game_name);

                    if (!confName.equals("") ) {

                        int checkConfName = 0;

                        for(Configuration c : configurations)
                            if(confName.equals(c.getConfName()))
                                checkConfName++;

                        if(checkConfName == 0) {

                            Game game  = MainModel.getInstance().getGame(game_name);
                            Configuration newConf = new Configuration(confName, game);
                            MainModel.getInstance().addNewConfiguration(newConf);
                            MainModel.getInstance().writeConfigurationsJson();

                            Intent intent_toConfdetail = new Intent(getContext(), ConfigurationDetail.class);

                            intent_toConfdetail.putExtra("title",game_name);
                            intent_toConfdetail.putExtra("name",confName);

                            startActivity(intent_toConfdetail);

                            transaction2.remove(fragment).commit();
                        }
                        else {
                            editText_conf.setError(getString(R.string.fragment_add_config_error_name));
                        }

                    }
                    else {
                        editText_conf.setError(getString(R.string.fragment_add_config_error_empty_name));
                    }

                    break;
            }

        }
    };
}
