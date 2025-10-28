package com.example.demo2;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class GroupStatsUI {
    private CSVExportService csvExportService = new CSVExportService();
    private TableView<GroupStats> statsTableView = new TableView<>();

    public void showStatsDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Group Statistics");

        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(10));

        setupStatsTable();
        updateStatsTable();

        Button exportButton = new Button("Export to CSV");
        exportButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Group Stats CSV File");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("group_stats.csv");

            File file = fileChooser.showSaveDialog(dialog);
            if (file != null) {
                csvExportService.exportGroupStatsToCSV(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export Complete",
                        "Group statistics exported to " + file.getName());
            }
        });

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialog.close());

        HBox buttonBox = new HBox(10, exportButton, closeButton);
        buttonBox.setPadding(new Insets(10));

        mainLayout.getChildren().addAll(
                new Label("Group Statistics"),
                statsTableView,
                buttonBox
        );

        Scene scene = new Scene(mainLayout, 600, 400);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void setupStatsTable() {
        TableColumn<GroupStats, String> nameCol = new TableColumn<>("Group Name");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGroupName()));

        TableColumn<GroupStats, Number> empCountCol = new TableColumn<>("Employee Count");
        empCountCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleLongProperty(getEmployeeCount(cellData.getValue().getGroupId())));

        TableColumn<GroupStats, Number> countCol = new TableColumn<>("Rating Count");
        countCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleLongProperty(cellData.getValue().getRatingCount()));

        TableColumn<GroupStats, Number> avgCol = new TableColumn<>("Average Rating");
        avgCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getAverageRating()));
        avgCol.setCellFactory(col -> new TableCell<GroupStats, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item.doubleValue()));
                }
            }
        });

        statsTableView.getColumns().addAll(nameCol, empCountCol, countCol, avgCol);
    }

    private long getEmployeeCount(Long groupId) {
        if (groupId == null) return 0;

        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "select count(e) from Employee e where e.group.id = :groupId", Long.class)
                    .setParameter("groupId", groupId)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void updateStatsTable() {
        List<GroupStats> stats = csvExportService.getGroupStatsByCriteria();
        ObservableList<GroupStats> statsData = FXCollections.observableArrayList(stats);
        statsTableView.setItems(statsData);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}