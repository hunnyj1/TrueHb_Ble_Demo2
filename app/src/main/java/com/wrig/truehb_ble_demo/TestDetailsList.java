package com.wrig.truehb_ble_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.wrig.truehb_ble_demo.adapter.TestDetailsAdapter;
import com.wrig.truehb_ble_demo.database.DatabaseHelper;
import com.wrig.truehb_ble_demo.modal.TestDetailsModal;

import java.util.ArrayList;

public class TestDetailsList extends AppCompatActivity implements SearchView.OnQueryTextListener {
    DatabaseHelper dataBaseHelper;
    ListView listView;
    ArrayList<TestDetailsModal> patientArrayList;
    private SearchView searchView;
    private MenuItem searchMenuItem;
    TestDetailsAdapter testDetailsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dataBaseHelper=new DatabaseHelper(TestDetailsList.this);
        listView=findViewById(R.id.patient_list);
        patientArrayList = new ArrayList<>();
        Cursor cursor = dataBaseHelper.getAllData();
        if (cursor.moveToFirst()) {
            do {
                try {
                    patientArrayList.add(new TestDetailsModal(cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_DEVICE_ID)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_HB_RESULT)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_DATE)),
                            cursor.getString(cursor.getColumnIndex(DatabaseHelper.COL_TIME))));
                }catch (Exception e){}
            }
            while (cursor.moveToNext());

            testDetailsAdapter=    new TestDetailsAdapter(getApplicationContext(),R.layout.testdeatilsadapter,patientArrayList);
            listView.setAdapter(testDetailsAdapter);

        }
        else
        {
            Toast.makeText(getApplicationContext(),"No Data Found",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.testdetails,menu);
        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.app_bar_search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if(TextUtils.isEmpty(newText))
        {
            testDetailsAdapter.getFilter().filter("");
            listView.clearTextFilter();
        }
        else
        {
            testDetailsAdapter.getFilter().filter(newText);
        }

        return true;
    }
}
