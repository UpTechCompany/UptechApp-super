package com.example.uptechapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.uptechapp.R;
import com.example.uptechapp.databinding.FragmentSplashBinding;

import java.util.Locale;

public class SplashFragment extends Fragment {

    private static final String TAG = "ActivitySplash";
    private FragmentSplashBinding binding;
    private NavController navController;

    public static String language;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        navController = navHostFragment.getNavController();
        requireActivity().findViewById(R.id.navigation).setVisibility(View.GONE);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                long id = 0L;
                try {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                    id = sharedPref.getLong(getString(R.string.id_logging), 0L);

                } catch (Exception e) {
                    Log.i(TAG, e.getMessage());
                }

                if (id != 0l){
                    navController.navigate(R.id.fragment_emergency_feed);
                }
                else {
                    navController.navigate(R.id.fragment_emergency_feed);
                    EmergencyFeedFragment.learn = 0;
                }
                requireActivity().findViewById(R.id.navigation).setVisibility(View.VISIBLE);
            }
        }, 3000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void updateLocale() {
        String language = Locale.getDefault().getLanguage();
        SplashFragment.language = language;
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

    }
    public static String getLanguage() {
        return language;
    }
}
