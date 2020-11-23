package com.ewlab.a_cube.tab_games;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.SVMmodel;

public class RemoveLink extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_remove_event);

    final String title = getIntent().getStringExtra("title");
    this.setTitle(title+" - "+getString(R.string.remove_link));

    final String event = getIntent().getStringExtra("event");
    final String config = getIntent().getStringExtra("name");

    final Configuration thisConf = MainModel.getInstance().getConfiguration(title, config);
    Link thisLink = thisConf.getLink(event);
    final Action action = thisLink.getAction();

    TextView nameTV = findViewById(R.id.activity_remove_text_name);
    TextView eventTV = findViewById(R.id.activity_remove_text_event);
    TextView actionTV = findViewById(R.id.activity_remove_action_text);

    nameTV.setText(title);
    eventTV.setText(event);
    if(action!=null){
      actionTV.setText(action.getName());
    }


    Button remove = findViewById(R.id.activity_remove_delete);
    remove.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Link link = thisConf.getLink(event);
        Action action = link.getAction();

        boolean removed = thisConf.removeLink(link);

        //se viene rimosso il link cerco se esiste un svmModel che va bene per la nostra configurazione
        if(removed) {

          if(action instanceof ActionVocal) {

            SVMmodel newModel = MainModel.getInstance().getSVMmodel(thisConf.getVocalActions());
            thisConf.setModel(newModel);

          }

          MainModel.getInstance().writeConfigurationsJson();
          MainModel.getInstance().writeGamesJson();

          Toast.makeText(getApplicationContext(), R.string.link_deleted, Toast.LENGTH_LONG).show();

          Intent intent = new Intent(getApplicationContext(), MainActivity.class);
          startActivity(intent);
        }
      }
    });
  }
}
