package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.FirebaseUtils;

import java.util.Arrays;

public class ConnectionActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;

    private LoginButton facebookLogin;
    private SignInButton googleLogin;

    public ProgressBar progressBar;

    // Identifier for Sign-In Activity
    public static final int RC_SIGN_IN = 123;

    private static final String TAG = "FacebookLogin";

    // FirebaseUtils class
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    private CallbackManager callbackManager;

    // Declare authentication
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        constraintLayout = findViewById(R.id.connection_activity_layout);

        progressBar = findViewById(R.id.progress_bar);

        LoginButton facebookLogin = findViewById(R.id.connection_activity_facebook_login);
        //googleLogin = findViewById(R.id.connection_activity_google_login);

        // Initialize Firebase Auth
        auth = firebaseUtils.authGetInstance();

        // Create a callback facebookLogin button
        callbackManager = CallbackManager.Factory.create();
        facebookLogin.setPermissions("email", "public_profile");
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "facebook:onError", error);
            }
        });
    }

    // On start check if user is already logged
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) then start MainActivity
        Log.i("Tag", "user logged = " + firebaseUtils.isCurrentUserLogged());
        if (firebaseUtils.isCurrentUserLogged()) {
            startMainActivity();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //handleResponseAfterSignIn(requestCode, resultCode, data);

    }

    // Start auth with facebook
    private void handleFacebookAccessToken(AccessToken token) {
        Log.i(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(TAG, "signInWithCredential:success");
                            Snackbar.make(constraintLayout, getString(R.string.connection_succeed), Snackbar.LENGTH_SHORT).show();
                            startMainActivity();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(ConnectionActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //--------------------------------------------------------------------------------------

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /*
    // Show Snack Bar with a message
    private void showSnackBar(String message){
        Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show();
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
                //.setTheme(R.style.AppTheme_Connection)
                .build(), RC_SIGN_IN);
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
    */

}