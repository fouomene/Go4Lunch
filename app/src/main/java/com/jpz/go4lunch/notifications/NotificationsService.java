package com.jpz.go4lunch.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.activities.MainActivity;
import com.jpz.go4lunch.models.Workmate;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

import static com.jpz.go4lunch.api.WorkmateHelper.FIELD_RESTAURANT_DATE;
import static com.jpz.go4lunch.api.WorkmateHelper.FIELD_RESTAURANT_ID;
import static com.jpz.go4lunch.api.WorkmateHelper.FIELD_USERNAME;
import static com.jpz.go4lunch.api.WorkmateHelper.getCurrentWorkmate;
import static com.jpz.go4lunch.api.WorkmateHelper.getWorkmatesCollection;

public class NotificationsService extends FirebaseMessagingService {

    // Models and API
    private Workmate currentWorkmate = new Workmate();

    // Utils
    private FirebaseUtils firebaseUtils = new FirebaseUtils();
    private ConvertData convertData = new ConvertData();

    private static final String TAG = NotificationsService.class.getSimpleName();
    private final int NOTIFICATION_ID = 123;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            String messageBody = remoteMessage.getNotification().getBody();
            // Get data and show notification after received message
            getRestaurantData(messageBody);
            Log.i(TAG, messageBody);
        }
    }

    // Get the data needed for the notification
    private void getRestaurantData(String messageBody) {
        // Data to show
        List<String> myLunchWorkmates = new ArrayList<>();

        // Initialize FireBase User
        FirebaseUser currentUser = firebaseUtils.getCurrentUser();

        if (currentUser != null) {
            getCurrentWorkmate(currentUser.getUid())
                    .addOnSuccessListener(documentSnapshot -> {
                        currentWorkmate = documentSnapshot.toObject(Workmate.class);
                        // Get the name of the restaurant chosen from rhe current user
                        if (currentWorkmate != null) {
                            String myLunchRestaurant = currentWorkmate.getRestaurantName();
                            String myRestaurantAddress = currentWorkmate.getRestaurantAddress();

                            // Get the names of the workmates who chose this restaurant
                            getWorkmatesCollection()
                                    .whereEqualTo(FIELD_RESTAURANT_DATE, convertData.getTodayDate())
                                    .whereEqualTo(FIELD_RESTAURANT_ID, currentWorkmate.getRestaurantId())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful() && task.getResult() != null) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                // Remove the current user
                                                if (currentUser.getDisplayName() != null &&
                                                        !currentUser.getDisplayName()
                                                                .equals(document.getString(FIELD_USERNAME))) {
                                                    // Add them in a list
                                                    myLunchWorkmates.add(document.getString(FIELD_USERNAME));
                                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                                }
                                            }
                                        } else {
                                            Log.d(TAG, "Error getting documents: ", task.getException());
                                        }
                                        // Show the notification
                                        sendVisualNotification(messageBody, myLunchRestaurant,
                                                myRestaurantAddress, myLunchWorkmates);
                                    });
                        }
                    });
        }
    }

    private void sendVisualNotification(String messageBody, String myLunchRestaurant,
                                        String myRestaurantAddress, List<String> myLunchWorkmates) {

        // Create an Intent that will be shown when user will click on the Notification
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        String notificationText;
        if (myLunchWorkmates.isEmpty()) {
            // You are alone to eat...
            notificationText = getString(R.string.notification_alone,
                    myLunchRestaurant, myRestaurantAddress);
        } else {
            // Format the list of workmates
            String formatMyLunchWorkmates = TextUtils.join(", ", myLunchWorkmates);
            // Get notificationText with them
            notificationText = getString(R.string.notification,
                    myLunchRestaurant, myRestaurantAddress, formatMyLunchWorkmates);
        }

        // Create a Style for the Notification
        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        // Create a Channel (Android 8)
        String channelId = getString(R.string.default_notification_channel_id);

        // Build a Notification object
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.hot_food_bowl_white)
                        .setContentTitle(messageBody)
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setAutoCancel(true)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentIntent(pendingIntent)
                        .setStyle(bigTextStyle.bigText(notificationText));

        // Add the Notification to the NotificationManager and show it.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = getString(R.string.message_from_firebase);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        // Show the notification (notify builder)
        notificationManager.notify(TAG, NOTIFICATION_ID, notificationBuilder.build());
    }
}
