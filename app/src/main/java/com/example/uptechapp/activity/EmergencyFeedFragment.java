package com.example.uptechapp.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.uptechapp.R;
import com.example.uptechapp.api.CompleteListener;
import com.example.uptechapp.dao.Database;
import com.example.uptechapp.dao.EmergencyAdapter;
import com.example.uptechapp.dao.MyViewModel;
import com.example.uptechapp.databinding.FragmentEmergencyFeedBinding;
import com.example.uptechapp.model.Emergency;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EmergencyFeedFragment extends Fragment {

    public static int learn;
    private FragmentEmergencyFeedBinding binding;

    private RecyclerView emergencyFeed;
    private Dialog progressBar;
    private TextView dialogText;

    List<Emergency> myEmergencyList;
    EmergencyAdapter adapter;
    private NavController navController;


    public Dialog Dialog(@Nullable Bundle savedInstanceState) {
        String title = getResources().getString(R.string.training);
        String message = getResources().getString(R.string.massage);
        String button1String = getResources().getString(R.string.yes);
        String button2String = getResources().getString(R.string.no);
        final int[] par = {0};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);  // заголовок
        builder.setMessage(message); // сообщение


        Dialog dialog1 = new Dialog(getContext());
        dialog1.setContentView(R.layout.learning);
        dialog1.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog1.getWindow().setBackgroundDrawable(new ColorDrawable(androidx.constraintlayout.widget.R.style.AlertDialog_AppCompat_Light));
        builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                LearnFragment learnFragment = new LearnFragment();
                FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.mainFragmentContainer, learnFragment);
                fragmentTransaction.addToBackStack(null);
                requireActivity().findViewById(R.id.navigation).setVisibility(View.GONE);
                fragmentTransaction.commit();
            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.setCancelable(true);

        return builder.show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentEmergencyFeedBinding.inflate(getLayoutInflater());
        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
        navController = navHostFragment.getNavController();

        if (learn == 0){
            Dialog(null);
            learn = 1;
        }
        Log.d("NIKSD", String.valueOf(learn));

        init();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        emergencyFeed.setLayoutManager(layoutManager);

        myEmergencyList = new ArrayList<Emergency>();
        adapter = new EmergencyAdapter(myEmergencyList, getContext(), getActivity(), navController);

        emergencyFeed.setAdapter(adapter);

        Database.loadEmergencies(new CompleteListener() {
            @Override
            public void OnSuccess() {
                progressBar.dismiss();
            }

            @Override
            public void OnFailure() {
                progressBar.dismiss();

            }
        });

        final Observer<List<Emergency>> myObserver = new Observer<List<Emergency>>() {
            @Override
            public void onChanged(List<Emergency> emergencies) {
                myEmergencyList.clear();
                myEmergencyList.addAll(emergencies);
                Collections.reverse(myEmergencyList);
                adapter.notifyDataSetChanged();
            }
        };
        MyViewModel.getInstance().getEmergencyLiveData().observe(this, myObserver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void init() {

        emergencyFeed = binding.emergencyFeed;

        progressBar = new Dialog(getContext());
        progressBar.setContentView(R.layout.dialog_layout);
        progressBar.setCancelable(false);
        progressBar.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressBar.findViewById(R.id.dialogText);
        dialogText.setText("Loading");

        progressBar.show();

        binding.swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                myEmergencyList.clear();
                Database.loadEmergencies(new CompleteListener() {
                    @Override
                    public void OnSuccess() {
                        progressBar.dismiss();
                    }

                    @Override
                    public void OnFailure() {
                        progressBar.dismiss();
                    }
                });
                adapter.notifyDataSetChanged();
                binding.swiperefresh.setRefreshing(false);
            }
        });
    }
}
