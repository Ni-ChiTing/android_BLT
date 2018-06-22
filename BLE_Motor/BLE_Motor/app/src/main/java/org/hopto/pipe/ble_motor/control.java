package org.hopto.pipe.ble_motor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class control extends AppCompatActivity {
    private String name;
    private String address;
    private  BluetoothGatt mGatt =  null ;
    private BluetoothDevice device;
    private BluetoothAdapter adapter;
    private ArrayList< BluetoothGattService> service_list=new ArrayList< BluetoothGattService>();
    private int state;
    private ListAdapter listadapter;
    private ArrayList<String> serviceName;
    private boolean done;
    private Handler mHandler ;
    private ListView listview;
     private TextInputLayout Input;
     private Button returnlist;
     private Button send;
     private EditText textfield;
     private BluetoothGattCharacteristic characteristic=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        listview= (ListView) findViewById(R.id.list1);
        Intent intent = getIntent();
        name=intent.getStringExtra("name");
        address=intent.getStringExtra("address");
        Input=( TextInputLayout) findViewById(R.id.motorinput);
        returnlist=(Button) findViewById(R.id.re);
        send=(Button)findViewById(R.id.send);
        textfield=(EditText) findViewById(R.id.text_dis);
        Input.setVisibility(View.INVISIBLE);
        returnlist.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);
        textfield.setVisibility(View.INVISIBLE);
        textfield.setKeyListener(null);
        Log.d("name",name);
        Log.d("address",address);
        serviceName=new ArrayList<String>();
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        adapter = bluetoothManager.getAdapter();
        listadapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,serviceName);
        device=adapter.getRemoteDevice(address);
        listview.setAdapter(listadapter);
        listview.setOnItemClickListener(onClickListView);
        done=false;
        connect_BLE();
       mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case 1:
                        ((BaseAdapter)listadapter).notifyDataSetChanged();
                        break;
                    case 2:
                        textfield.append(msg.getData().getString("text"));
                        break;
                }
            }
        };
    }
    private void connect_BLE() {
        ParcelUuid[] uuids = (ParcelUuid[]) device.getUuids();
        mGatt = device.connectGatt( this ,  true , mGattCallback);
        textfield.setText("");
        Log.d("a",mGatt.toString());
    }
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // Toast 快顯功能 第三個參數 Toast.LENGTH_SHORT 2秒  LENGTH_LONG 5秒
            Toast.makeText(control.this,"點選第 "+(position +1) +" 個 \n內容："+parent.getAdapter().getItem(position), Toast.LENGTH_SHORT).show();
            characteristic=service_list.get(position).getCharacteristics().get(0);
            mGatt.setCharacteristicNotification( characteristic,true);
            mGatt.readCharacteristic(characteristic);
            Input.setVisibility(View.VISIBLE);
            returnlist.setVisibility(View.VISIBLE);
            send.setVisibility(View.VISIBLE);
            textfield.setVisibility(View.VISIBLE);
            listview.setVisibility(View.INVISIBLE);
        }
    };
    public void reclick(View view)
    {
        textfield.setText("");
        mGatt.setCharacteristicNotification( characteristic,false);
        Input.setVisibility(View.INVISIBLE);
        returnlist.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);
        textfield.setVisibility(View.INVISIBLE);
        listview.setVisibility(View.VISIBLE);
    }
    public void sendclick(View view) {
        byte[] send1=null;
        try{
            send1 = Input.getEditText().getText().toString().getBytes("UTF-8");
        }catch (Exception e)
        {

        }

        if(send1!=null) {
            Log.e("dataSend", new String(send1));
            characteristic.setValue(send1);
            boolean status = mGatt.writeCharacteristic(characteristic);
            Log.e("dataSend", status + "");
            mGatt.writeCharacteristic(characteristic);
        }
    }

//    private BluetoothGattCallback mGattCallback =  new  BluetoothGattCallback() {
//        @Override
//        public void  onConnectionStateChange(BluetoothGatt gatt,  int  status,  int  newState) {
//            super .onConnectionStateChange(gatt, status, newState);
//            Log.v("TAG",  "Connection State Changed: "  + (newState == BluetoothProfile.STATE_CONNECTED ?  "Connected"  :  "Disconnected" ));
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                state=1;
//            } else {
//                state=0;
//
//            }
//        }
//
//    };
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            Log.i("t", "Connected to GATT server.");
            // Attempts to discover services after successful connection.
            Log.i("t", "Attempting to start service discovery:" + gatt.discoverServices());
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            Log.i("t", "Disconnected from GATT server.");

            String address = gatt.getDevice().getAddress();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            boolean isGood = true;
            for (int i = 0; i < gatt.getServices().size(); i++) {
                BluetoothGattService bgs = gatt.getServices().get(i);
                Log.w("t", "found service " + bgs.getUuid().toString());
                Log.w("t", bgs.getCharacteristics().toString());
                service_list.add(bgs);
                String text = "found service " + Integer.toString(i + 1) + "\n" + bgs.getUuid().toString();
                serviceName.add(text);
                if (bgs.getCharacteristics().size() == 0)
                    isGood = false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = new Message();
            msg.what = 1;
            mHandler.sendMessage(msg);
        } else {
            Log.w("t", "onServicesDiscovered received: " + status);
        }
        if(characteristic!=null)
        mGatt.setCharacteristicNotification( characteristic,true);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic, int status) {
        Log.e("onCharacteristicRead中", "recieve" + new String(characteristic.getValue()));
        Message msg = new Message();
        msg.what = 2;
        Bundle b=new Bundle();
        b.putString("text","now speed  "+new String(characteristic.getValue())+" (rmp) "+"\n");
        msg.setData(b);
        mHandler.sendMessage(msg);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic, int status) {//发送数据时调用
        if (status == BluetoothGatt.GATT_SUCCESS) {//写入成功
            Message msg = new Message();
            msg.what = 2;
            Bundle b=new Bundle();
            b.putString("text","寫入成功\n");
            msg.setData(b);
            mHandler.sendMessage(msg);
        } else if (status == BluetoothGatt.GATT_FAILURE) {
            Log.e("onCharacteristicWrite中", "fail");
        } else if (status == BluetoothGatt.GATT_WRITE_NOT_PERMITTED) {
            Log.e("onCharacteristicWrite中", "no permission");
        }

    }
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {// Characteristic 改变，数据接收会调用
        Log.e("onCharacteristicChange中", "recieve" + new String(characteristic.getValue()));
        Message msg = new Message();
        msg.what = 2;
        Bundle b=new Bundle();
        b.putString("text","now speed  "+new String(characteristic.getValue())+" (rmp) "+"\n");
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
};





}
