package com.fant.insapp.app;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


public class ReportAdapter extends BaseAdapter {

	
	
   private LayoutInflater inflater;
  private ArrayList<ReportObject> objects;

   private class ViewHolder {
      TextView textView1;
      TextView textView2;
   }

   public ReportAdapter(Context context, ArrayList<ReportObject> objects) {
      inflater = LayoutInflater.from(context);
      this.objects = objects;
   }

   public int getCount() {
      return objects.size();
   }

   public ReportObject getItem(int position) {
      return objects.get(position);
   }

   public long getItemId(int position) {
      return position;
   }

   public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;
      if(convertView == null) {
         holder = new ViewHolder();
         convertView = inflater.inflate(R.layout.list_report, null);
         holder.textView1 = (TextView) convertView.findViewById(R.id.descrizioneReport);
        holder.textView2 = (TextView) convertView.findViewById(R.id.valoreReport);
         convertView.setTag(holder);
      } else {
         holder = (ViewHolder) convertView.getTag();
      }
      holder.textView1.setText(objects.get(position).getProp1());
      holder.textView2.setText(objects.get(position).getProp2());
      return convertView;
   }
}