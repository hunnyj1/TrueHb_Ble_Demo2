package com.wrig.truehb_ble_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.wrig.truehb_ble_demo.R;
import com.wrig.truehb_ble_demo.modal.TestDetailsModal;

import java.util.ArrayList;

public class TestDetailsAdapter extends ArrayAdapter<TestDetailsModal> implements Filterable {
    private int resourceLayout;
    private Context mContext;
    ServerFilter serverFilter;
    private ArrayList<TestDetailsModal> serverList;
    private ArrayList<TestDetailsModal> serverFilteredList;

    public TestDetailsAdapter(Context context, int resource, ArrayList<TestDetailsModal> items) {
        super(context, resource,items);
        this.resourceLayout = resource;
        this.mContext = context;
        this.serverList=items;
        this.serverFilteredList=items;

        getFilter();
    }
    @Override
    public int getCount() {
        return serverFilteredList.size();
    }

    @Override
    public TestDetailsModal getItem(int i) {
        return serverFilteredList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(mContext);
            v = vi.inflate(resourceLayout, null);
        }

        TestDetailsModal p = getItem(position);
        if (p != null) {
            TextView tt1 =  v.findViewById(R.id.textpatientid);
            TextView tt2 =  v.findViewById(R.id.texthb);
            TextView tt3 =  v.findViewById(R.id.textdate);
            TextView tt4 =  v.findViewById(R.id.textime);



            if (tt1 != null) {
                tt1.setText(p.getDeviceid());
            }

            if (tt2 != null) {
                tt2.setText(p.getHbresult());
            }
            if (tt3 != null) {
                tt3.setText(p.getDate());
            }
            if (tt4 != null) {
                tt4.setText(p.getTime());
            }

        }

        return v;

    }
    @Override
    public Filter getFilter() {
        if(serverFilter==null)
        {
            serverFilter=new ServerFilter();
        }
        return serverFilter;
    }
    private class ServerFilter extends Filter
    {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<TestDetailsModal> tempList = new ArrayList<TestDetailsModal>();

                // search content in friend list
                for (TestDetailsModal user : serverList) {
                    if (user.getDeviceid().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(user);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = serverList.size();
                filterResults.values = serverList;
            }

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            serverFilteredList = (ArrayList<TestDetailsModal>) results.values;
            notifyDataSetChanged();
        }
    }
}
