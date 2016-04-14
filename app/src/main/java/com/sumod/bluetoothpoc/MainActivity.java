package com.sumod.bluetoothpoc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter = null;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private static final String MY_BT_NAME = "OnRoute";
    private static final UUID MY_BT_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private AcceptThread acceptThread;

    @ViewById(R.id.linearlayout_main)
    LinearLayout linearLayout_main;

    @ViewById(R.id.device_name)
    TextView deviceName;

    @ViewById(R.id.device_address)
    TextView deviceAddress;

    @AfterViews
    protected void afterViews(){
        linearLayout_main.setVisibility(View.GONE);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        acceptThread = new AcceptThread();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {

            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        //Setting the device discoverable for '0' seconds makes it always discoverable
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);


        // Register the BroadcastReceiver
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver, filter1);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mReceiver, filter2);
        // Don't forget to unregister during onDestroy

//        acceptThread.start();
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                /*try {
                    device.getClass().getMethod("setPairingConfirmation", boolean.class).invoke(device, true);
                    device.getClass().getMethod("cancelPairingUserInput").invoke(device);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }*/

                /*// Display the device name and MAC address
                linearLayout_main.setVisibility(View.VISIBLE);
                deviceName.setText(device.getName());
                deviceAddress.setText(device.getAddress());*/
            }

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){

                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Display the device name and MAC address
                linearLayout_main.setVisibility(View.VISIBLE);
                deviceName.setText(device.getName());
                deviceAddress.setText(device.getAddress());
            }
        }
    };


    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_BT_NAME, MY_BT_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    manageConnectedSocket(socket);
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    protected synchronized void manageConnectedSocket(BluetoothSocket socket){
        BluetoothDevice clientDevice = socket.getRemoteDevice();

        linearLayout_main.setVisibility(View.VISIBLE);
        deviceName.setText(clientDevice.getName());
        deviceAddress.setText(clientDevice.getAddress());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
