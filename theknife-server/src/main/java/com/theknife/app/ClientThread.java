package com.theknife.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class ClientThread extends Thread {
    private String ip;
    private BufferedReader reader;
    private OutputStream os;
    private int user_id = -1;

    public ClientThread(Socket socket) throws IOException {
        ip = socket.getInetAddress().toString();
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(),  StandardCharsets.UTF_8));
        os = socket.getOutputStream();
        start();
    }

    public void run() {
        try {
            exec();
        } catch(InterruptedException | IOException | SQLException e) {
            if(e.getMessage().equals("Connection reset"))
                log("Disconnected");
            else
                e.printStackTrace();
        }
    }

    private void log(String msg) {
        System.out.println("[" + ip + "]" + msg);
    }

    private String readStream() throws IOException {
        String msg = reader.readLine();
        log("[In<--]" + msg);
        return msg;
    }

    private void sendStream(String msg) throws IOException {
        log("[Out->]" + msg);
        os.write((msg + '\n').getBytes(StandardCharsets.UTF_8));
    }

    private void exec() throws InterruptedException, IOException, SQLException {
        log("Connected");

        String cmd = readStream();
        while(!cmd.equals("disconnect")) {
            switch (cmd) {
                case "ping":
                    sendStream("pong");
                    break;
                case "register":
                    String nome = readStream();
                    String cognome = readStream();
                    String username = readStream();
                    String password = readStream();
                    String data_nascita = readStream();
                    String latitude_string = readStream();
                    String longitude_string = readStream();
                    boolean is_ristoratore = readStream().equals("y");

                    sendStream(User.registerUser(nome, cognome, username, password, data_nascita, latitude_string, longitude_string, is_ristoratore));
                    break;
                case "login":
                    username = readStream();
                    password = readStream();

                    int logged_user_id = User.loginUser(username, password);
                    if(logged_user_id > 0) {
                        sendStream("ok");
                        user_id = logged_user_id;
                    }
                    else if(logged_user_id == -1) //no user was found with given username
                        sendStream("username");
                    else if(logged_user_id == -2) //password mismatch
                        sendStream("password");
                    break;
                case "getUserInfo":
                    if(user_id < 1)
                        sendStream("unauthorized");
                    else {
                        String[] user_info = DBHandler.getUserInfo(user_id);
                        sendStream(user_info[0]);
                        sendStream(user_info[1]);
                        sendStream(user_info[2]);
                    }
                    break;
                case "addRestaurant":
                    //if the user isn't a restaurator, he cannot add a restaurant
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    String name = readStream(),
                    nation = readStream(),
                    city = readStream(),
                    address = readStream();
                    latitude_string = readStream();
                    longitude_string = readStream();
                    String avg_price_string = readStream(), categories = readStream();
                    boolean has_delivery = readStream().equals("y"), has_online = readStream().equals("y");

                    if(name.trim().isEmpty() || nation.trim().isEmpty() || city.trim().isEmpty() || address.trim().isEmpty() || categories.trim().isEmpty()) {
                        sendStream("missing");
                        break;
                    }

                    double latitude, longitude;
                    try {
                        latitude = Double.parseDouble(latitude_string);
                        longitude = Double.parseDouble(longitude_string);
                    } catch (NumberFormatException e) {
                        sendStream("coordinates");
                        break;
                    }

                    int price;
                    try {
                        price = Integer.parseInt(avg_price_string);
                    } catch (NumberFormatException e) {
                        sendStream("price_format");
                        break;
                    }

                    if(price < 0) {
                        sendStream("price_negative");
                        break;
                    }

                    if(DBHandler.addRestaurant(user_id, name, nation, city, address, latitude, longitude, price, categories, has_delivery, has_online))
                        sendStream("ok");
                    else
                        sendStream("error");
                    break;
                case "editRestaurant":
                    //if the user isn't a restaurator, he cannot edit a restaurant
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }
                    
                    int id = Integer.parseInt(readStream());

                    if(!DBHandler.hasAccess(user_id, id))
                        sendStream("no access");
                    else {
                        name = readStream();
                        nation = readStream();
                        city = readStream();
                        address = readStream();
                        latitude_string = readStream();
                        longitude_string = readStream();
                        avg_price_string = readStream();
                        categories = readStream();
                        has_delivery = readStream().equals("y");
                        has_online = readStream().equals("y");

                        if(name.trim().isEmpty() || nation.trim().isEmpty() || city.trim().isEmpty() || address.trim().isEmpty() || categories.trim().isEmpty()) {
                            sendStream("missing");
                            break;
                        }

                        try {
                            latitude = Double.parseDouble(latitude_string);
                            longitude = Double.parseDouble(longitude_string);
                        } catch (NumberFormatException e) {
                            sendStream("coordinates");
                            break;
                        }

                        try {
                            price = Integer.parseInt(avg_price_string);
                        } catch (NumberFormatException e) {
                            sendStream("price_format");
                            break;
                        }

                        if(price < 0) {
                            sendStream("price_negative");
                            break;
                        }

                        if(DBHandler.editRestaurant(id, name, nation, city, address, latitude, longitude, price, categories, has_delivery, has_online))
                            sendStream("ok");
                        else
                            sendStream("error");
                    }
                    break;
                case "deleteRestaurant":
                    //if the user isn't a restaurator, he cannot delete a restaurants
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());

                    if(!DBHandler.hasAccess(user_id, id))
                        sendStream("no access");
                    else {
                        if(DBHandler.deleteRestaurant(id))
                            sendStream("ok");
                        else
                            sendStream("error");
                    }
                    break;
                case "getMyRestaurants":
                    //if the user isn't a restaurator, he cannot get his restaurants
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    //fetches restaurants data from the db
                    int page = Integer.parseInt(readStream());
                    String[][] restaurants_info = DBHandler.getUserRestaurants(user_id, page);
                    sendStream(Integer.toString(restaurants_info.length));

                    //send the data sequentially to the client
                    for(String[] restaurant_info : restaurants_info) {
                        sendStream(restaurant_info[0]);
                        sendStream(restaurant_info[1]);
                    }

                    break;
                case "getMyRestaurantsPages":
                    //if the user isn't a restaurator, he cannot get his restaurants
                    if(user_id < 1 || !DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    sendStream(Integer.toString(DBHandler.getUserRestaurantsPages(user_id)));
                    break;
                case "getRestaurantInfo":
                    id = Integer.parseInt(readStream());

                    String[] restaurant_info = DBHandler.getRestaurantInfo(id);

                    if(restaurant_info == null)
                        sendStream("not found");
                    else for(String info : restaurant_info)
                        sendStream(info);
                    break;
                case "getRestaurants":
                    //if the user is a restaurator, he cannot get all the restaurants
                    if(user_id > 0 && DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }
                    page = Integer.parseInt(readStream());
                    latitude_string = readStream();
                    longitude_string = readStream();
                    String range_km = readStream();
                    String price_min = readStream();
                    String price_max = readStream();
                    has_delivery = readStream().equals("y");
                    has_online = readStream().equals("y");
                    String stars_min = readStream();
                    String stars_max = readStream();
                    String category = null;
                    if(readStream().equals("y"))
                        category = readStream();
                    boolean near_me = readStream().equals("y");

                    boolean favourites = false;
                    if(user_id > 0)
                        favourites = readStream().equals("y");
                    
                    String[][] restaurants = Restaurant.getRestaurantsWithFilter(page, latitude_string, longitude_string, range_km, price_min, price_max, has_delivery, has_online, stars_min, stars_max, favourites ? user_id : -1, category, near_me ? user_id : -1);

                    if(restaurants[0][0].equals("error")) {
                        sendStream(restaurants[0][1]);
                        break;
                    }

                    sendStream("ok");

                    for(String[] restaurant : restaurants) {
                        sendStream(restaurant[0]);
                        sendStream(restaurant[1]);
                    }
                    break;
                case "addFavourite":
                    //if the user isn't a customer, he cannot add favourites
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    sendStream(DBHandler.setFavourite(user_id, id, true) ? "ok" : "error");

                    break;
                case "removeFavourite":
                    //if the user isn't a customer, he cannot remove favourites
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    sendStream(DBHandler.setFavourite(user_id, id, false) ? "ok" : "error");

                    break;
                case "isFavourite":
                    //if the user isn't a customer, he look out if a restaurant is in his favourites
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    sendStream(DBHandler.isFavourite(user_id, id) ? "y" : "n");

                    break;
                case "addReview":
                    //if the user isn't a customer, he cannot review a restaurant
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    int stars = Integer.parseInt(readStream());
                    String text = readStream();

                    if(stars < 1 || stars > 5) {
                        sendStream("stars");
                        break;
                    }

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    //if the restaurant isn't found
                    if(DBHandler.getRestaurantInfo(id) == null) {
                        sendStream("not found");
                        break;
                    }

                    sendStream(DBHandler.addReview(user_id, id, stars, text) ? "ok" : "error");

                    break;
                case "getMyReview":
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());

                    for(String s : DBHandler.getUserReview(user_id, id))
                        sendStream(s);

                    break;
                case "editReview":
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());
                    stars = Integer.parseInt(readStream());
                    text = readStream();

                    if(stars < 1 || stars > 5) {
                        sendStream("stars");
                        break;
                    }

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    sendStream(DBHandler.editReview(user_id, id, stars, text) ? "ok" : "error");
                    break;
                case "removeReview":
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    id = Integer.parseInt(readStream());

                    sendStream(DBHandler.removeReview(user_id, id) ? "ok" : "error");
                    break;
                case "getReviewsPages":
                    id = Integer.parseInt(readStream());
                    sendStream(Integer.toString(DBHandler.getReviewsPages(id)));
                    break;
                case "getReviews":
                    id = Integer.parseInt(readStream());
                    page = Integer.parseInt(readStream());

                    String[][] reviews = DBHandler.getReviews(id, page);

                    sendStream(Integer.toString(reviews.length));

                    for(String[] review : reviews) {
                        sendStream(review[0]);
                        sendStream(review[1]);
                        sendStream(review[2]);
                        if(review[3] == null)
                            sendStream("n");
                        else {
                            sendStream("y");
                            sendStream(review[3]);
                        }
                    }
                    
                    break;
                case "addResponse":
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    text = readStream();

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    sendStream(DBHandler.addResponse(id, text) ? "ok" : "error");
                    break;
                case "getResponse":
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    String response_text = DBHandler.getResponse(id);
                    if(response_text == null)
                        sendStream("empty");
                    else {
                        sendStream("ok");
                        sendStream(response_text);
                    }
                    break;
                case "editResponse":
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    text = readStream();

                    if(text.length() > 255) {
                        sendStream("text length");
                        break;
                    }

                    sendStream(DBHandler.editResponse(id, text) ? "ok" : "error");
                    break;
                case "removeResponse":
                    id = Integer.parseInt(readStream());
                    if(user_id < 1 || !DBHandler.canRespond(user_id, id)) {
                        sendStream("unauthorized");
                        break;
                    }

                    sendStream(DBHandler.removeResponse(id) ? "ok" : "error");
                    break;
                case "getMyReviewsPages":
                    //if the user isn't a customer, he cannot get his reviews
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    int num_pages = DBHandler.getUserReviewsPages(user_id);

                    if(num_pages < 0)
                        sendStream("error");
                    else {
                        sendStream("ok");
                        sendStream(Integer.toString(num_pages));
                    }

                    break;
                case "getMyReviews":
                    //if the user isn't a customer, he cannot get his reviews
                    if(user_id < 1 || DBHandler.getUserInfo(user_id)[2].equals("y")) {
                        sendStream("unauthorized");
                        break;
                    }

                    page = Integer.parseInt(readStream());

                    //review_format: {"restaurant name", "given stars", "review text"}
                    String[][] my_reviews = DBHandler.getUserReviews(user_id, page);

                    sendStream(Integer.toString(my_reviews.length));

                    for(String[] review : my_reviews) {
                        sendStream(review[0]);
                        sendStream(review[1]);
                        sendStream(review[2]);
                    }

                    break;
                case "logout":
                    user_id = -1;
                    sendStream("ok");
                    break;
                default:
                    sendStream("Unknown command");
                    break;
            }
            cmd = readStream();
        }
    }
}
