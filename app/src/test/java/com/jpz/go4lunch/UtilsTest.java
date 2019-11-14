package com.jpz.go4lunch;

import android.content.Context;

import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.models.RestaurantDataToSort;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.MySharedPreferences;

import org.hamcrest.collection.IsEmptyCollection;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class UtilsTest {

    private ConvertData convertData = new ConvertData();
    // Mock context
    private Context context = mock(Context.class);

    // To calculate the distance between two locations
    private Double lat1;
    private Double lng1;
    private Double lat2;
    private Double lng2;
    private boolean isChecked;

    // To set a list of RestaurantDataToSort
    private Place place;
    private Place place2;
    private RestaurantDataToSort restaurantDataToSort;
    private RestaurantDataToSort restaurantDataToSort2;
    private List<RestaurantDataToSort> restaurantDataList = new ArrayList<>();

    @Before
    public void setForTests() {
        // GPS coordinates
        // Paris
        lat1 = 48.866667;
        lng1 = 2.333333;
        // London
        lat2 = 51.507322;
        lng2 = -0.127647;

        // Boolean for the value of the CheckBox in SettingsActivity
        isChecked = true;

        // Set values of 2 RestaurantDataToSort objects
        int proximity = 10;
        double rating = 3.5;
        int numberWorkmates = 2;
        restaurantDataToSort = new RestaurantDataToSort(place, proximity, rating, numberWorkmates);

        int proximity2 = 5;
        double rating2 = 4.5;
        int numberWorkmates2 = 4;
        restaurantDataToSort2 = new RestaurantDataToSort(place2, proximity2, rating2, numberWorkmates2);

        // Add these values to a list
        restaurantDataList.add(restaurantDataToSort);
        restaurantDataList.add(restaurantDataToSort2);
    }

    @Test
    public void getDistanceCalculationTest() {
        // 341 884 meters from Paris to London, as the crow flies
        assertEquals(341884, convertData.distanceCalculation(lat1, lng1, lat2, lng2));
    }

    @Test
    public void getNotificationStateTest() {
        // Mock MySharedPreferences context
        MySharedPreferences prefs = mock(MySharedPreferences.class);
        // Mock with value put in method saveNotificationState()
        when(prefs.getNotificationState()).thenReturn(isChecked);
        assertTrue("true", prefs.getNotificationState());
    }

    @Test
    public void sortByProximityTest() {
        convertData.sortByProximity(restaurantDataList);
        // 1. Check restaurantDataList isn't empty
        assertThat(restaurantDataList, not(IsEmptyCollection.empty()));
        // 2. Check restaurantDataList size
        assertThat(restaurantDataList, hasSize(2));
        assertThat(restaurantDataList.size(), is(2));
        // 3. Check restaurantDataList has restaurantDataToSort and restaurantDataToSort2 items
        assertThat(restaurantDataList, hasItems(restaurantDataToSort));
        assertThat(restaurantDataList, hasItems(restaurantDataToSort2));
        // 4. Test the first value is the nearest value (lowest)
        assertThat(restaurantDataList.get(0).getProximity(), is(5));
    }

    @Test
    public void sortByRatingTest() {
        convertData.sortByRating(restaurantDataList);
        // 1. Check restaurantDataList isn't empty
        assertThat(restaurantDataList, not(IsEmptyCollection.empty()));
        // 2. Check restaurantDataList size
        assertThat(restaurantDataList, hasSize(2));
        assertThat(restaurantDataList.size(), is(2));
        // 3. Check restaurantDataList has restaurantDataToSort and restaurantDataToSort2 items
        assertThat(restaurantDataList, hasItems(restaurantDataToSort));
        assertThat(restaurantDataList, hasItems(restaurantDataToSort2));
        // 4. Test the first value is the biggest value
        assertThat(restaurantDataList.get(0).getRating(), is(4.5));
    }

    @Test
    public void sortByNumberWorkmatesTest() {
        convertData.sortByNumberWorkmates(restaurantDataList);
        // 1. Check restaurantDataList isn't empty
        assertThat(restaurantDataList, not(IsEmptyCollection.empty()));
        // 2. Check restaurantDataList size
        assertThat(restaurantDataList, hasSize(2));
        assertThat(restaurantDataList.size(), is(2));
        // 3. Check restaurantDataList has restaurantDataToSort and restaurantDataToSort2 items
        assertThat(restaurantDataList, hasItems(restaurantDataToSort));
        assertThat(restaurantDataList, hasItems(restaurantDataToSort2));
        // 4. Test the first value is the biggest value
        assertThat(restaurantDataList.get(0).getNumberWorkmates(), is(4));
    }

    @Test
    public void displayOpeningHourTest() {
        List<Period> periods = new ArrayList<>();
        when(convertData.getOpenCloseHours(periods, context)).thenReturn("Closed");
        assertEquals("Closed", convertData.getOpenCloseHours(periods, context));
    }

}
