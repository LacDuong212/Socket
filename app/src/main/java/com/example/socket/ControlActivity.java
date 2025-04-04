package com.example.socket;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    // SPP UUID
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Bluetooth UI Components
    ImageButton btnDevice1, btnDevice2, btnDisconnect;
    TextView txtStatus, txtMacAddress;
    // Bluetooth Core Components
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket bluetoothSocket = null;
    private boolean isBluetoothConnected = false;
    // Device Management
    private String deviceAddress = null;
    // UI Feedback
    private ProgressDialog connectionProgressDialog;
    // Device Control Flags
    private int lamp1StatusFlag = 0;
    private int lamp2StatusFlag = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get device address from intent
        Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        setContentView(R.layout.activity_control);

        // Initialize views
        btnDevice1 = findViewById(R.id.btnTb1);
        btnDevice2 = findViewById(R.id.btnTb2);
        txtStatus = findViewById(R.id.textV1);
        txtMacAddress = findViewById(R.id.textViewMAC);
        btnDisconnect = findViewById(R.id.btnDisc);

        // Show MAC address
        txtMacAddress.setText(deviceAddress);

        // Connect to Bluetooth device
        new ConnectBT().execute();

        btnDevice1.setOnClickListener(v -> thietTbi1());
        btnDevice2.setOnClickListener(v -> thietTbi7());
        btnDisconnect.setOnClickListener(v -> Disconnect());
    }

    private void Disconnect() {
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                msg("Lỗi khi ngắt kết nối.");
            }
        }
        finish();
    }

    private void thietTbi1() {
        if (bluetoothSocket != null) {
            try {
                if (lamp1StatusFlag == 0) {
                    lamp1StatusFlag = 1;
                    btnDevice1.setBackgroundResource(R.drawable.tblon);
                    bluetoothSocket.getOutputStream().write("1".getBytes());
                    txtStatus.setText("Thiết bị số 1 đang bật");
                } else {
                    lamp1StatusFlag = 0;
                    btnDevice1.setBackgroundResource(R.drawable.btnotconnect);
                    bluetoothSocket.getOutputStream().write("0".getBytes());
                    txtStatus.setText("Thiết bị số 1 đang tắt");
                }
            } catch (IOException e) {
                msg("Lỗi điều khiển thiết bị 1.");
            }
        }
    }

    private void thietTbi7() {
        if (bluetoothSocket != null) {
            try {
                if (lamp2StatusFlag == 0) {
                    lamp2StatusFlag = 1;
                    btnDevice2.setBackgroundResource(R.drawable.tblon);
                    bluetoothSocket.getOutputStream().write("7".getBytes());
                    txtStatus.setText("Thiết bị số 7 đang bật");
                } else {
                    lamp2StatusFlag = 0;
                    btnDevice2.setBackgroundResource(R.drawable.btnotconnect);
                    bluetoothSocket.getOutputStream().write("G".getBytes());
                    txtStatus.setText("Thiết bị số 7 đang tắt");
                }
            } catch (IOException e) {
                msg("Lỗi điều khiển thiết bị 7.");
            }
        }
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            connectionProgressDialog = ProgressDialog.show(ControlActivity.this, "Đang kết nối...", "Xin vui lòng đợi!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (bluetoothSocket == null || !isBluetoothConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (connectionProgressDialog != null && connectionProgressDialog.isShowing()) {
                connectionProgressDialog.dismiss();
            }

            if (!ConnectSuccess) {
                msg("Kết nối thất bại! Kiểm tra thiết bị.");
                finish();
            } else {
                msg("Kết nối thành công.");
                isBluetoothConnected = true;
            }
        }
    }
}
