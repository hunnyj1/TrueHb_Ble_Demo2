package com.wrig.truehb_ble_demo;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.wrig.truehb_ble_demo.database.DatabaseHelper;
import com.wrig.truehb_ble_demo.modal.TestDetailsModal;
import com.wrig.truehb_ble_demo.util.BluetoothUtils;
import com.wrig.truehb_ble_demo.util.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.wrig.truehb_ble_demo.Constants.SCAN_PERIOD;
import static com.wrig.truehb_ble_demo.Constants.SERVICE_UUID;

public class MainActivity extends AppCompatActivity implements GattClientActionListener {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback mScanCallback;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mGatt;

    private Map<String, BluetoothDevice> mScanResults;
    private Handler mHandler;

    private boolean mScanning;
    private boolean mConnected;
    private boolean mTimeInitialized;
    private boolean mEchoInitialized;
    TextView textViewdevice;
    EditText editTextdisplay;
    String deviceName;

    SharedPreferences sharedPreferences;

    Button buttonconnect;
    Spinner spinnerDevice;
    ArrayList<String> deviceArrayList;
    ProgressDialog progressDialog,scanprogressDialog;

    DatabaseHelper databaseHelper;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        sharedPreferences=getSharedPreferences("device_data",MODE_PRIVATE);


        buttonconnect=findViewById(R.id.btnconnect);
        buttonconnect.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshapeconnect1));
        textViewdevice=findViewById(R.id.textdevice);
        editTextdisplay=findViewById(R.id.display);


        spinnerDevice=findViewById(R.id.spindevice);
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Processing...");
        progressDialog.setCancelable(false);
        progressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Abort Test", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                sendMessage("U371");
            }
        });
        //progressDialog.getButton(ProgressDialog.BUTTON_NEUTRAL).setEnabled(false);

        scanprogressDialog=new ProgressDialog(MainActivity.this);
        scanprogressDialog.setMessage("Scanning...");
        scanprogressDialog.setCancelable(false);
        scanprogressDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Stop Scan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        deviceArrayList=new ArrayList();

        databaseHelper=new DatabaseHelper(MainActivity.this);

        requestPermission();



    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    void requestPermission()
    {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_FINE_LOCATION);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnectGattServer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check low energy support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Get a newer device
            logError("No BLE Support.");
            showToast("No BLE Support.");
            finish();
        }
        /*Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm") ;
        dateFormat.format(new Date());
        System.out.println(dateFormat.format(date));

        try {
            if(dateFormat.parse(dateFormat.format(date)).after(dateFormat.parse("17:00")) || dateFormat.parse(dateFormat.format(date)).before(dateFormat.parse("06:00")))
            {
                showToast("true");
            }else{
                showToast("false");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
    }
    @Override
    public void showToast(String msg)
    {

//for progress bar
        new Thread()
        {
            public void run()
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        //   Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                        if(msg.contains("Test"))
                        {
                            progressDialog.show();
                        }
                        else if(msg.contains("Hb = "))
                        {
                            progressDialog.dismiss();
                            try {
                                String hbvalue=msg;
                                hbvalue=hbvalue.replaceAll("[^0-9.]", "");

                                String device_name = sharedPreferences.getString("devicename", "NA");
                                String device_address = sharedPreferences.getString("device", "NA");
                                String device_id = device_name + "_" + device_address;
                                String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                                String time = new SimpleDateFormat("h:mm a").format(new Date());
                                databaseHelper.insertData(new TestDetailsModal(device_id, hbvalue, date, time));
                            }catch (Exception e)
                            {
                                showToast(e.toString());
                            }
                        }
                    }
                });
            }
        }.start();

        //for toast msg
        new Thread()
        {
            public void run()
            {
                MainActivity.this.runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
                        editTextdisplay.append(msg+"\n");
                        /*if(msg.contains("Test"))
                        {
                            progressDialog.show();
                        }
                        else if(msg.contains("Hb"))
                        {
                            progressDialog.dismiss();
                        }*/
                    }
                });
            }
        }.start();

    }

    @Override
    public void log(String message) {
//showToast(message);
        Log.d("msg: ",message);
    }

    @Override
    public void logError(String msg) {
        Log.d("Error: " , msg);
    }

    @Override
    public void setConnected(boolean connected) {

        mConnected = connected;
        buttonconnect.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshapeconnect2));
        buttonconnect.setText("Connected");
        textViewdevice.setText(deviceName);
    }

    @Override
    public void initializeTime() {
        mTimeInitialized = true;
    }

    @Override
    public void initializeEcho() {
        mEchoInitialized = true;
    }


    @Override
    public void disconnectGattServer() {
        if(progressDialog!=null)
        {
            progressDialog.dismiss();
        }

        mConnected = false;
        mEchoInitialized = false;
        mTimeInitialized = false;
        buttonconnect.setBackground(ContextCompat.getDrawable(getApplicationContext(),R.drawable.buttonshapeconnect1));
        textViewdevice.setText("NA");
        buttonconnect.setText("Connect");
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scan(View view) {
        if (!hasPermissions()|| mScanning)
        {
            return;
        }
        disconnectGattServer();
        scanprogressDialog.show();
        if(!sharedPreferences.getString("device","NA").equals("NA"))
        {
            //BluetoothDevice device=mBluetoothAdapter.getRemoteDevice(sharedPreferences.getString("device","NA"));
            //connectDevice(device);
            //deviceName=sharedPreferences.getString("devicename","NA");
        }


        mScanResults = new HashMap<>();
        mScanCallback = new BtleScanCallback(mScanResults);

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Note: Filtering does not work the same (or at all) on most devices. It also is unable to
        // search for a mask or anything less than a full UUID.
        // Unless the full UUID of the server is known, manual filtering may be necessary.
        // For example, when looking for a brand of device that contains a char sequence in the UUID
        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        // filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        mHandler = new Handler();
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        mScanning = true;

    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;

    }
    private void scanComplete() {
        if (mScanResults.isEmpty()) {
            return;
        }
        scanprogressDialog.dismiss();
        list_show();
    }
    public void list_show() {

deviceArrayList.clear();

        for (String deviceAddress : mScanResults.keySet()) {
            BluetoothDevice device = mScanResults.get(deviceAddress);
            if(device.getName()!=null)
                deviceArrayList.add(device.getName()+"\n"+device);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter(this,R.layout.text1,R.id.text1,deviceArrayList);
        adapter.notifyDataSetChanged();
        spinnerDevice.setAdapter(adapter);


      /*  AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.custom_alert_list, null);
        builder.setView(convertView);
        builder.setTitle("Scan Result");
        builder.setCancelable(false);
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ListView lv =  convertView.findViewById(R.id.list);
        ArrayAdapter<BluetoothDevice> adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,arrayList);
        lv.setAdapter(adapter);
        AlertDialog alertDialog=builder.create();
        alertDialog.show();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // showToast(arrayList.get(position));

                SharedPreferences.Editor editor=sharedPreferences.edit();
                editor.putString("device",arrayList.get(position)+"");
                editor.commit();
                connectDevice(arrayList.get(position));
                alertDialog.dismiss();
            }
        });*/
    }

    // Gatt connection

    private void connectDevice(BluetoothDevice device) {
        // log("Connecting to " + device.getAddress());
   //     BluetoothGattCallback bluetoothGattCallback=new GattClientCallback(this);

        GattClientCallback gattClientCallback = new GattClientCallback(this);
        mGatt = device.connectGatt(this, true, gattClientCallback);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestPermission();
            return false;
        }
        return true;
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

    }

    public void start(View view) {
        sendMessage("U371");

    }

    public void test(View view) {
        //sendMessage("ON");
        sendMessage("U401");
        sendMessage("U401");
    }
    public void readLastTest(View view) {
        sendMessage("U502");
    }

    public void connect(View view) {
        disconnectGattServer();
        if(deviceArrayList.size()!=0) {
            //  if(position>0) {
            String s=spinnerDevice.getSelectedItem().toString();
            BluetoothDevice device=mBluetoothAdapter.getRemoteDevice(s.substring(s.length()-17));
            connectDevice(device);
            showToast(device.getName()+"");
            deviceName=device.getName();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("device", s.substring(s.length()-17));
            editor.putString("devicename",device.getName());
            editor.commit();

            deviceArrayList.clear();
            ArrayAdapter<String> adapter = new ArrayAdapter(this,R.layout.text1,R.id.text1,deviceArrayList);
            adapter.notifyDataSetChanged();
            spinnerDevice.setAdapter(adapter);

            // }
        }
    }

    public void readBatchCode(View view) {
        sendMessage("U402");
    }

    public void writeBatchCode(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("Set Batch Code");

        final EditText input = new EditText(MainActivity.this);
        input.setHint("Batch Code");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String code=input.getText().toString();
                if(code.isEmpty())
                {
                    input.setError("Not Left Blank");
                }
                else
                {
                    sendMessage("U403"+code);
                }
              //  Toast.makeText(getApplicationContext(), "Text entered is " + input.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void device_off(View view) {
        sendMessage("U370");
        disconnectGattServer();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class BtleScanCallback extends ScanCallback {

        private Map<String, BluetoothDevice> mScanResults;

        BtleScanCallback(Map<String, BluetoothDevice> scanResults) {
            mScanResults = scanResults;
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            addScanResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                addScanResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            logError("BLE Scan Failed with code " + errorCode);
        }

        private void addScanResult(ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress, device);


        }
    }

    // Messaging

    private void sendMessage(String msg) {
        if (!mConnected || !mEchoInitialized) {
            showToast("Not Connected");
            return;
        }

        BluetoothGattCharacteristic characteristic = BluetoothUtils.findEchoCharacteristic(mGatt);
        if (characteristic == null) {
            logError("Unable to find echo characteristic.");
            disconnectGattServer();
            return;
        }

        byte[] messageBytes = StringUtils.bytesFromString(msg);
        if (messageBytes.length == 0) {
            logError("Unable to convert message to bytes");
            return;
        }

        characteristic.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(characteristic);
        if (success) {
            // log("Wrote: " + StringUtils.byteArrayInHexFormat(messageBytes));
            showToast("MSG SENT");

        } else {
            logError("Failed to write data");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id=item.getItemId();
        switch (id)
        {
            case R.id.power:
            {
                disconnectGattServer();
               // sendMessage("ON");
                /*sendMessage("OFF");
                sendMessage("OFF");*/

              /*  new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                       // sendMessage("OFF");
                    }
                }, 2000);*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                       // disconnectGattServer();
                    }
                }, 500);

                /*if (mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.disable();
                }*/
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //finish();
                    }
                }, 2000);

                break;
            }
            case R.id.exportcsv:
            {
                exportDB();
                break;
            }
            case R.id.testresult:
            {
                startActivity(new Intent(MainActivity.this,TestDetailsList.class));
                break;
            }
            case R.id.exit:
            {
                disconnectGattServer();
                finish();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    private void exportDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory()+File.separator+ "True HB");


        if (!exportDir.exists())
        {
            exportDir.mkdirs();
            //       Toast.makeText(getApplicationContext(),"Export folder created",Toast.LENGTH_LONG).show();
        }
        String timeStamp_date=new SimpleDateFormat("yyyyMMdd").format(new Date());
        File file = new File(exportDir, "TrueHb_"+timeStamp_date+"_.csv");

        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            Cursor curCSV=databaseHelper.getAllData();

            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={curCSV.getString(curCSV.getColumnIndex(DatabaseHelper.COL_DEVICE_ID))
                        ,curCSV.getString(curCSV.getColumnIndex(DatabaseHelper.COL_HB_RESULT))
                        ,curCSV.getString(curCSV.getColumnIndex(DatabaseHelper.COL_DATE))
                        ,curCSV.getString(curCSV.getColumnIndex(DatabaseHelper.COL_TIME))

                };
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            showToast("Csv exported");
        }
        catch(Exception sqlEx)
        {
             Toast.makeText(MainActivity.this,sqlEx.toString(),Toast.LENGTH_LONG).show();
            //  Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }
}
