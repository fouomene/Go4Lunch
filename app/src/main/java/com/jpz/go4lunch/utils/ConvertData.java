package com.jpz.go4lunch.utils;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.snackbar.Snackbar;
import com.jpz.go4lunch.R;
import com.jpz.go4lunch.models.RestaurantDataToSort;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ConvertData {
    // Class to convert data from Places SDK or Firestore and use its for UI purpose

    private static final String TAG = ConvertData.class.getSimpleName();

    // Format the address with the components
    public String getAddress(Place place) {
        String streetNumber = null;
        String route = null;
        String address = null;
        if (place.getAddressComponents() != null) {
            for (AddressComponent addressComponent : place.getAddressComponents().asList()) {
                if (addressComponent.getTypes().get(0).equals("street_number")) {
                    streetNumber = addressComponent.getName();
                }
                if (addressComponent.getTypes().get(0).equals("route"))
                    route = addressComponent.getName();
            }
            address = streetNumber + " " + route;
        }
        return address;
    }

    //--------------------------------------------------------------------------------------

    // Recover the opening hours
    public String openingHours(Place place, Context context) {

        String openingHours;

        Calendar calendar = Calendar.getInstance();
        // Set date format for full weekday
        String dateFormat = "EEEE";
        // Create a SimpleDateFormat with full weekday
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        // Get the weekday in uppercase
        String weekday = sdf.format(calendar.getTime()).toUpperCase();

        // Prevent the user if there is no data
        if (place.getOpeningHours() == null) {
            openingHours = context.getString(R.string.not_disclosed);
        }
        // Else get today's closing time
        else {
            openingHours = getOpenCloseHours(getPeriods(place, DayOfWeek.valueOf(weekday)), context);
        }

        return openingHours;
    }

    // Get a list of periods
    private List<Period> getPeriods(Place place, DayOfWeek dayOfWeek) {
        List<Period> periodList = new ArrayList<>();
        if (place.getOpeningHours() != null) {
            for (Period period : place.getOpeningHours().getPeriods()) {
                if (period.getOpen() != null && period.getOpen().getDay() == dayOfWeek) {
                    periodList.add(period);
                }
            }
        }
        return periodList;
    }

    // Calculate the open and close hours and manage UI
    private String getOpenCloseHours(List<Period> periodList, Context context) {
        String openingHours;
        int closeMinuteUI = 0;
        int closeHourUI = 0;
        long compareClosingTime = 0;
        boolean restaurantIsOpen = false;

        // Set the current time
        Calendar actualDay = Calendar.getInstance();
        Date today = actualDay.getTime();
        actualDay.setTime(today);
        long actualHour = actualDay.getTimeInMillis();

        for (Period period : periodList) {
            if (period.getOpen() != null && period.getClose() != null) {

                // Get the opening hour of the period
                int openHour = period.getOpen().getTime().getHours();
                int openMinute = period.getOpen().getTime().getMinutes();
                // Set the calendar with opening time
                Calendar openCalendar = Calendar.getInstance();
                openCalendar.setTimeInMillis(System.currentTimeMillis());
                openCalendar.set(Calendar.HOUR_OF_DAY, openHour);
                openCalendar.set(Calendar.MINUTE, openMinute);
                long openRestaurantHour = openCalendar.getTimeInMillis();

                // Get the closing hour of the period
                int closeHour = period.getClose().getTime().getHours();
                int closeMinute = period.getClose().getTime().getMinutes();
                // Set the calendar with closure hour
                Calendar closeCalendar = Calendar.getInstance();
                closeCalendar.setTimeInMillis(System.currentTimeMillis());
                closeCalendar.set(Calendar.HOUR_OF_DAY, closeHour);
                closeCalendar.set(Calendar.MINUTE, closeMinute);
                long closeRestaurantHour = closeCalendar.getTimeInMillis();

                // If the restaurant is currently open, set values for UI and boolean true
                if (openRestaurantHour <= actualHour && closeRestaurantHour > actualHour) {
                    // Get the closure minute of this period to manage UI
                    closeMinuteUI = period.getClose().getTime().getMinutes();
                    // Get the closure hour of this period in PM to manage UI
                    closeHourUI = (period.getClose().getTime().getHours() - 12);
                    // Comparison between the closing time of the restaurant and the actual hour
                    compareClosingTime = closeRestaurantHour - actualHour;
                    restaurantIsOpen = true;
                }
            }
        }
        if (restaurantIsOpen) {
            // If the closing time is less than 30 minutes, prevent the user of the close closure
            if (compareClosingTime < 30 * 60 * 1000) {
                openingHours = context.getString(R.string.closing_soon);
            }
            // Else if the closure hour is "time o'clock", display the closure hour without the minutes
            else if (closeMinuteUI == 0) {
                openingHours = context.getString(R.string.open_until_hour, closeHourUI);
            }
            // Else display the closure hour with the hours and minutes
            else {
                openingHours = context.getString(R.string.open_until_hour_minute, closeHourUI, closeMinuteUI);
            }
        // Else the restaurant is currently closed
        } else openingHours = context.getString(R.string.closed);

        return openingHours;
    }

    //--------------------------------------------------------------------------------------

    // Update rating and attribute stars
    public void updateRating(Place place, ImageView firstStar, ImageView secondStar, ImageView thirdStar) {
        // Reset the stars in the item view before assigning rating
        firstStar.setVisibility(View.GONE);
        secondStar.setVisibility(View.GONE);
        thirdStar.setVisibility(View.GONE);
        //The place's rating, from 1.0 to 5.0, based on aggregated user reviews.
        if (place.getRating() != null) {
            if (place.getRating() > 2.5 && place.getRating() < 3) {
                firstStar.setVisibility(View.VISIBLE);
            }
            if (place.getRating() >= 3 && place.getRating() < 4) {
                firstStar.setVisibility(View.VISIBLE);
                secondStar.setVisibility(View.VISIBLE);
            }
            if (place.getRating() >= 4) {
                firstStar.setVisibility(View.VISIBLE);
                secondStar.setVisibility(View.VISIBLE);
                thirdStar.setVisibility(View.VISIBLE);
            }
        }
    }

    //--------------------------------------------------------------------------------------

    /*
     * Calculate distance between two points in latitude and longitude.
     * Uses Haversine method as its base.
     * lat1, lon1 Start point lat2, lon2 End point.
     * Return Distance in Meters.
     */
    public int distanceCalculation(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        distance = Math.pow(distance, 2);
        return (int) Math.sqrt(distance);
    }

    //--------------------------------------------------------------------------------------

    // Get the date of the day
    public String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        // Create a SimpleDateFormat
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        // Get today date
        return sdf.format(calendar.getTime());
    }

    //--------------------------------------------------------------------------------------

    public void showSnackbar(View view, String text) {
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    //----------------------------------------------------------------------------------
    // Methods to sort data of restaurants in function of the user's choice

    public void sortByProximity(List<RestaurantDataToSort> restaurantDataList) {
        Collections.sort(restaurantDataList, (o1, o2) -> {
            //if (o1.getProximity().equals(o2.getProximity())) {
            if (Objects.equals(o1.getProximity(), o2.getProximity())) {
                return 0;
            }
            //return o1.getProximity().compareTo(o2.getProximity());
            return Objects.compare(o1.getProximity(), o2.getProximity(), Integer::compareTo);
        });
    }

    public void sortByRating(List<RestaurantDataToSort> restaurantDataList) {
        Collections.sort(restaurantDataList, (o1, o2) -> {
            if (Objects.equals(o1.getRating(), o2.getRating())) {
                return 0;
            }
            // Compare in descending order
            return Objects.compare(o2.getRating(), o1.getRating(), Double::compareTo);
        });
    }

    public void sortByNumberWorkmates(List<RestaurantDataToSort> restaurantDataList) {
        Collections.sort(restaurantDataList, (o1, o2) -> {
            if (Objects.equals(o1.getNumberWorkmates(), o2.getNumberWorkmates())) {
                return 0;
            }
            // Compare in descending order
            return Objects.compare(o2.getNumberWorkmates(), o1.getNumberWorkmates(), Integer::compareTo);
        });
    }

    //----------------------------------------------------------------------------------

}
