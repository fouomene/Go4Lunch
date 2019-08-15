package com.jpz.go4lunch.utils;

import com.google.android.libraries.places.api.model.Place;

public class ConvertMethods {
    
    // Format the address with the components
    public String getAddress(Place place) {
        String streetNumber = null;
        String route = null;
        String address;
        if (place.getAddressComponents() != null) {
            int size  = (place.getAddressComponents().asList().size()) - 1;
            for (int i = 0; i <= size; i++) {
                if (place.getAddressComponents().asList().get(i).getTypes().get(0).equals("street_number"))
                    streetNumber = place.getAddressComponents().asList().get(i).getName();
                if (place.getAddressComponents().asList().get(i).getTypes().get(0).equals("route"))
                    route = place.getAddressComponents().asList().get(i).getName();
            }
            address = streetNumber + " " + route;
        }
        else address = null;
        return address;
    }

}
