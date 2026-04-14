package com.easyotp;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BluetoothManagerFragment extends Fragment implements BluetoothDeviceAdapter.DeviceActionListener {

    private TextView tvBluetoothStatus;
    private TextView tvEmpty;
    private RecyclerView recyclerBluetoothDevices;
    private BluetoothDeviceAdapter adapter;
    private Button btnRefreshDevices;

    private BluetoothAdapter bluetoothAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_manager, container, false);
        tvBluetoothStatus = view.findViewById(R.id.tvBluetoothStatus);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        recyclerBluetoothDevices = view.findViewById(R.id.recyclerBluetoothDevices);
        btnRefreshDevices = view.findViewById(R.id.btnRefreshDevices);

        recyclerBluetoothDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new BluetoothDeviceAdapter(new ArrayList<>(), this);
        recyclerBluetoothDevices.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        updateStatus();
        loadDevices();

        btnRefreshDevices.setOnClickListener(v -> loadDevices());
        return view;
    }

    private void updateStatus() {
        if (bluetoothAdapter == null) {
            tvBluetoothStatus.setText("Bluetooth not supported");
            btnRefreshDevices.setEnabled(false);
        } else if (!bluetoothAdapter.isEnabled()) {
            tvBluetoothStatus.setText("Bluetooth is disabled. Please enable Bluetooth on your device.");
        } else {
            tvBluetoothStatus.setText("Bluetooth is enabled. Manage paired devices below.");
        }
    }

    private void loadDevices() {
        if (bluetoothAdapter == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Bluetooth unavailable");
            adapter = new BluetoothDeviceAdapter(new ArrayList<>(), this);
            recyclerBluetoothDevices.setAdapter(adapter);
            return;
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Bluetooth permission not granted");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        List<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
        adapter = new BluetoothDeviceAdapter(deviceList, this);
        recyclerBluetoothDevices.setAdapter(adapter);
        tvEmpty.setVisibility(deviceList.isEmpty() ? View.VISIBLE : View.GONE);
        if (deviceList.isEmpty()) {
            tvEmpty.setText("No paired devices found");
        }
    }

    @Override
    public void onDeviceAction(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            unpairDevice(device);
        } else {
            pairDevice(device);
        }
    }

    private void pairDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Bluetooth permission required", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean result = device.createBond();
        Toast.makeText(requireContext(), result ? "Pairing started" : "Pairing failed", Toast.LENGTH_SHORT).show();
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method removeBond = device.getClass().getMethod("removeBond");
            boolean result = (boolean) removeBond.invoke(device);
            Toast.makeText(requireContext(), result ? "Unpairing started" : "Unpairing failed", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Unable to unpair device", Toast.LENGTH_SHORT).show();
        }
    }
}
