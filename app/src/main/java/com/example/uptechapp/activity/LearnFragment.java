package com.example.uptechapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.uptechapp.R;
import com.example.uptechapp.dao.GifImage;
import com.example.uptechapp.dao.GifPagerAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class LearnFragment extends Fragment {
    private final List<GifImage> gifImages_ru = Arrays.asList(
        new GifImage(R.drawable.learn1_ru),
        new GifImage(R.drawable.learn2_ru),
            new GifImage(R.drawable.learn3_ru)
    );
    private final List<GifImage> gifImages_en = Arrays.asList(
            new GifImage(R.drawable.learn1_en),
            new GifImage(R.drawable.learn2_en),
            new GifImage(R.drawable.learn3_en)
    );

    public String language = SplashFragment.getLanguage();


    @SuppressLint("UseRequireInsteadOfGet")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.learning, container, false);


        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        if (Objects.equals(language, "ru")){
            viewPager.setAdapter(new GifPagerAdapter(gifImages_ru));
        } else {
            viewPager.setAdapter(new GifPagerAdapter(gifImages_en));
        }

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> {
            Objects.requireNonNull(getActivity()).findViewById(R.id.navigation).setVisibility(View.VISIBLE);
            Objects.requireNonNull(getActivity()).onBackPressed();
        });


        return view;
    }
}

