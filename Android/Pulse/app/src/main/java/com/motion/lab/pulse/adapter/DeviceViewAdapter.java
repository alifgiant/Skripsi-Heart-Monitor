package com.motion.lab.pulse.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.ionicons_typeface_library.Ionicons;
import com.motion.lab.pulse.R;
import com.motion.lab.pulse.model.PulseDevice;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
        PulseDevice deviceData;

        @BindView(R.id.device_condition_image) ImageView mDeviceConditions;
        @BindView(R.id.device_quick_result) LineChart mQuickResult;
        @BindView(R.id.device_username) TextView mDeviceName;
        @BindView(R.id.device_heartbeat) TextView mDeviceBeat;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mDeviceConditions.setImageDrawable(context.getResources().getDrawable(R.drawable.boy0));

            ((ImageView)itemView.findViewById(R.id.device_edit_button))
                    .setImageDrawable(new IconicsDrawable(context)
                            .icon(Ionicons.Icon.ion_edit));
            ((ImageView)itemView.findViewById(R.id.love_img))
                    .setImageDrawable(new IconicsDrawable(context)
                            .icon(Ionicons.Icon.ion_ios_heart_outline));
            mQuickResult.setNoDataText("No data transmitted yet");

            mQuickResult.setData(new LineData());
        }

        public void addData(float v, String name){
            LineData data = mQuickResult.getData();
            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = new LineDataSet(null, name);
                set.setAxisDependency(YAxis.AxisDependency.LEFT);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(v, set.getEntryCount()), 0);

            // let the chart know it's data has changed
            mQuickResult.notifyDataSetChanged();

//            // limit the number of visible entries
//            mQuickResult.setVisibleXRangeMaximum(120);

            // move to the latest entry
            mQuickResult.moveViewToX(data.getXValCount());
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
        deviceList.get(position).setViewHolder(holder);
//        holder.mDeviceLastCheck.setText("30 S");
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
