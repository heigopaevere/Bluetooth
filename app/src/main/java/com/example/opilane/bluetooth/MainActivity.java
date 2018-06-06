package com.example.opilane.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.security.PublicKey;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static  final String TAG = "MainActivity";
    BluetoothAdapter mBluetoothAdapter;
    ListView IvNewDevices;
    Button btnEnableDisable_Discoverable;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        mBluetoothAdapter.ERROR);
                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG,"onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG,"mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG,"mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG,"mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.i(TAG,"mBroadcastreceiver2: Discovering enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.i(TAG,"mBroadcastreceiver2: Disoverability enabled. " +
                                "Able to receive connections. ");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.i(TAG,"mBroadcastreceiver2: Discoverinilirt disabled. " +
                            "Not able to receive connections. ");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.i(TAG,"mBroadcastreceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.i(TAG,"mBroadcastreceiver2: connected");
                        break;
                }
            }
        }
    };
    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.i(TAG,"onreceive: ACTION FOUND");
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.i(TAG,"onreceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view,
                        mBTDevices);
                IvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.i(TAG,"ondestory: called. ");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnOnOFF = findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = findViewById(R.id.btnDiscoverable_on_off);
        IvNewDevices = findViewById(R.id.IvNewDevices);
        mBTDevices = new ArrayList<>();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null){
            Toast.makeText(MainActivity.this, "No Bluetooth adapter found",
                    Toast.LENGTH_SHORT).show();
        }
        btnOnOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"onClick; enabling/disabling bluetooth");
                enableDisableBT();
            }
        });

    }
    public void enableDisableBT(){
        if (mBluetoothAdapter == null){
            Log.i(TAG,"enableDisableBT; Does not have Bluetooth capabilities. ");
        }
        if (!mBluetoothAdapter.isEnabled()){
            Log.i(TAG,"enableDisableBT: enabling bluetooth. ");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if (mBluetoothAdapter.isEnabled()){
            Log.i(TAG,"enableDisableBT: disabling bluetooth");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
    public void btnEnableDisable_Discoverable(View view){
        Log.i(TAG, "btnEnableDisable_Discoverable: making device discoverable for 300 seconds");

        Intent discoverableIntent = new Intent (BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }
    public void btnDiscover(View view){
        Log.i(TAG,"btnDiscover: Looking for unpaired devices. ");

        if (mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.i(TAG,"btnDiscover: Canceling discovery");
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if (!mBluetoothAdapter.isDiscovering()){
            chechBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
    }
    private void checkBTPermissions(){
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionsCheck = this.checkSelfPermission("Manifest.permission.ACCESS.FINE.LOCATION");
            permissionsCheck += this.checkSelfPermission("Manifest.permission.ACCESS.FINE.LOCATION");
            if (permissionsCheck !=0){
                this.requestPermissions(new String[](Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 1001);
            }
            else {
                Log.i(TAG,"checkBTpermissions: No need to check permissions. " +
                        "SDK version < LOLLIPOP");
            }
        }
    }
}
