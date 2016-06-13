package com.motion.lab.pulse.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.ionicons_typeface_library.Ionicons;
import com.motion.lab.pulse.R;
import com.motion.lab.pulse.model.PulseDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maaakbar on 4/28/16.
 */
public class DeviceViewAdapter extends RecyclerView.Adapter<DeviceViewAdapter.ViewHolder> {
    private Context context;
    private ArrayList<PulseDevice> deviceList;

    public DeviceViewAdapter(Context context, ArrayList<PulseDevice> deviceList) {
        this.context = context;
        this.deviceList = deviceList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView mDeviceConditions;
        PulseDevice deviceData;

        TextView mDeviceName;
        TextView mDeviceBeat;

        View mQuickResult;

        public ViewHolder(View itemView) {
            super(itemView);
            mQuickResult = itemView.findViewById(R.id.device_quick_result);
            mDeviceConditions = (ImageView)itemView.findViewById(R.id.device_condition_image);
            mDeviceConditions.setImageDrawable(context.getResources().getDrawable(R.drawable.boy0));

            mDeviceName = (TextView) itemView.findViewById(R.id.device_username);
            mDeviceBeat = (TextView) itemView.findViewById(R.id.device_heartbeat);
            ((ImageView)itemView.findViewById(R.id.device_edit_button))
                    .setImageDrawable(new IconicsDrawable(context)
                            .icon(Ionicons.Icon.ion_edit));
            ((ImageView)itemView.findViewById(R.id.love_img))
                    .setImageDrawable(new IconicsDrawable(context)
                            .icon(Ionicons.Icon.ion_ios_heart_outline));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.deviceData = deviceList.get(position);
        holder.mDeviceName.setText(holder.deviceData.getName());
        holder.mDeviceBeat.setText(String.valueOf(holder.deviceData.getHeartBeat()));
//        holder.mDeviceLastCheck.setText("30 S");
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
