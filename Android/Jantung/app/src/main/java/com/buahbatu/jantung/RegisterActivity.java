package com.buahbatu.jantung;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

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

    @OnClick(R.id.button_next) void onNextClick(){
        int position = viewPager.getCurrentItem();
        if (position < fragmentList.size()-1){
            viewPager.setCurrentItem(position+1, true);
        }else {
            finish();
        }
    }

    List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        fragmentList = new ArrayList<>();
        fragmentList.add(TutorialFragment.newInstance(R.drawable.boy0, R.string.tutor1));
        fragmentList.add(TutorialFragment.newInstance(R.drawable.boy1, R.string.tutor2));
        fragmentList.add(TutorialFragment.newInstance(R.drawable.boy2, R.string.tutor3));
        fragmentList.add(TutorialFragment.newInstance(R.drawable.girl0, R.string.tutor4));
        fragmentList.add(new DataFragment());

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
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_data, container, false);
            TextView textView = (TextView) view.findViewById(R.id.terms_check);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.terms_check)));
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
