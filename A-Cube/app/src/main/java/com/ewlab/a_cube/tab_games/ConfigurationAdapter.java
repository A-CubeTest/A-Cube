package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;

import java.util.List;

public class ConfigurationAdapter extends ArrayAdapter<Configuration> {
  private static final String TAG = GamesAdapter.class.getName();
  int positionSelected = -1;


  public ConfigurationAdapter(@NonNull Context context, int resource) {
    super(context, resource);
  }

  public ConfigurationAdapter(@NonNull Context context, int resource, @NonNull List<Configuration> objects) {
    super(context, resource, objects);
  }


  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View v = convertView;

    if (v == null) {
      v = LayoutInflater.from(getContext()).inflate(R.layout.configuration_item, null);
//            RadioButton r = v.findViewById(R.id.radioButton);

    }

    final Configuration conf = getItem(position);
        RadioButton r = (RadioButton) v.findViewById(R.id.radioButton);


    TextView tv = (TextView) v.findViewById(R.id.configurationName);
    tv.setText(conf.getConfName());

        if (conf.getSelected()) {
            r.setChecked(true);
            positionSelected = position;
        }


        if(positionSelected!=-1) {
            r.setChecked(position == positionSelected);
        }

        r.setTag(position);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainModel.getInstance().setSelectedConfiguration(conf);
                positionSelected = (Integer) view.getTag();
                notifyDataSetChanged();
                MainModel.getInstance().writeConfigurationsJson();
            }
        });
    return v;
  }
}