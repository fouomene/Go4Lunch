package com.jpz.go4lunch.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toolbar;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.snackbar.Snackbar;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.FirebaseUtils;

import java.util.Arrays;

public class ConnectionActivity extends AppCompatActivity {

    // Identifier for Sign-In Activity
    public static final int RC_SIGN_IN = 123;

    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        constraintLayout = findViewById(R.id.connexion_activity_layout);

        //Button button = findViewById(R.id.connexion_activity_button_login);

        //startSignInActivity();
        Log.i("Tag", "user logged = " + firebaseUtils.isCurrentUserLogged());

        if (!firebaseUtils.isCurrentUserLogged())
            startSignInActivity();
        else
            startMainActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    // Launch Sign-In Activity
    private void startSignInActivity() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(Arrays.asList(
                        new AuthUI.IdpConfig.FacebookBuilder().build(), // Support Google
                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                .setIsSmartLockEnabled(false, true)
                //.setAuthMethodPickerLayout(customLayout)
                .setTheme(R.style.AppTheme_Connection)
                .build(), RC_SIGN_IN);
    }

    // Show Snack Bar with a message
    private void showSnackBar(String message){
        Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                showSnackBar(getString(R.string.connection_succeed));
                startMainActivity();
            } else { // ERRORS
                if (response == null) {
                    showSnackBar(getString(R.string.error_authentication_canceled));
                } else {
                    if (response.getError() != null) {
                        if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                            showSnackBar(getString(R.string.error_no_internet));
                        } if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                            showSnackBar(getString(R.string.error_unknown_error));
                        }
                    }
                }
            }
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }


/*
    // You must provide a custom layout XML resource and configure at least one
    // provider button ID. It's important that that you set the button ID for every provider
    // that you have enabled.
    AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout
            .Builder(R.layout.activity_connection)
            .setFacebookButtonId(R.id.connexion_activity_facebook_login)
            .setGoogleButtonId(R.id.connexion_activity_google_login)
            .build();
*/

}