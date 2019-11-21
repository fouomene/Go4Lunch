package com.jpz.go4lunch.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.FirebaseUtils;
import com.jpz.go4lunch.utils.MySharedPreferences;
import com.jpz.go4lunch.utils.MyUtilsNavigation;

import static com.jpz.go4lunch.api.WorkmateHelper.deleteWorkmate;

public class SettingsActivity extends AppCompatActivity {

    // Layout & Widgets
    private ConstraintLayout constraintLayout;
    private CheckBox checkBoxNotification;
    private Button buttonDelete;

    // Utils
    private ConvertData convertData = new ConvertData();
    private MySharedPreferences prefs;
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private FirebaseUser currentUser;
    private MyUtilsNavigation myUtilsNavigation = new MyUtilsNavigation();

    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        constraintLayout = findViewById(R.id.settings_activity_layout);
        checkBoxNotification = findViewById(R.id.settings_activity_notifications);
        buttonDelete = findViewById(R.id.settings_activity_delete_account);

        configureToolbar();
        // Use MySharedPreferences...
        prefs = new MySharedPreferences(this);
        // ... to load the last checkBoxNotification state
        boolean boxIsChecked = prefs.getNotificationState();
        // If the checkbox was checked to notify, remember its state
        if (boxIsChecked) {
            // Set the checkbox state
            checkBoxNotification.setChecked(true);
        }

        subscribeToNotifications();
        onDeleteClicked();
    }

    //----------------------------------------------------------------------------------
    // Private methods to configure design

    private void configureToolbar() {
        // Get the toolbar view inside the activity layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Set the Toolbar
        setSupportActionBar(toolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Set and enable the Up button
        if (ab != null) {
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    //----------------------------------------------------------------------------------

    // Listener on the checkbox to subscribe notification
    private void subscribeToNotifications() {
        checkBoxNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Save the state of the checkbox and reuse it when SettingsActivity is open
                prefs.saveNotificationState(true);
                // If is checked, subscribe to notification
                FirebaseMessaging.getInstance().subscribeToTopic("notification")
                        .addOnCompleteListener(task -> {
                            String msg = getString(R.string.notification_subscribed);
                            if (!task.isSuccessful()) {
                                msg = getString(R.string.notification_subscribe_failed);
                            }
                            Log.d(TAG, msg);
                            convertData.showSnackbar(constraintLayout, msg);
                        });
            } else {
                // Save the state of the checkbox and reuse it when SettingsActivity is open
                prefs.saveNotificationState(false);
                // If is unchecked, unsubscribe to notification
                FirebaseMessaging.getInstance().unsubscribeFromTopic("notification")
                        .addOnCompleteListener(task -> {
                            String msg = getString(R.string.notification_unsubscribed);
                            if (!task.isSuccessful()) {
                                msg = getString(R.string.notification_unsubscribe_failed);
                            }
                            Log.d(TAG, msg);
                            convertData.showSnackbar(constraintLayout, msg);
                        });
            }
        });
    }

    // Delete the Firebase account
    private void onDeleteClicked() {
        // Initialize FireBase User
        currentUser = firebaseUtils.getCurrentUser();

        buttonDelete.setOnClickListener(v -> {
            // Create a dialog window to warn the user
            AlertDialog.Builder warningDialog = new AlertDialog.Builder(SettingsActivity.this, R.style.AlertDialogWarning);
            warningDialog.setTitle(getString(R.string.warning));
            warningDialog.setMessage(getString(R.string.warning_delete));

            // Configure cancel button
            warningDialog.setNegativeButton(getString(R.string.cancel), (DialogInterface dialog, int which) ->
                    dialog.dismiss());

            // Configure ok button to delete the user account and log out
            warningDialog.setPositiveButton(getString(R.string.ok), (DialogInterface dialog, int which) -> {

                // Delete account on Firestore
                deleteWorkmate(currentUser.getUid());

                // Unsubscribe to notification and save the state of the checkbox at false
                prefs.saveNotificationState(false);
                // Unsubscribe to notification
                FirebaseMessaging.getInstance().unsubscribeFromTopic("notification")
                        .addOnCompleteListener(task -> {
                            String msg = getString(R.string.notification_unsubscribed);
                            if (!task.isSuccessful()) {
                                msg = getString(R.string.notification_unsubscribe_failed);
                            }
                            Log.d(TAG, msg);
                        });

                // Delete Authentication
                // Some security-sensitive actions require that the user has recently signed in.
                // If you perform one of these actions, and the user signed in too long ago, the action fails.
                // So re-authentication is needed before

                // Get auth credentials from the user for re-authentication.
                AuthCredential credential;

                // Check if the user is logged in with Facebook...
                for (UserInfo user : currentUser.getProviderData()) {
                    if (user.getProviderId().equals("facebook.com")) {
                        Log.i(TAG, "provider in loop " + user.getProviderId());
                        // ... then, in this case, logout from Facebook
                        LoginManager.getInstance().logOut();
                        // Also, attribute FacebookAuthProvider to the credential
                        credential = FacebookAuthProvider
                                .getCredential(AccessToken.getCurrentAccessToken().toString());
                        // Delete the Firebase account
                        reAuthenticateAndDeleteFromFirebase(credential);
                    } else {
                        // Else the user is logged with Google
                        // Get the account
                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
                        if (acct != null) {
                            // Attribute GoogleAuthProvider to the credential
                            credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                            // Delete the Firebase account
                            reAuthenticateAndDeleteFromFirebase(credential);
                        }
                    }
                }
            });
            warningDialog.show();
        });
    }

    private void reAuthenticateAndDeleteFromFirebase(AuthCredential credential) {
        // Prompt the user to re-provide their sign-in credentials
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "User re-authenticated.");
                    currentUser.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                            // When the user account is deleted, return to ConnectionActivity
                            myUtilsNavigation.startConnectionActivity(this);
                            finish();
                        }
                    });
                });
    }
}
