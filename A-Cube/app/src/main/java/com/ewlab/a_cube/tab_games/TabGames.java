/*
TABGAMES: fragment to show the informatios about games
 */

package com.ewlab.a_cube.tab_games;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TabGames extends Fragment  {

  public static GamesAdapter adapter;

  private ListView listview;

  private View view;

  public TabGames() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment

    ArrayList<Game> games = MainModel.getInstance().getGames();

    if (games.size() > 0) {
      view = inflater.inflate(R.layout.fragment_tab_games, container, false);
      listview = view.findViewById(R.id.tab_games_listview);
      setListView(games);


      final Intent confList = new Intent(view.getContext(), ConfigurationList.class);

      listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
          Game gi = (Game)listview.getItemAtPosition(i);
          confList.putExtra("title",gi.getTitle());
          startActivity(confList);
        }
      });

      listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
          Game gameToRemove = (Game)listview.getItemAtPosition(position);

          FragmentManager fm = getFragmentManager();
          DialogGame alertDialog = new DialogGame();
          alertDialog.show(fm, "fragment_alert");
          alertDialog.getData(gameToRemove.getTitle());
          return true;
        }
      });


    }else{

      view = inflater.inflate(R.layout.empty_listview_games, container, false);
    }


    FloatingActionButton fab = view.findViewById(R.id.newGame);

    final Intent addGame = new Intent(view.getContext(), GameList.class);

    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivity(addGame);
      }
    });

    return view;

  }

  private void setListView(ArrayList<Game> games){
    adapter = new GamesAdapter(view.getContext(),android.R.layout.list_content, games);

    listview.setAdapter(adapter);
  }

}
