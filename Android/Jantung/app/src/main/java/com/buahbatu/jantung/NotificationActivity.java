package com.buahbatu.jantung;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buahbatu.jantung.misc.ViewHolder;
import com.buahbatu.jantung.model.Notification;
import com.buahbatu.jantung.model.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationActivity extends AppCompatActivity {
    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;

    List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ButterKnife.bind(this);

        fragmentList = new ArrayList<>();
        fragmentList.add(new RequestFragment());
        fragmentList.add(new AlertFragment());
        final String[] names = {"Request", "Alert"};

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return names[position];
            }
        });

        tabLayout.setupWithViewPager(viewPager);
    }

    public static class RequestFragment extends Fragment{
        RecyclerView recyclerView;

        List<Request> requestList;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            recyclerView =  (RecyclerView) inflater.inflate(R.layout.fragment_recycler, container, false);

            // init4dp
            requestList = new ArrayList<>();
            requestList.add(new Request("user", "H101"));
            requestList.add(new Request("Alif", "H1200"));
            requestList.add(new Request("Akbar", "H1201"));
            requestList.add(new Request("user", "H100"));

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new RecyclerView.Adapter() {
                private static final int INCOMING = 801;
                private static final int OUTGOING = 467;

                @Override
                public int getItemViewType(int position) {
                    if (requestList.get(position).getUsername().equals("user")) // from self
                        return OUTGOING;
                    else
                        return INCOMING;
                }

                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.item_request, parent, false);
                    return new ViewHolder(view);
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    Request request = requestList.get(position);

                    TextView title = (TextView) holder.itemView.findViewById(R.id.item_title);
                    TextView detail = (TextView) holder.itemView.findViewById(R.id.item_detail);
                    if (getItemViewType(position)== OUTGOING) {
                        title.setText(getString(R.string.outgoing_message));
                        detail.setText(String.format(Locale.US, "%s %s.", getString(R.string.outgoing_message_confirm), request.getDeviceTarget()));

                        View acceptButton = holder.itemView.findViewById(R.id.button_accepts);
                        acceptButton.setVisibility(View.GONE);
                    }else {
                        title.setText(getString(R.string.incoming_message));
                        detail.setText(getString(R.string.incoming_message_confirm));
                        detail.setText(String.format(Locale.US, "%s %s.", getString(R.string.incoming_message_confirm), request.getUsername()));
                    }
                }

                @Override
                public int getItemCount() {
                    return requestList.size();
                }
            });
            return recyclerView;
        }
    }

    public static class AlertFragment extends Fragment{
        RecyclerView recyclerView;
        List<Notification> notificationList;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            recyclerView = (RecyclerView) inflater.inflate(R.layout.fragment_recycler, container, false);

            notificationList = new ArrayList<>();
            notificationList.add(new Notification("Akbar is fine",
                    "Nothing to worry in last 30 minutes", Notification.HEALTH));
            notificationList.add(new Notification("Masyithah is doing great",
                    "Nothing to worry in last 30 minutes", Notification.HEALTH));
            notificationList.add(new Notification("Dani got a PAC",
                    "Not serious, you should not worry", Notification.SICK));
            notificationList.add(new Notification("Fahmi got a Tycardhia",
                    "A bit dangerous, you should call him", Notification.DANGER));
            notificationList.add(new Notification("Dani got a PAC",
                    "Not serious, you should not worry", Notification.SICK));
            notificationList.add(new Notification("Nana is doing great",
                    "Nothing to worry in last 30 minutes", Notification.HEALTH));
            notificationList.add(new Notification("Fahmi just got a SVT",
                    "Very dangerous, you need to call him ASAP", Notification.DANGER));
            notificationList.add(new Notification("Rere is fine",
                    "Nothingto worry in last 30 minutes", Notification.HEALTH));

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new RecyclerView.Adapter() {
                @Override
                public int getItemViewType(int position) {
                    return notificationList.get(position).getCondition();
                }

                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(getContext()).inflate(R.layout.item_alert, parent, false);
                    return new ViewHolder(view);
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                    Notification notification = notificationList.get(position);

                    TextView title = (TextView) holder.itemView.findViewById(R.id.item_title);
                    TextView detail = (TextView) holder.itemView.findViewById(R.id.item_detail);
                    title.setText(notification.getTitle());
                    detail.setText(notification.getDetail());
                    if (getItemViewType(position) == Notification.HEALTH) {
                        ImageView imageAlert = (ImageView) holder.itemView.findViewById(R.id.item_image);
                        imageAlert.setImageResource(R.drawable.ic_error_green);
                    }else if (getItemViewType(position) == Notification.SICK){
                        ImageView imageAlert = (ImageView) holder.itemView.findViewById(R.id.item_image);
                        imageAlert.setImageResource(R.drawable.ic_error_yellow);
                    }else {
                        ImageView imageAlert = (ImageView) holder.itemView.findViewById(R.id.item_image);
                        imageAlert.setImageResource(R.drawable.ic_error_red);
                    }
                }

                @Override
                public int getItemCount() {
                    return notificationList.size();
                }
            });
            return recyclerView;
        }
    }
}
