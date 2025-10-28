package com.example.demo2;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

public class EmployeeManagementApp extends Application {

    private DatabaseService dbService = new DatabaseService();
    private CSVExportService csvExportService = new CSVExportService();
    private GroupContainer organizacja = new GroupContainer();
    private TableView<Employee> tableView = new TableView<>();
    private ListView<String> groupListView = new ListView<>();
    private EmployeeGroup currentGroup;
    private ObservableList<Employee> employeeData = FXCollections.observableArrayList();
    private TextField filterField = new TextField();
    private DecimalFormat percentFormat = new DecimalFormat("#0.0%");

    @Override
    public void start(Stage primaryStage) {
        HibernateUtil.getSessionFactory();
        loadDataFromDatabase();

        HBox mainLayout = new HBox(10);
        mainLayout.setPadding(new Insets(10));

        VBox groupsPanel = createGroupsPanel();
        VBox employeesPanel = createEmployeesPanel();

        mainLayout.getChildren().addAll(groupsPanel, employeesPanel);

        Scene scene = new Scene(mainLayout, 1000, 600);
        primaryStage.setTitle("Employee Management");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (!organizacja.getGroups().isEmpty()) {
            groupListView.getSelectionModel().selectFirst();
        }
    }

    private VBox createGroupsPanel() {
        VBox groupsPanel = new VBox(10);
        groupsPanel.setPadding(new Insets(10));
        groupsPanel.getChildren().add(new Label("Groups:"));
        updateGroupList();

        groupListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String groupName = extractGroupName(newVal);
                currentGroup = organizacja.getGroup(groupName);
                filterField.clear();
                updateEmployeeTable();
            } else {
                currentGroup = null;
                tableView.getItems().clear();
            }
        });

        GridPane addGroupForm = createAddGroupForm();

        // Add buttons for group statistics and export
        Button statsButton = new Button("Group Statistics");
        statsButton.setOnAction(e -> {
            GroupStatsUI statsUI = new GroupStatsUI();
            statsUI.showStatsDialog();
        });

        groupsPanel.getChildren().addAll(groupListView, addGroupForm, statsButton);
        return groupsPanel;
    }

    private VBox createEmployeesPanel() {
        VBox employeesPanel = new VBox(10);
        employeesPanel.setPadding(new Insets(10));

        setupTable();

        Label filterLabel = new Label("Filter by surname:");
        filterField.setPromptText("Enter the surname");
        filterField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                updateEmployeeTable();
            }
        });
        HBox filterBox = new HBox(5, filterLabel, filterField);
        filterBox.setPadding(new Insets(5, 0, 5, 0));

        GridPane addForm = createAddEmployeeForm();
        HBox actionButtons = createActionButtons();

        employeesPanel.getChildren().addAll(
                new Label("Employee list:"),
                filterBox,
                tableView,
                actionButtons,
                addForm
        );
        return employeesPanel;
    }

    private String extractGroupName(String displayText) {
        if (displayText.contains("[")) {
            return displayText.substring(0, displayText.indexOf("[")).trim();
        }
        return displayText;
    }

    private void updateGroupList() {
        ObservableList<String> groupNames = FXCollections.observableArrayList();
        RateService rateService = new RateService();

        for (String groupName : organizacja.getGroups().keySet()) {
            EmployeeGroup group = organizacja.getGroup(groupName);
            if (group != null) {
                int currentSize = group.getEmployees().size();
                int maxSize = group.getMaxSize();
                double fillPercentage = (maxSize > 0) ? (double) currentSize / maxSize : 0.0;

                Double avgRating = null;
                if (group.getId() != null) {
                    avgRating = rateService.getAverageRatingForGroup(group.getId());
                }

                String ratingText = avgRating != null ? String.format(" Rating: %.1f", avgRating) : "";

                String formattedGroupName = String.format("%s [%s - %d/%d]%s",
                        groupName,
                        percentFormat.format(fillPercentage),
                        currentSize,
                        maxSize,
                        ratingText);
                groupNames.add(formattedGroupName);
            }
        }
        groupListView.setItems(groupNames);
    }

    private HBox createActionButtons() {
        HBox buttons = new HBox(10);
        buttons.setPadding(new Insets(10));

        Button modifyButton = new Button("Modify Data");
        modifyButton.setOnAction(e -> {
            Employee selected = tableView.getSelectionModel().getSelectedItem();
            if (selected != null && currentGroup != null) {
                showModifyDialog(selected);
            } else showAlert("Error", "Please select an employee first.");
        });

        Button sortButton = new Button("Sort");
        sortButton.setOnAction(e -> sortEmployees());

        Button rateButton = new Button("Group Ratings");
        rateButton.setOnAction(e -> {
            if (currentGroup != null) {
                RateManagementUI rateManagementUI = new RateManagementUI(dbService, currentGroup);
                rateManagementUI.showRateManagementDialog();
            } else {
                showAlert("Error", "Please select a group first.");
            }
        });

        // Export to CSV button
        Button exportButton = new Button("Export to CSV");
        exportButton.setOnAction(e -> exportToCSV());

        buttons.getChildren().addAll(modifyButton, sortButton, rateButton, exportButton);
        return buttons;
    }

    private void exportToCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("employees.csv");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                csvExportService.exportEmployeesToCSV(file.getAbsolutePath());
                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                        "Employee data has been exported to " + file.getName());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Export Failed",
                        "Failed to export data: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void sortEmployees() {
        if (currentGroup != null && tableView.getItems() != null) {
            Comparator<Employee> employeeComparator = Comparator
                    .comparing(Employee::getLastName, String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(Employee::getFirstName, String.CASE_INSENSITIVE_ORDER);
            FXCollections.sort(tableView.getItems(), employeeComparator);
        }
    }

    private void showModifyDialog(Employee employee) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Modify Employee Data");

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label statusLabel = new Label("Current Status: " + employee.getCondition());
        grid.add(statusLabel, 0, 0, 2, 1);

        ComboBox<EmployeeCondition> conditionCombo = new ComboBox<>();
        conditionCombo.getItems().addAll(EmployeeCondition.values());
        conditionCombo.setValue(employee.getCondition());
        grid.add(new Label("New Status:"), 0, 1);
        grid.add(conditionCombo, 1, 1);

        Button applyStatusButton = new Button("Apply Status Change");
        grid.add(applyStatusButton, 1, 2);

        Label salaryLabel = new Label("Current Salary: " + employee.getSalary());
        grid.add(salaryLabel, 0, 3, 2, 1);

        TextField salaryField = new TextField();
        salaryField.setPromptText("Amount to add");
        grid.add(new Label("Add to Salary:"), 0, 4);
        grid.add(salaryField, 1, 4);

        Button applySalaryButton = new Button("Apply Salary Change");
        grid.add(applySalaryButton, 1, 5);

        Button removeButton = new Button("Remove Employee");
        grid.add(removeButton, 0, 7, 2, 1);

        applyStatusButton.setOnAction(e -> {
            EmployeeCondition newCondition = conditionCombo.getValue();
            if (newCondition != null) {
                employee.setCondition(newCondition);
                statusLabel.setText("Current Status: " + newCondition);
                tableView.refresh();
                dbService.updateEmployee(employee);
                updateGroupList();
            }
        });

        applySalaryButton.setOnAction(e -> {
            try {
                String text = salaryField.getText();
                if (text != null && !text.trim().isEmpty()) {
                    double amount = Double.parseDouble(text);
                    double newSalary = employee.getSalary() + amount;
                    employee.setSalary(newSalary);
                    salaryLabel.setText("Current Salary: " + newSalary);
                    salaryField.clear();
                    dbService.updateEmployee(employee);
                    tableView.refresh();
                } else showAlert("Error", "Amount cannot be empty.");
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid amount format.");
            }
        });

        removeButton.setOnAction(e -> {
            if (currentGroup != null) {
                dbService.deleteEmployee(employee);
                currentGroup.removeEmployee(employee);
                updateEmployeeTable();
                updateGroupList();
                dialog.close();
            }
        });

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }

    private GridPane createAddGroupForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));
        TextField nameField = new TextField();
        nameField.setPromptText("Group name");
        TextField capacityField = new TextField();
        capacityField.setPromptText("Capacity");
        Button addButton = new Button("Add Group");
        Button removeButton = new Button("Remove Group");
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Capacity:"), 0, 1);
        grid.add(capacityField, 1, 1);
        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(addButton, removeButton);
        grid.add(buttons, 1, 2);

        addButton.setOnAction(e -> {
            try {
                String name = nameField.getText();
                int capacity = Integer.parseInt(capacityField.getText());
                if (name == null || name.trim().isEmpty()) {
                    showAlert("Error", "Enter a group name");
                    return;
                }
                if (organizacja.getGroups().containsKey(name)) {
                    showAlert("Error", "A group with this name already exists.");
                    return;
                }
                if (capacity <= 0) {
                    showAlert("Error", "Capacity must be a positive number.");
                    return;
                }

                EmployeeGroup newGroup = new EmployeeGroup(name, capacity);
                dbService.saveGroup(newGroup);
                organizacja.addClass(name, capacity);
                EmployeeGroup inMemoryGroup = organizacja.getGroup(name);
                inMemoryGroup.setId(newGroup.getId());
                updateGroupList();
                nameField.clear();
                capacityField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid capacity format");
            }
        });

        removeButton.setOnAction(e -> {
            String selectedDisplayName = groupListView.getSelectionModel().getSelectedItem();
            if (selectedDisplayName != null) {
                String selectedGroupName = extractGroupName(selectedDisplayName);
                EmployeeGroup groupToRemove = organizacja.getGroup(selectedGroupName);

                if (groupToRemove != null) {
                    dbService.deleteGroup(groupToRemove);
                    organizacja.removeClass(selectedGroupName);
                    updateGroupList();

                    if (currentGroup != null && currentGroup.getName().equals(selectedGroupName)) {
                        currentGroup = null;
                        tableView.getItems().clear();
                    }
                    if (!organizacja.getGroups().isEmpty()) {
                        groupListView.getSelectionModel().selectFirst();
                    } else {
                        tableView.getItems().clear();
                    }
                }
            } else {
                showAlert("Error", "Select a group to remove");
            }
        });
        return grid;
    }

    private void setupTable() {
        TableColumn<Employee, String> firstNameCol = new TableColumn<>("First Name");
        firstNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFirstName()));
        TableColumn<Employee, String> lastNameCol = new TableColumn<>("Last Name");
        lastNameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLastName()));
        TableColumn<Employee, String> conditionCol = new TableColumn<>("Status");
        conditionCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCondition().toString()));
        TableColumn<Employee, String> birthYearCol = new TableColumn<>("Birth Year");
        birthYearCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getBirthYear())));
        TableColumn<Employee, String> salaryCol = new TableColumn<>("Salary");
        salaryCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(String.valueOf(cellData.getValue().getSalary())));
        firstNameCol.setSortable(true);
        lastNameCol.setSortable(true);
        conditionCol.setSortable(true);
        birthYearCol.setSortable(true);
        salaryCol.setSortable(true);
        tableView.getColumns().addAll(firstNameCol, lastNameCol, conditionCol, birthYearCol, salaryCol);
    }

    private void updateEmployeeTable() {
        if (currentGroup != null) {
            List<Employee> allEmployeesInGroup = currentGroup.getEmployees();
            String filterText = filterField.getText();
            List<Employee> filteredEmployees;
            if (filterText == null || filterText.trim().isEmpty()) {
                filteredEmployees = new ArrayList<>(allEmployeesInGroup);
            } else {
                String lowerCaseFilter = filterText.toLowerCase().trim();
                filteredEmployees = allEmployeesInGroup.stream()
                        .filter(emp -> emp.getLastName().toLowerCase().contains(lowerCaseFilter))
                        .collect(Collectors.toList());
            }
            employeeData = FXCollections.observableArrayList(filteredEmployees);
            tableView.setItems(employeeData);
            if (!tableView.getSortOrder().isEmpty()) tableView.sort();
        } else {
            tableView.getItems().clear();
        }
    }

    private GridPane createAddEmployeeForm() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));
        TextField firstNameField = new TextField();
        TextField lastNameField = new TextField();
        ComboBox<EmployeeCondition> conditionCombo = new ComboBox<>();
        conditionCombo.getItems().addAll(EmployeeCondition.values());
        TextField birthYearField = new TextField();
        TextField salaryField = new TextField();
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Status:"), 0, 2);
        grid.add(conditionCombo, 1, 2);
        grid.add(new Label("Birth Year:"), 0, 3);
        grid.add(birthYearField, 1, 3);
        grid.add(new Label("Salary:"), 0, 4);
        grid.add(salaryField, 1, 4);
        Button addButton = new Button("Add Employee");
        grid.add(addButton, 1, 5);

        addButton.setOnAction(e -> {
            if (currentGroup == null) {
                showAlert("Error", "Select a group first");
                return;
            }
            if (currentGroup.getEmployees().size() >= currentGroup.getMaxSize()) {
                showAlert("Error", "Group '" + currentGroup.getName() + "' is full. Cannot add an employee.");
                return;
            }
            try {
                String firstName = firstNameField.getText();
                String lastName = lastNameField.getText();
                EmployeeCondition condition = conditionCombo.getValue();
                int birthYear = Integer.parseInt(birthYearField.getText());
                double salary = Double.parseDouble(salaryField.getText());

                if (firstName == null || firstName.trim().isEmpty() ||
                        lastName == null || lastName.trim().isEmpty() || condition == null) {
                    showAlert("Error", "First name, last name, and status are required.");
                    return;
                }
                if (birthYear <= 1900 || birthYear > java.time.Year.now().getValue()) {
                    showAlert("Error", "Invalid birth year.");
                    return;
                }
                if (salary < 0) {
                    showAlert("Error", "Salary cannot be negative.");
                    return;
                }

                Employee newEmployee = new Employee(firstName, lastName, condition, birthYear, salary);
                newEmployee.setGroup(currentGroup);
                dbService.saveEmployee(newEmployee);
                currentGroup.addEmployee(newEmployee);
                updateEmployeeTable();
                updateGroupList();

                firstNameField.clear();
                lastNameField.clear();
                conditionCombo.setValue(null);
                birthYearField.clear();
                salaryField.clear();
            } catch (NumberFormatException ex) {
                showAlert("Error", "Invalid numerical data format (Birth Year, Salary).");
            }
        });

        return grid;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadDataFromDatabase() {
        organizacja = new GroupContainer();
        List<EmployeeGroup> groupsFromDB = dbService.getAllGroupsWithEmployees();

        for (EmployeeGroup group : groupsFromDB) {
            organizacja.addClass(group.getName(), group.getMaxSize());
            EmployeeGroup inMemoryGroup = organizacja.getGroup(group.getName());
            inMemoryGroup.setId(group.getId());

            if (group.getEmployees() != null) {
                for (Employee employee : group.getEmployees()) {
                    inMemoryGroup.getEmployees().add(employee);
                    employee.setGroup(inMemoryGroup);
                }
            }
        }

        updateGroupList();
    }

    @Override
    public void stop() {
        HibernateUtil.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}