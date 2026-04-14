package com.easyotp;

import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {

    public interface DeviceActionListener {
        void onDeviceAction(BluetoothDevice device);
    }

    private final List<BluetoothDevice> devices;
    private final DeviceActionListener listener;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices, DeviceActionListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bluetooth_device, parent, false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.name.setText(device.getName() != null ? device.getName() : "Unknown device");
        holder.address.setText(device.getAddress());
        holder.action.setText(device.getBondState() == BluetoothDevice.BOND_BONDED ? "Unpair" : "Pair");
        holder.action.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeviceAction(device);
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView address;
        Button action;

        DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvDeviceName);
            address = itemView.findViewById(R.id.tvDeviceAddress);
            action = itemView.findViewById(R.id.btnDeviceAction);
        }
    }
}
