package org.hopto.pipe.ble_motor;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 2;
    private BluetoothManager mBluetoothManager;
    private Handler mHandler;
    private ArrayList<String> deviceName;
    private ArrayList<BluetoothDevice> mBluetoothDevices=new ArrayList<BluetoothDevice>();
    private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    private ListAdapter adapter;
    private BluetoothLeScanner scanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listview = (ListView) findViewById(R.id.list);
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        deviceName=new ArrayList<String>();
        adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,deviceName) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.WHITE);
                return view;
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {

                }
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_CODE_ACCESS_COARSE_LOCATION);
            }
        }
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(onClickListView);
        mHandler=new Handler();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
    }
    public void startScanning(){
        deviceName.clear();
        mBluetoothDevices.clear();
        ((BaseAdapter)adapter).notifyDataSetChanged();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(new MyScanCallback());

    }
    public void stopScanning()
    {
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
        scanner.stopScan(new MyScanCallback());

    }
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            Toast.makeText(MainActivity.this,"點選第 "+(position +1) +" 個 \n內容："+parent.getAdapter().getItem(position), Toast.LENGTH_SHORT).show();
            final BluetoothDevice mBluetoothDevice=mBluetoothDevices.get(position);
            Log.d("device choose",mBluetoothDevice.toString());
            Intent goControlIntent=new Intent(MainActivity.this,control.class);
            //將device Name與address存到ControlActivity的DEVICE_NAME與ADDRESS，以供ControlActivity使用
            goControlIntent.putExtra("name",mBluetoothDevice.getName());
            goControlIntent.putExtra("address",mBluetoothDevice.getAddress());
            stopScanning();
            startActivity(goControlIntent);
        }
    };
    public void scan_click(View view){
        Toast.makeText(MainActivity.this,"Begin scan", Toast.LENGTH_SHORT).show();
        startScanning();
    }
    public void stop_click(View view){
        Toast.makeText(MainActivity.this,"Stop scan", Toast.LENGTH_SHORT).show();
        stopScanning();

    }
    @Override
    protected void onResume() {
        super.onResume();
        if(!mBluetoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_ENABLE_BT);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(REQUEST_ENABLE_BT==2 && resultCode== Activity.RESULT_CANCELED){
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

//    private ScanCallback scanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult scanResult) {
//            BluetoothDevice device = scanResult.getDevice();
//            Log.d("msg",  device.toString());
//            if(!mBluetoothDevices.contains(device)) return;
//            mBluetoothDevices.add(device);
//            String deviceInfo = device.getName() + " - " + device.getAddress();
//            ScanRecord scanRecord = scanResult.getScanRecord();
//            List<ParcelUuid> uuids = scanRecord.getServiceUuids();
//
//            if(uuids != null) {
//                for(int i = 0; i < uuids.size(); i++) {
//                    deviceInfo += "\n" + uuids.get(i).toString();
//                }
//            }
//
//            final String text = deviceInfo;
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                   deviceName.add(text);
//                    ((BaseAdapter)adapter).notifyDataSetChanged();
//                }
//            });
//        }
//
//        @Override
//        public void onScanFailed(int i) {
//            Log.e("error", "Scan attempt failed");
//        }
//    };

    //    private void scanfuction(boolean enable)
//    {
//        if(enable)
//        {
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    scanner.stopScan (scanCallback );
//                    mScanning=false;
//                }
//            },SCAN_TIME);
//            mScanning=true;
//            scanner.startScan (scanCallback);
//        }
//        else
//        {
//            mScanning=false;
//           scanner.stopScan (scanCallback );
//
//        }
//    }
    public class MyScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, final ScanResult result) {

            BluetoothDevice device = result.getDevice();
            if(mBluetoothDevices.contains(device))
                return;
            mBluetoothDevices.add(device);
            String deviceInfo = device.getName() + " - " + device.getAddress();
            ScanRecord scanRecord = result.getScanRecord();
            List<ParcelUuid> uuids = scanRecord.getServiceUuids();

            if(uuids != null) {
                for(int i = 0; i < uuids.size(); i++) {
                    deviceInfo += "\n" + uuids.get(i).toString();
                }
            }
            final String text = deviceInfo;
            Log.d("res",text );
            deviceName.add(text);
            ((BaseAdapter)adapter).notifyDataSetChanged();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //Do something with batch of results
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d("error","Cannot scan");
            Toast.makeText(MainActivity.this,"Cannot scan", Toast.LENGTH_SHORT).show();
        }
    }

}
