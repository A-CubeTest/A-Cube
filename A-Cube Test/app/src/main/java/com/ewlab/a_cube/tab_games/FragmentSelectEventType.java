package com.ewlab.a_cube.tab_games;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.R;

public class FragmentSelectEventType extends Fragment {

    TextView eventType_tv;
    ImageView eventType_iv;
    RecyclerView recyclerView_events;
    ImageButton cancel_btn;
    SelectEventTypeAdapter selectEventTypeAdapter;

    public FragmentSelectEventType() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("LIFE_CYCLE","OnCreateView FragmentSelectEventType");
        View v = inflater.inflate(R.layout.fragment_select_event_type, container, false);

        eventType_tv = v.findViewById(R.id.eventType_tv);
        eventType_iv = v.findViewById(R.id.eventType_iv);

        //TODO RECYCLER VIEW ACTIONS
        recyclerView_events = v.findViewById(R.id.recycler_view_events);
        recyclerView_events.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                false));
        selectEventTypeAdapter = new SelectEventTypeAdapter((NewLink) this.getActivity());
        recyclerView_events.setAdapter(selectEventTypeAdapter);
        selectEventTypeAdapter.notifyDataSetChanged();

        cancel_btn = v.findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RIMUOVO IL FRAGMENT
                Fragment fragment = getFragmentManager().findFragmentByTag("fragmentEventsType");
                FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
                transaction2.remove(fragment).commit();
            }
        });

        return v;
    }

}
