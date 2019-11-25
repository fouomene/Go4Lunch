package com.jpz.go4lunch.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.ProgressBar;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.FirebaseUtils;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

import static com.jpz.go4lunch.api.WorkmateHelper.createWorkmate;

public class ConnectionActivity extends AppCompatActivity {

    private View contentView;
    private ProgressBar progressBar;

    // Identifier for Google and Facebook Sign-In
    public static final int RC_GOOGLE_SIGN_IN = 9001;
    private static int RC_FACEBOOK_SIGN_IN;

    private static final String FACEBOOK_TAG = "FacebookLogin";
    private static final String GOOGLE_TAG = "GoogleActivity";

    // Utils
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private ConvertData convertData = new ConvertData();
    private MyUtilsNavigation myUtilsNavigation = new MyUtilsNavigation();

    // Declare Firebase authentication
    private FirebaseAuth auth;

    // Callback Manager for Facebook purpose
    private CallbackManager callbackManager;
    // For Google SignIn
    private GoogleSignInClient googleSignInClient;

    private static final String TAG = ConnectionActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        contentView = findViewById(android.R.id.content);
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
                convertData.showSnackbar(contentView, getString(R.string.sign_in_cancelled));
            }

            @Override
            public void onError(FacebookException error) {
                Log.w(FACEBOOK_TAG, "facebook:onError", error);
                if (error.toString().equals("CONNECTION_FAILURE: CONNECTION_FAILURE")) {
                    convertData.showSnackbar(contentView, getString(R.string.connection_failure));
                } else {
                    convertData.showSnackbar(contentView, getString(R.string.authentication_failed));
                }
            }
        });

        RC_FACEBOOK_SIGN_IN = facebookLogin.getRequestCode();
        Log.i(FACEBOOK_TAG, "facebook : requestCode :" + RC_FACEBOOK_SIGN_IN);

        //--------------------------------------------------------------------------------------
        // For Google Authentication

        // Handle sign-in button taps by creating a sign-in intent with the googleSignIn method
        googleLogin.setOnClickListener(v ->
                googleSignIn()
        );

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
        Log.i(TAG, "user logged = " + firebaseUtils.isCurrentUserLogged());
        if (firebaseUtils.isCurrentUserLogged()) {
            myUtilsNavigation.startMainActivity(this);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult : " + requestCode + ":" + resultCode + ":" + data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null)
                    firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(GOOGLE_TAG, "Google sign in failed", e);
                Log.i(GOOGLE_TAG, "signInResult:failed code=" + e.getStatusCode());
                if (e.getStatusCode() == 12501) {
                    convertData.showSnackbar(contentView, getString(R.string.sign_in_cancelled));
                } else if (e.getStatusCode() == 7) {
                    convertData.showSnackbar(contentView, getString(R.string.connection_failure));
                } else {
                    convertData.showSnackbar(contentView, getString(R.string.authentication_failed));
                }
            }
        } else if (requestCode == RC_FACEBOOK_SIGN_IN) {
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
    private void handleFacebookAccessToken(@NonNull AccessToken token) {
        Log.i(FACEBOOK_TAG, "handleFacebookAccessToken:" + token);
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Create a workmate document for the current user in Firestore
                        createWorkmateInFirestore();
                        // Sign in success, update UI with the signed-in user's information
                        Log.i(FACEBOOK_TAG, "signInWithCredential:success");
                        myUtilsNavigation.startMainActivity(this);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(FACEBOOK_TAG, "signInWithCredential:failure", task.getException());
                        try {
                            if (task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            // If account already exists, logout from Facebook provider
                            convertData.showSnackbar(contentView, getString(R.string.account_exists));
                            LoginManager.getInstance().logOut();
                        } catch (FirebaseNetworkException e) {
                            convertData.showSnackbar(contentView, getString(R.string.connection_failure));
                        } catch (Exception e) {
                            e.printStackTrace();
                            convertData.showSnackbar(contentView, getString(R.string.authentication_failed));
                        }
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                });
    }

    //--------------------------------------------------------------------------------------
    // Privates methods for Google Authentication

    // Starting the intent prompts the user to select a Google account to sign in with
    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    /*
    After a user successfully signs in, get an ID token from the GoogleSignInAccount object,
    exchange it for a Firebase credential, and authenticate with Firebase using the Firebase credential
     */
    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount acct) {
        Log.i(GOOGLE_TAG, "firebaseAuthWithGoogle:" + acct.getId());
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Create a workmate document for the current user in Firestore
                        createWorkmateInFirestore();
                        // Sign in success, update UI with the signed-in user's information
                        Log.i(GOOGLE_TAG, "signInWithCredential:success");
                        myUtilsNavigation.startMainActivity(this);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(GOOGLE_TAG, "signInWithCredential:failure", task.getException());
                        try {
                            if (task.getException() != null)
                                throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {
                            convertData.showSnackbar(contentView, getString(R.string.account_exists));
                        } catch (FirebaseNetworkException e) {
                            convertData.showSnackbar(contentView, getString(R.string.connection_failure));
                        } catch (Exception e) {
                            e.printStackTrace();
                            convertData.showSnackbar(contentView, getString(R.string.authentication_failed));
                        }
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                });
    }

    //--------------------------------------------------------------------------------------

    // Create the current user in Firestore when he is identified
    private void createWorkmateInFirestore() {
        if (firebaseUtils.getCurrentUser() != null) {
            String id = firebaseUtils.getCurrentUser().getUid();
            String username = firebaseUtils.getCurrentUser().getDisplayName();
            String urlPicture = (firebaseUtils.getCurrentUser().getPhotoUrl() != null) ?
                    firebaseUtils.getCurrentUser().getPhotoUrl().toString() : null;
            // Set data. By default restaurantId, restaurantName, restaurantAddress and restaurantsLikedId are null.
            createWorkmate(id, username, urlPicture, null, null, null, null, null);
        }
    }

}