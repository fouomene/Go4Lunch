package com.jpz.go4lunch;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.Place;
import com.jpz.go4lunch.fragments.RestaurantListFragment;
import com.jpz.go4lunch.models.RestaurantDataToSort;
import com.jpz.go4lunch.utils.ConvertData;
import com.jpz.go4lunch.utils.MySharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;


public class UtilsTest {

    private ConvertData convertData = new ConvertData();

    private Double lat1;
    private Double lng1;
    private Double lat2;
    private Double lng2;
    private boolean isChecked;

    private Place place;
    int proximity;
    private double rating;
    private int numberWorkmates;

    private RestaurantDataToSort restaurantDataToSort;


    private Place place2;
    int proximity2;
    private double rating2;
    private int numberWorkmates2;
    private RestaurantDataToSort restaurantDataToSort2;

    private List<RestaurantDataToSort> restaurantDataList = new ArrayList<>();

    @Before
    public void setStringsForTests() {
        // GPS coordinates
        // Paris
        lat1 = 48.866667;
        lng1 = 2.333333;
        // London
        lat2 = 51.507322;
        lng2 = -0.127647;

        // Boolean for the value of the CheckBox in SettingsActivity
        isChecked = true;

        proximity = 10;
        rating = 3.5;
        numberWorkmates = 2;
        restaurantDataToSort = new RestaurantDataToSort(place, proximity, rating, numberWorkmates);

        proximity2 = 5;
        rating2 = 4.5;
        numberWorkmates2 = 4;
        restaurantDataToSort2 = new RestaurantDataToSort(place2, proximity2, rating2, numberWorkmates2);

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
        // Mock context
        MySharedPreferences prefs = mock(MySharedPreferences.class);
        // Mock with value put in method saveNotificationState()
        when(prefs.getNotificationState()).thenReturn(isChecked);
        assertTrue("true", prefs.getNotificationState());
    }

    @Test
    public void displayOpeningHourTest() {
        List<Period> periods = new ArrayList<>();
        Context context = RuntimeEnvironment.systemContext;
        //Context context = ApplicationProvider.getApplicationContext();
        //RestaurantListFragment restaurantListFragment = mock(RestaurantListFragment.class);
        //Context context = viewHolder.itemView.getContext().getApplicationContext();
        //context = restaurantListFragment.getActivity().getApplicationContext();

        when(convertData.getOpenCloseHours(periods, context)).thenReturn("Closed");
        assertEquals("Closed", convertData.getOpenCloseHours(periods, context));
    }

    @Test
    public void sortByProximityTest() {
        convertData.sortByProximity(restaurantDataList);
        assertThat(restaurantDataList, containsInAnyOrder(10));
    }

}
