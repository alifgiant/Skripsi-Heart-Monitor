package com.buahbatu.jantung;

import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.pager_indicator) CircleIndicator pagerIndicator;
    @BindView(R.id.button_next) ImageView nextButton;

    DataFragment dataFragment;

    @OnClick(R.id.button_next) void onNextClick(){
        int position = viewPager.getCurrentItem();
        if (position < fragmentList.size()-1){
            viewPager.setCurrentItem(position+1, true);
        }else {
            TextInputLayout[] inputLayouts = {
                    dataFragment.textUserName,
                    dataFragment.textUserPassword,
                    dataFragment.textUserFullName,
                    dataFragment.textUserAddress,
                    dataFragment.textUserPhone,
                    dataFragment.textUserEmergencyPhone,
                    dataFragment.textUserAge,
                    dataFragment.textUserDeviceId
            };
            boolean anyEmpty = false;
            for (TextInputLayout inputLayout: inputLayouts) {
                inputLayout.setError(null);
                if (TextUtils.isEmpty(inputLayout.getEditText().getText())){
                    inputLayout.setError(getString(R.string.error_form));
                    anyEmpty = true;
                }
            }
            if (!anyEmpty) {
                JSONObject patient = new JSONObject();
                try {
                    patient.put("username", dataFragment.textUserName.getEditText().getText().toString())
                            .put("password", dataFragment.textUserPassword.getEditText().getText().toString())
                            .put("full_name", dataFragment.textUserFullName.getEditText().getText().toString())
                            .put("address", dataFragment.textUserAddress.getEditText().getText().toString())
                            .put("my_phone", dataFragment.textUserPhone.getEditText().getText().toString())
                            .put("emergency_phone", dataFragment.textUserEmergencyPhone.getEditText().getText().toString())
                            .put("age", dataFragment.textUserAge.getEditText().getText().toString())
                            .put("is_male", dataFragment.spinnerUserGender.getSelectedItem().toString().equals("Male"))
                            .put("device_id", dataFragment.textUserDeviceId.getEditText().getText().toString());

                    AppSetting.showProgressDialog(RegisterActivity.this, "Registering");
                    AndroidNetworking.post(AppSetting.getHttpAddress(RegisterActivity.this)
                            +"/{user}"+getString(R.string.register_url))
                            .addPathParameter("user", "patient")
                            .addJSONObjectBody(patient)
                            .setPriority(Priority.MEDIUM)
                            .build()
                            .getAsJSONObject(new JSONObjectRequestListener() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    AppSetting.dismissProgressDialog();
                                    // do anything with response
                                    Log.i("REGISTER", "onResponse: "+response.toString());
                                    Toast.makeText(RegisterActivity.this, "Register Success", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                                @Override
                                public void onError(ANError error) {
                                    dataFragment.textUserName.setError(null);
                                    dataFragment.textUserDeviceId.setError(null);

                                    AppSetting.dismissProgressDialog();
                                    switch (error.getErrorCode()){
                                        case 401: dataFragment.textUserName.setError("Username already exist");
                                            break;
                                        case 422: dataFragment.textUserDeviceId.setError("Device not registered");
                                            break;
                                    }
                                    // handle error
                                    Log.i("REGISTER", "onError: "+ error.getErrorCode());
                                    Log.i("REGISTER", "onError: "+ error.getErrorDetail());
                                    Log.i("REGISTER", "onError: "+ error.getErrorBody());
                                }
                            });
                }catch (JSONException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        dataFragment = new DataFragment();

        fragmentList = new ArrayList<>();
        fragmentList.add(TutorialFragment.newInstance(R.drawable.boy0, R.string.tutor1));
        fragmentList.add(TutorialFragment.newInstance(R.drawable.boy1, R.string.tutor2));
        fragmentList.add(TutorialFragment.newInstance(R.drawable.boy2, R.string.tutor3));
        fragmentList.add(TutorialFragment.newInstance(R.drawable.girl0, R.string.tutor4));
        fragmentList.add(dataFragment);

        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return fragmentList.size();
            }
        });
        pagerIndicator.setViewPager(viewPager);

        viewPager.addOnPageChangeListener(pageChangeListener);
    }

    ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position < fragmentList.size()-1)
                nextButton.setImageResource(R.drawable.ic_action_next);
            else
                nextButton.setImageResource(R.drawable.ic_action_done);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    public static class DataFragment extends Fragment{
        @BindView(R.id.text_user_name) TextInputLayout textUserName;
        @BindView(R.id.text_user_password) TextInputLayout textUserPassword;
        @BindView(R.id.text_full_name) TextInputLayout textUserFullName;
        @BindView(R.id.text_address) TextInputLayout textUserAddress;
        @BindView(R.id.text_user_phone) TextInputLayout textUserPhone;
        @BindView(R.id.text_emergency_phone) TextInputLayout textUserEmergencyPhone;
        @BindView(R.id.text_user_age) TextInputLayout textUserAge;
        @BindView(R.id.spinner_gender) Spinner spinnerUserGender;
        @BindView(R.id.text_device_id) TextInputLayout textUserDeviceId;
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_data, container, false);
            TextView textView = (TextView) view.findViewById(R.id.terms_check);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.terms_check)));
            ButterKnife.bind(this, view);
            return view;
        }
    }

    public static class TutorialFragment extends Fragment{
        private static final String IMAGE_KEY = "image";
        private static final String STRING_KEY = "string";

        public static TutorialFragment newInstance(int imageResId, int stringResId){
            TutorialFragment fragment = new TutorialFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(IMAGE_KEY, imageResId);
            bundle.putInt(STRING_KEY, stringResId);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tutorial, container, false);
            ImageView imageTutor = (ImageView) view.findViewById(R.id.image_tutorial);
            TextView textTutor = (TextView) view.findViewById(R.id.text_tutorial);

            Bundle bundle = getArguments();

            imageTutor.setImageResource(bundle.getInt(IMAGE_KEY));
            textTutor.setText(bundle.getInt(STRING_KEY));

            return view;
        }
    }
}
