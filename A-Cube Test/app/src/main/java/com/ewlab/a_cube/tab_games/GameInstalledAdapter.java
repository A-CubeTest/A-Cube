package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


import com.ewlab.a_cube.R;

import java.util.ArrayList;
import java.util.List;

public class GameInstalledAdapter extends ArrayAdapter<ApplicationInfo> implements Filterable {

  private List<ApplicationInfo> originalData;
  private List<ApplicationInfo> filteredData;

  private List<ApplicationInfo> applist = null;
  private Context context;
  private PackageManager packageManager;

  public GameInstalledAdapter(Context context, int resource, List<ApplicationInfo> objects) {
    super(context, resource, objects);

    this.context = context;
    this.applist = objects;
    packageManager = context.getPackageManager();

    this.originalData = objects;
    this.filteredData = objects;

  }

  public int getCount(){
    return ((null != filteredData) ? filteredData.size() : 0);
  }

  public ApplicationInfo getItem(int position){
    return ((null != filteredData) ? filteredData.get(position) : null);
  }

  public long getItemId(int position){
    return position;
  }

  @Override
  public Filter getFilter(){

    return new Filter(){

      @Override
      protected FilterResults performFiltering(CharSequence charSequence) {

        FilterResults results = new FilterResults();

        if(charSequence == null || charSequence.length() == 0){

          results.values = originalData;
          results.count = originalData.size();

          return results;

        }
        else {

          ArrayList<ApplicationInfo> filterResultsData = new ArrayList<ApplicationInfo>();

          for(ApplicationInfo data : originalData){

            String a = (String)data.loadLabel(packageManager);
            a = a.toLowerCase();

            String b = (String)charSequence;
            b = b.toLowerCase();

            if(a.contains(b)){
              filterResultsData.add(data);
            }
          }

          results.values = filterResultsData;
          results.count = filterResultsData.size();
        }

        return results;
      }

      @Override
      protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        filteredData = (ArrayList<ApplicationInfo>)filterResults.values;
        notifyDataSetChanged();
      }
    };
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    View view = convertView;

    if(null == view){
      LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      //view = layoutInflater.inflate(R.layout.game_list_item, null);
      view = layoutInflater.inflate(R.layout.game_list_item2, null);
    }

    ApplicationInfo data = filteredData.get(position);

    if(null != data){

      TextView appName = (TextView) view.findViewById(R.id.app_name);
      //TextView packageName = (TextView) view.findViewById(R.id.app_package);
      ImageView appIcon = (ImageView) view.findViewById(R.id.app_icon);

      appName.setText(data.loadLabel(packageManager));
      //NON SERVE MOSTRSRE NOME DEL PACKAGE
      //packageName.setText(data.packageName);
      appIcon.setImageDrawable(data.loadIcon(packageManager));
    }

    return view;
  }

}
