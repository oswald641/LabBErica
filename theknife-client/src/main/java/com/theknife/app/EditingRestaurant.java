package com.theknife.app;

import java.io.IOException;

public class EditingRestaurant {
    //editing parameters
    private static int editing_id = -1;
    private static String name, nation, city, address, latitude, longitude, avg_price, has_delivery, has_online;

    public static void setEditing(int id) throws IOException {
        editing_id = id;

        Communicator.sendStream("getRestaurantInfo");
        Communicator.sendStream(Integer.toString(id));
        name = Communicator.readStream();
        nation = Communicator.readStream();
        city = Communicator.readStream();
        address = Communicator.readStream();
        latitude = Communicator.readStream();
        longitude = Communicator.readStream();
        avg_price = Communicator.readStream();
        has_delivery = Communicator.readStream();
        has_online = Communicator.readStream();
    }

    public static void reset() {
        editing_id = -1;
    }

    public static int getId() {
        return editing_id;
    }

    public static String[] getInfo() {
        return new String[]{name, nation, city, address, latitude, longitude, avg_price, has_delivery, has_online};
    }

    public static String addRestaurant(String name, String nation, String city, String address, String latitude, String longitude, String avg_price, boolean has_delivery, boolean has_online) throws IOException {
        Communicator.sendStream("addRestaurant");
        Communicator.sendStream(name);
        Communicator.sendStream(nation);
        Communicator.sendStream(city);
        Communicator.sendStream(address);
        Communicator.sendStream(latitude);
        Communicator.sendStream(longitude);
        Communicator.sendStream(avg_price);
        Communicator.sendStream(has_delivery ? "y" : "n");
        Communicator.sendStream(has_online ? "y" : "n");

        return Communicator.readStream();
    }

    public static String editRestaurant(int id, String name, String nation, String city, String address, String latitude, String longitude, String avg_price, boolean has_delivery, boolean has_online) throws IOException {
        Communicator.sendStream("editRestaurant");
        Communicator.sendStream(Integer.toString(id));
        Communicator.sendStream(name);
        Communicator.sendStream(nation);
        Communicator.sendStream(city);
        Communicator.sendStream(address);
        Communicator.sendStream(latitude);
        Communicator.sendStream(longitude);
        Communicator.sendStream(avg_price);
        Communicator.sendStream(has_delivery ? "y" : "n");
        Communicator.sendStream(has_online ? "y" : "n");

        return Communicator.readStream();
    }
}
