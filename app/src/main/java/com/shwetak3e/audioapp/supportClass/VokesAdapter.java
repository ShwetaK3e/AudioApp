package com.shwetak3e.audioapp.supportClass;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.shwetak3e.audioapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pervacio on 3/9/2017.
 */

public class VokesAdapter extends BaseAdapter {

    Context context;
    List<String> vokes=new ArrayList<>();
    LayoutInflater inflater;

    public VokesAdapter(Context context, List<String> vokes) {
        this.context = context;
        this.vokes = vokes;
        for(String v:vokes){
            Log.i("TAG",v.charAt(2)+"");
        }
        this.inflater=(LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return vokes.size();
    }

    @Override
    public Object getItem(int position) {
        return vokes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder=new Holder();
        if (convertView == null) {
            convertView= inflater.inflate(R.layout.each_voke, null);
            holder.vokeName = (TextView) convertView.findViewById(R.id.vokeName);
            convertView.setTag(holder);
        } else {
            holder=(Holder) convertView.getTag();
        }
        holder.vokeName.setText(vokes.get(position));
        return convertView;
    }

    private class Holder{
        TextView vokeName;

    }
}
