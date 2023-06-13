package com.example.uptechapp.activity;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.uptechapp.R;
import com.example.uptechapp.databinding.FragmentLoginBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginUser";

    private FragmentLoginBinding binding;
    private RelativeLayout layoutGoogle;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> activityResultLauncher;
    private Dialog progressBar;
    private TextView dialogText;
    private NavController navController;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentLoginBinding.inflate(getLayoutInflater());

        init();
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

        layoutGoogle = binding.signGoogle;

        requireActivity().findViewById(R.id.navigation).setVisibility(View.GONE);

        progressBar = new Dialog(getContext());
        progressBar.setContentView(R.layout.dialog_layout);
        progressBar.setCancelable(false);
        progressBar.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogText = progressBar.findViewById(R.id.dialogText);
        dialogText.setText(R.string.progressBarLogging);

        layoutGoogle.setOnClickListener(v -> googleSignIn());

        oneTapClient = Identity.getSignInClient(getContext());
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id_google))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build();

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {

            if (result.getResultCode() == RESULT_OK) {

                try {

                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    String idToken = credential.getGoogleIdToken();

                    if (idToken != null) {
                        String email = credential.getId();
                        Log.i(TAG, "EMAIL - " + email);

                        Log.i(TAG, "DATA " + credential.getPhoneNumber());
                        Log.i(TAG, "DATA-NAME" + credential.getProfilePictureUri() + " " + credential.getGivenName() + " " + credential.getDisplayName());

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = sharedPref.edit();

                        // get id from database


                        long id = 1L;

                        editor.putString("email", email);
                        editor.putLong(getString(R.string.id_logging), id);
                        editor.apply();

                        NavHostFragment navHostFragment = (NavHostFragment) getParentFragment();
                        navController = navHostFragment.getNavController();
                        requireActivity().findViewById(R.id.navigation).setVisibility(View.VISIBLE);
                        navController.navigate(R.id.fragment_emergency_feed);

                    } else {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    }

                } catch (ApiException e) {
                    progressBar.dismiss();

                    Toast.makeText(getContext(), "API: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    progressBar.dismiss();
                    Log.i(TAG, e.getMessage());

                    Toast.makeText(getContext(), "Something went wrong with getting data", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(getContext(), "Something went wrong. Try later", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void googleSignIn() {
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        try {
                            IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(
                                    beginSignInResult.getPendingIntent().getIntentSender()
                            ).build();
                            activityResultLauncher.launch(intentSenderRequest);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Exception " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
