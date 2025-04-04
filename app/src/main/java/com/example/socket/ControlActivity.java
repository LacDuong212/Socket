package com.example.socket;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class ControlActivity extends AppCompatActivity {

    // SPP UUID
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    // UI Components
    private ImageButton btnDevice1, btnDevice2, btnDisconnect;
    private TextView txtStatus, txtMacAddress;

    // Bluetooth Components
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private boolean isBtConnected = false;
    private String deviceAddress;

    // Device State
    private int device1State = 0;
    private int device2State = 0;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        // Get device address from intent
        Intent intent = getIntent();
        deviceAddress = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);

        // Initialize views
        initViews();

        // Check and request Bluetooth permissions
        if (checkBluetoothPermissions()) {
            new ConnectBluetoothTask().execute();
        }
    }

    private void initViews() {
        btnDevice1 = findViewById(R.id.btnTb1);
        btnDevice2 = findViewById(R.id.btnTb2);
        txtStatus = findViewById(R.id.textV1);
        txtMacAddress = findViewById(R.id.textViewMAC);
        btnDisconnect = findViewById(R.id.btnDisc);

        btnDevice1.setOnClickListener(v -> toggleDevice1());
        btnDevice2.setOnClickListener(v -> toggleDevice2());
        btnDisconnect.setOnClickListener(v -> disconnect());
    }

    private boolean checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new ConnectBluetoothTask().execute();
            } else {
                showToast("Bluetooth permission required");
                finish();
            }
        }
    }

    private void toggleDevice1() {
        if (bluetoothSocket != null && isBtConnected) {
            try {
                if (device1State == 0) {
                    // Turn device 1 ON
                    device1State = 1;
                    btnDevice1.setBackgroundResource(R.drawable.tbon);
                    bluetoothSocket.getOutputStream().write("1".getBytes());
                    txtStatus.setText("Thiết bị số 1 đang bật");
                } else {
                    // Turn device 1 OFF
                    device1State = 0;
                    btnDevice1.setBackgroundResource(R.drawable.tboff);
                    bluetoothSocket.getOutputStream().write("A".getBytes());
                    txtStatus.setText("Thiết bị số 1 đang tắt");
                }
            } catch (IOException e) {
                showToast("Lỗi gửi lệnh");
            }
        }
    }

    private void toggleDevice2() {
        if (bluetoothSocket != null && isBtConnected) {
            try {
                if (device2State == 0) {
                    // Turn device 2 ON
                    device2State = 1;
                    btnDevice2.setBackgroundResource(R.drawable.tbon);
                    bluetoothSocket.getOutputStream().write("7".getBytes());
                    txtStatus.setText("Thiết bị số 2 đang bật");
                } else {
                    // Turn device 2 OFF
                    device2State = 0;
                    btnDevice2.setBackgroundResource(R.drawable.tboff);
                    bluetoothSocket.getOutputStream().write("G".getBytes());
                    txtStatus.setText("Thiết bị số 2 đang tắt");
                }
            } catch (IOException e) {
                showToast("Lỗi gửi lệnh");
            }
        }
    }

    private void disconnect() {
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            showToast("Lỗi khi ngắt kết nối");
        }
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class ConnectBluetoothTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ControlActivity.this,
                    "Đang kết nối...", "Vui lòng đợi...", true);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                if (bluetoothSocket == null || !isBtConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

                    if (ActivityCompat.checkSelfPermission(ControlActivity.this,
                            Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
                        bluetoothAdapter.cancelDiscovery();
                        bluetoothSocket.connect();
                        return true;
                    }
                }
            } catch (IOException e) {
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressDialog.dismiss();

            if (success) {
                isBtConnected = true;
                showToast("Kết nối thành công");
                showPairedDevices();
            } else {
                showToast("Kết nối thất bại");
                finish();
            }
        }

        private void showPairedDevices() {
            if (ActivityCompat.checkSelfPermission(ControlActivity.this,
                    Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                if (pairedDevices != null && !pairedDevices.isEmpty()) {
                    for (BluetoothDevice device : pairedDevices) {
                        txtMacAddress.setText(device.getName() + " - " + device.getAddress());
                    }
                } else {
                    showToast("Không tìm thấy thiết bị đã kết nối");
                }
            }
        }
    }
}