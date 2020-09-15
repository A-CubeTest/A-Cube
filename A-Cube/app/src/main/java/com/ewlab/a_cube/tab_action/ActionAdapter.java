package com.ewlab.a_cube.tab_action;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;

import java.util.List;

public class ActionAdapter extends ArrayAdapter<Action> {

    public ActionAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public ActionAdapter(@NonNull Context context, int resource, @NonNull List<Action> objects) {
        super(context, resource, objects);
    }


    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            v = LayoutInflater.from(getContext()).inflate(R.layout.action_element, null);
        }

        Action ai = getItem(position);

        if (ai != null && !ai.getName().equals("Noise")) {
            final TextView actionName =  v.findViewById(R.id.action_name);
            final TextView actionType =  v.findViewById(R.id.action_type);
            actionName.setText(ai.getName());

            if (ai instanceof ActionButton) {
                actionType.setText(R.string.type_button);
            } else if (ai instanceof ActionVocal) {
                actionType.setText(R.string.type_vocal);
            }

        }
        return v;

    }

}
