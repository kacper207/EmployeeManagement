package com.example.demo2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class RateManagementUI {
    private RateService rateService = new RateService();
    private DatabaseService dbService;
    private EmployeeGroup currentGroup;
    private TableView<Rate> rateTableView = new TableView<>();
    private ObservableList<Rate> rateData = FXCollections.observableArrayList();

    public RateManagementUI(DatabaseService dbService, EmployeeGroup group) {
        this.dbService = dbService;
        this.currentGroup = group;
    }

    public void showRateManagementDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Group Rating Management - " + currentGroup.getName());

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        setupRateTable();
        updateRateTable();

        Double avgRating = rateService.getAverageRatingForGroup(currentGroup.getId());
        Label avgRatingLabel = new Label("Average rating: " +
                (avgRating != null ? String.format("%.2f", avgRating) : "No ratings yet"));

        GridPane addForm = createAddRateForm(avgRatingLabel);

        Button deleteButton = new Button("Delete Selected");
        deleteButton.setOnAction(e -> {
            Rate selectedRate = rateTableView.getSelectionModel().getSelectedItem();
            if (selectedRate != null) {
                try {
                    Long rateId = selectedRate.getId();
                    System.out.println("Attempting to delete rate with ID: " + rateId);

                    rateService.deleteRate(selectedRate);

                    updateRateTable();
                    Double newAvg = rateService.getAverageRatingForGroup(currentGroup.getId());
                    avgRatingLabel.setText("Average rating: " +
                            (newAvg != null ? String.format("%.2f", newAvg) : "No ratings yet"));

                    showAlert(Alert.AlertType.INFORMATION, "Success", "Rating successfully deleted.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "Failed to delete rating: " + ex.getMessage());
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a rate to delete.");
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10, deleteButton, closeButton);
        buttonBox.setPadding(new Insets(10));

        mainLayout.getChildren().addAll(
                new Label("Ratings for group: " + currentGroup.getName()),
                avgRatingLabel,
                rateTableView,
                buttonBox,
                new Separator(),
                addForm
        );

        Scene scene = new Scene(mainLayout, 600, 500);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void setupRateTable() {
        rateTableView.getColumns().clear();

        TableColumn<Rate, Number> valueCol = new TableColumn<>("Rating");
        valueCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getValue()));

        TableColumn<Rate, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getRateDate()));

        TableColumn<Rate, String> commentCol = new TableColumn<>("Comment");
        commentCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getComment()));

        TableColumn<Rate, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleLongProperty(cellData.getValue().getId()));
        idCol.setPrefWidth(40);

        valueCol.setPrefWidth(80);
        dateCol.setPrefWidth(120);
        commentCol.setPrefWidth(310);

        rateTableView.getColumns().addAll(idCol, valueCol, dateCol, commentCol);
    }

    private void updateRateTable() {
        if (currentGroup != null) {
            List<Rate> rates = rateService.getRatesByGroup(currentGroup);
            rateData = FXCollections.observableArrayList(rates);
            rateTableView.setItems(rateData);

            System.out.println("Updated rate table, found " + rates.size() + " ratings");
        }
    }

    private GridPane createAddRateForm(Label avgRatingLabel) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        Slider ratingSlider = new Slider(0, 6, 3);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);

        Label sliderValueLabel = new Label("3.0");
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sliderValueLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });

        HBox sliderBox = new HBox(10, ratingSlider, sliderValueLabel);

        DatePicker datePicker = new DatePicker(LocalDate.now());

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Optional comment");
        commentArea.setPrefRowCount(3);

        grid.add(new Label("Rating (0-6):"), 0, 0);
        grid.add(sliderBox, 1, 0);
        grid.add(new Label("Date:"), 0, 1);
        grid.add(datePicker, 1, 1);
        grid.add(new Label("Comment:"), 0, 2);
        grid.add(commentArea, 1, 2);

        Button addButton = new Button("Add Rating");
        grid.add(addButton, 1, 3);

        addButton.setOnAction(e -> {
            try {
                double value = ratingSlider.getValue();
                LocalDate rateDate = datePicker.getValue();
                String comment = commentArea.getText();

                if (rateDate == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Please select a date.");
                    return;
                }

                Rate newRate = new Rate(value, currentGroup, rateDate, comment);
                rateService.saveRate(newRate);

                updateRateTable();

                Double newAvg = rateService.getAverageRatingForGroup(currentGroup.getId());
                avgRatingLabel.setText("Average rating: " +
                        (newAvg != null ? String.format("%.2f", newAvg) : "No ratings yet"));

                ratingSlider.setValue(3.0);
                datePicker.setValue(LocalDate.now());
                commentArea.clear();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Rating added successfully.");

            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add rating: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        return grid;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }
}