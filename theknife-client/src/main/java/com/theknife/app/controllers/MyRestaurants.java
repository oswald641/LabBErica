package com.theknife.app.controllers;

import java.io.IOException;

import com.theknife.app.Communicator;
import com.theknife.app.EditingRestaurant;
import com.theknife.app.SceneManager;
import com.theknife.app.User;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

public class MyRestaurants {
    @FXML
    private ListView<String> restaurants_container;
    @FXML
    private Label no_restaurants_label, page_label;
    @FXML
    private Button edit_btn, reviews_btn, prev_btn, next_btn;

    private int[] restaurants_ids;
    private String[] restaurants_names;
    private int total_pages, current_page = 0;

    @FXML
    private void initialize() throws IOException {
        EditingRestaurant.reset();
        Communicator.sendStream("getMyRestaurantsPages");
        total_pages = Integer.parseInt(Communicator.readStream());
        if(total_pages > 0)
            changePage(0);
        else
            no_restaurants_label.setVisible(true);
    }

    private void changePage(int page) throws IOException {
        page_label.setText(Integer.toString(page + 1) + "/" + Integer.toString(total_pages));
        prev_btn.setDisable(page < 1);
        next_btn.setDisable(page + 1 >= total_pages);

        Communicator.sendStream("getMyRestaurants");
        Communicator.sendStream(Integer.toString(page));
        int size = Integer.parseInt(Communicator.readStream());

        restaurants_ids = new int[size];
        restaurants_names = new String[size];

        for(int i = 0; i < size; i++) {
            restaurants_ids[i] = Integer.parseInt(Communicator.readStream());
            restaurants_names[i] = Communicator.readStream();
        }

        restaurants_container.getItems().setAll(restaurants_names);
        checkSelected();
    }

    @FXML
    private void prevPage() throws IOException {
        changePage(--current_page);
    }

    @FXML
    private void nextPage() throws IOException {
        changePage(++current_page);
    }

    @FXML
    private void checkSelected() {
        int index = restaurants_container.getSelectionModel().getSelectedIndex();
        edit_btn.setDisable(index < 0);
        reviews_btn.setDisable(index < 0);
    }

    @FXML
    private void editSelected() throws IOException {
        int restaurant_id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        EditingRestaurant.setEditing(restaurant_id);
        SceneManager.changeScene("EditRestaurant");
    }

    @FXML
    private void viewReviews() {
        int restaurant_id = restaurants_ids[restaurants_container.getSelectionModel().getSelectedIndex()];
        System.out.println("View reviews with this id: " + restaurant_id);
    }

    @FXML
    private void logout() throws IOException {
        User.logout();
        SceneManager.changeScene("App");
    }

    @FXML
    private void addRestaurant() throws IOException {
        SceneManager.changeScene("EditRestaurant");
    }
}
