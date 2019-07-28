package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.FirebaseUtils;

public class ConnectionActivity extends AppCompatActivity {

    private ConstraintLayout constraintLayout;
    public ProgressBar progressBar;

    // Identifier for Google Sign-In Activity
    public static final int RC_SIGN_IN = 9001;

    private static final String FACEBOOK_TAG = "FacebookLogin";
    private static final String GOOGLE_TAG = "GoogleActivity";

    // FirebaseUtils class
    private FirebaseUtils firebaseUtils = new FirebaseUtils();

    // Declare Firebase authentication
    private FirebaseAuth auth;

    private CallbackManager callbackManager;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        constraintLayout = findViewById(R.id.connection_activity_layout);

        progressBar = findViewById(R.id.progress_bar);

        LoginButton facebookLogin = findViewById(R.id.connection_activity_facebook_login);
        SignInButton googleLogin = findViewById(R.id.connection_activity_google_login);

        // Initialize Firebase Auth
        auth = firebaseUtils.authGetInstance();

        //--------------------------------------------------------------------------------------
        // For Facebook Authentication

        // Create a callback facebookLogin button
        callbackManager = CallbackManager.Factory.create();
        facebookLogin.setPermissions("email", "public_profile");
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(FACEBOOK_TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(FACEBOOK_TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(FACEBOOK_TAG, "facebook:onError", error);
                showSnackBar(getString(R.string.authentication_failed));
            }
        });

        //--------------------------------------------------------------------------------------
        // For Google Authentication

        // Handle sign-in button taps by creating a sign-in intent with the googleSignIn method
        googleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    //--------------------------------------------------------------------------------------
    // Activity lifecycle

    // On start check if user is already logged
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null), then if that is the case start MainActivity
        Log.i("Tag", "user logged = " + firebaseUtils.isCurrentUserLogged());
        if (firebaseUtils.isCurrentUserLogged()) {
            startMainActivity();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("Tag", "onActivityResult : " + requestCode + ":" + resultCode + ":" + data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(GOOGLE_TAG, "Google sign in failed", e);
            }
        } else {
            // Pass the activity result back to the Facebook SDK
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    //--------------------------------------------------------------------------------------
    // Private method for Facebook Authentication

    /*
    After a user successfully signs in, in the LoginButton's onSuccess callback method,
    get an access token for the signed-in user, exchange it for a Firebase credential,
    and authenticate with Firebase using the Firebase credential
     */
    private void handleFacebookAccessToken(AccessToken token) {
        Log.i(FACEBOOK_TAG, "handleFacebookAccessToken:" + token);
        // showProgressDialog();

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(FACEBOOK_TAG, "signInWithCredential:success");
                            //showSnackBar(getString(R.string.authentication_succeed));
                            startMainActivity();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(FACEBOOK_TAG, "signInWithCredential:failure", task.getException());
                            //showSnackBar(getString(R.string.authentication_failed));
                        }
                        // hideProgressDialog();
                    }
                });
    }

    //--------------------------------------------------------------------------------------
    // Privates methods for Google Authentication

    // Starting the intent prompts the user to select a Google account to sign in with
    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /*
    After a user successfully signs in, get an ID token from the GoogleSignInAccount object,
    exchange it for a Firebase credential, and authenticate with Firebase using the Firebase credential
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.i(GOOGLE_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.i(GOOGLE_TAG, "signInWithCredential:success");
                            //showSnackBar(getString(R.string.authentication_succeed));
                            startMainActivity();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(GOOGLE_TAG, "signInWithCredential:failure", task.getException());
                            showSnackBar(getString(R.string.authentication_failed));
                        }
                        // hideProgressDialog();
                    }
                });
    }

    //--------------------------------------------------------------------------------------

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Show Snack Bar with a message
    private void showSnackBar(String message){
        Snackbar.make(constraintLayout, message, Snackbar.LENGTH_SHORT).show();
    }


    /*

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