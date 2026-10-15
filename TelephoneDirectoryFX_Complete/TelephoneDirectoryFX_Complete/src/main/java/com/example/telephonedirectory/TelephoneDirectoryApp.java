package com.example.telephonedirectory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Optional;

public class TelephoneDirectoryApp extends Application {

    private Stage stage;
    private final TableView<Contact> table = new TableView<>();
    private final ObservableList<Contact> contacts = FXCollections.observableArrayList();

    private final TextField idField = new TextField();
    private final TextField nameField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField addressField = new TextField();
    private final TextField searchField = new TextField();
    private final Label statusLabel = new Label("Ready");
    private final Label countLabel = new Label("Total Contacts: 0");

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        showLogin();
    }

    // ========================= LOGIN =========================
    private void showLogin() {
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        TextField visiblePassword = new TextField();

        username.setPromptText("Username");
        password.setPromptText("Password");
        visiblePassword.setPromptText("Password");
        visiblePassword.setVisible(false);
        visiblePassword.setManaged(false);

        CheckBox showPassword = new CheckBox("Show password");
        showPassword.setOnAction(e -> {
            boolean show = showPassword.isSelected();
            visiblePassword.setText(password.getText());
            visiblePassword.setVisible(show);
            visiblePassword.setManaged(show);
            password.setVisible(!show);
            password.setManaged(!show);
        });

        password.textProperty().addListener((obs, oldValue, newValue) -> visiblePassword.setText(newValue));
        visiblePassword.textProperty().addListener((obs, oldValue, newValue) -> password.setText(newValue));

        Label title = new Label("TELEPHONE DIRECTORY");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #17324d;");
        Label subtitle = new Label("Secure Contact Management System");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #607080;");

        VBox card = new VBox(14,
                title,
                subtitle,
                separator(),
                fieldLabel("Username"), username,
                fieldLabel("Password"), password, visiblePassword,
                showPassword
        );
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setMaxWidth(420);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #d9e2ec; -fx-border-radius: 14;");

        Button login = button("LOGIN", "#1565c0", 140);
        Button exit = button("EXIT", "#607d8b", 140);
        HBox buttons = new HBox(12, login, exit);
        buttons.setAlignment(Pos.CENTER);

        Label hint = new Label("Default login: admin / admin123");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #7b8794;");

        card.getChildren().addAll(buttons, hint);

        login.setOnAction(e -> {
            String user = username.getText().trim();
            String pass = password.getText();
            if (authenticate(user, pass)) {
                showDashboard(user);
            } else {
                showWarning("Invalid username or password.");
                password.clear();
                password.requestFocus();
            }
        });
        password.setOnAction(e -> login.fire());
        visiblePassword.setOnAction(e -> login.fire());
        exit.setOnAction(e -> PlatformExit());

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #eaf2fb, #f8fafc);");

        Scene scene = new Scene(root, 850, 600);
        stage.setTitle("Telephone Directory - Login");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    private boolean authenticate(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) return false;
        String sql = "SELECT password_hash FROM users WHERE username=? AND active=1";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("password_hash").equals(hashPassword(password));
            }
            return false;
        } catch (SQLException ex) {
            showError("Database Error", "Could not verify login.\n" + ex.getMessage());
            return false;
        }
    }

    // ========================= DASHBOARD =========================
    private void showDashboard(String username) {
        Label title = new Label("TELEPHONE DIRECTORY");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #17324d;");

        Label welcome = new Label("Welcome, " + username);
        welcome.setStyle("-fx-font-size: 14px; -fx-text-fill: #607080;");

        VBox header = new VBox(4, title, welcome);
        header.setAlignment(Pos.CENTER);

        VBox card1 = statCard("CONTACT MANAGEMENT", "Add, update, delete and search contacts", "#1565c0");
        VBox card2 = statCard("DATABASE", "MySQL connected contact storage", "#2e7d32");
        VBox card3 = statCard("SECURITY", "Login protected application", "#6a1b9a");
        HBox cards = new HBox(18, card1, card2, card3);
        cards.setAlignment(Pos.CENTER);
        HBox.setHgrow(card1, Priority.ALWAYS);
        HBox.setHgrow(card2, Priority.ALWAYS);
        HBox.setHgrow(card3, Priority.ALWAYS);

        Button manage = button("MANAGE CONTACTS", "#1565c0", 180);
        Button about = button("ABOUT", "#607d8b", 120);
        Button logout = button("LOGOUT", "#c62828", 120);
        HBox actions = new HBox(12, manage, about, logout);
        actions.setAlignment(Pos.CENTER);

        manage.setOnAction(e -> showDirectory(username));
        about.setOnAction(e -> showAbout());
        logout.setOnAction(e -> {
            if (confirm("Logout", "Do you want to logout?")) showLogin();
        });

        VBox root = new VBox(30, header, cards, actions);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: #f4f7fb;");

        Scene scene = new Scene(root, 1000, 650);
        stage.setTitle("Telephone Directory - Dashboard");
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private VBox statCard(String heading, String description, String color) {
        Label h = new Label(heading);
        h.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        Label d = new Label(description);
        d.setWrapText(true);
        d.setStyle("-fx-font-size: 12px; -fx-text-fill: #52606d;");
        VBox box = new VBox(10, h, d);
        box.setPadding(new Insets(20));
        box.setPrefHeight(120);
        box.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #d9e2ec; -fx-border-radius: 10;");
        return box;
    }

    // ========================= DIRECTORY =========================
    private void showDirectory(String username) {
        configureFields();
        configureTable();

        Label title = new Label("TELEPHONE DIRECTORY");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #17324d;");
        Label subtitle = new Label("Manage your contacts easily");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #607080;");
        VBox heading = new VBox(4, title, subtitle);
        heading.setAlignment(Pos.CENTER);

        Button dashboard = button("Dashboard", "#546e7a", 105);
        Button logout = button("Logout", "#c62828", 95);
        HBox topActions = new HBox(8, dashboard, logout);
        topActions.setAlignment(Pos.CENTER_RIGHT);
        BorderPane top = new BorderPane();
        top.setCenter(heading);
        top.setRight(topActions);
        BorderPane.setAlignment(topActions, Pos.CENTER_RIGHT);

        GridPane form = createForm();
        HBox actionButtons = createActionButtons();
        HBox searchBox = createSearchBox();

        HBox bottomBar = new HBox(15, countLabel, statusLabel);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);
        statusLabel.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(14, top, form, actionButtons, searchBox, table, bottomBar);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f4f7fb;");
        VBox.setVgrow(table, Priority.ALWAYS);

        dashboard.setOnAction(e -> showDashboard(username));
        logout.setOnAction(e -> {
            if (confirm("Logout", "Do you want to logout?")) showLogin();
        });

        Scene scene = new Scene(root, 1200, 760);
        stage.setTitle("Telephone Directory - Contacts");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setOnCloseRequest(e -> {
            if (!confirm("Exit", "Are you sure you want to close the application?")) e.consume();
        });
        loadContacts();
    }

    private void configureFields() {
        idField.setEditable(false);
        idField.setPromptText("Auto");
        nameField.setPromptText("Enter full name");
        phoneField.setPromptText("Enter phone number");
        emailField.setPromptText("Enter email address");
        addressField.setPromptText("Enter address");
        searchField.setPromptText("Search by name, phone or email...");
        for (TextField field : new TextField[]{idField, nameField, phoneField, emailField, addressField, searchField}) {
            field.setPrefHeight(34);
        }
    }

    private void configureTable() {
        table.getColumns().clear();
        TableColumn<Contact, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Contact, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Contact, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        TableColumn<Contact, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<Contact, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        table.getColumns().setAll(idCol, nameCol, phoneCol, emailCol, addressCol);
        table.setItems(contacts);
        table.setPlaceholder(new Label("No contacts found"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(420);
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) showSelectedContact(selected);
        });
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10); form.setPadding(new Insets(14));
        form.setStyle("-fx-background-color: white; -fx-border-color: #d9e2ec; -fx-border-radius: 8; -fx-background-radius: 8;");
        form.add(label("ID"), 0, 0); form.add(idField, 1, 0);
        form.add(label("Name"), 2, 0); form.add(nameField, 3, 0);
        form.add(label("Phone"), 0, 1); form.add(phoneField, 1, 1);
        form.add(label("Email"), 2, 1); form.add(emailField, 3, 1);
        form.add(label("Address"), 0, 2); form.add(addressField, 1, 2, 3, 1);
        ColumnConstraints c0 = new ColumnConstraints(70), c1 = new ColumnConstraints(), c2 = new ColumnConstraints(70), c3 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS); c3.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().setAll(c0, c1, c2, c3);
        return form;
    }

    private Label label(String text) {
        Label label = new Label(text + ":");
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #334e68;");
        return label;
    }

    private HBox createActionButtons() {
        Button add = button("Add", "#2e7d32", 100);
        Button update = button("Update", "#1565c0", 100);
        Button delete = button("Delete", "#c62828", 100);
        Button clear = button("Clear", "#607d8b", 100);
        add.setOnAction(e -> addContact()); update.setOnAction(e -> updateContact());
        delete.setOnAction(e -> deleteContact()); clear.setOnAction(e -> clearFields());
        HBox box = new HBox(10, add, update, delete, clear); box.setAlignment(Pos.CENTER);
        return box;
    }

    private HBox createSearchBox() {
        Button search = new Button("Search"), showAll = new Button("Show All"), clearSearch = new Button("Clear Search");
        search.setPrefHeight(34); showAll.setPrefHeight(34); clearSearch.setPrefHeight(34);
        search.setOnAction(e -> searchContacts());
        showAll.setOnAction(e -> { searchField.clear(); loadContacts(); });
        clearSearch.setOnAction(e -> { searchField.clear(); loadContacts(); });
        searchField.setOnAction(e -> searchContacts());
        HBox box = new HBox(10, searchField, search, showAll, clearSearch);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        return box;
    }

    private void loadContacts() {
        contacts.clear();
        String sql = "SELECT id, name, phone, email, address FROM contacts ORDER BY id DESC";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) contacts.add(readContact(rs));
            updateCount(); setStatus("Loaded contacts successfully.");
        } catch (SQLException ex) { showError("Database Error", ex.getMessage()); }
    }

    private void searchContacts() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadContacts(); return; }
        contacts.clear();
        String sql = "SELECT id, name, phone, email, address FROM contacts WHERE name LIKE ? OR phone LIKE ? OR email LIKE ? OR address LIKE ? ORDER BY name";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            String value = "%" + keyword + "%";
            for (int i = 1; i <= 4; i++) ps.setString(i, value);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) contacts.add(readContact(rs)); }
            updateCount(); setStatus("Search completed.");
        } catch (SQLException ex) { showError("Database Error", ex.getMessage()); }
    }

    private Contact readContact(ResultSet rs) throws SQLException {
        return new Contact(rs.getInt("id"), rs.getString("name"), rs.getString("phone"), rs.getString("email"), rs.getString("address"));
    }

    private void addContact() {
        if (!validateFields()) return;
        if (phoneExists(phoneField.getText().trim(), null)) { showWarning("This phone number already exists."); phoneField.requestFocus(); return; }
        String sql = "INSERT INTO contacts(name, phone, email, address) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            setContactParameters(ps, 1); ps.executeUpdate();
            showInfo("Success", "Contact added successfully."); clearFields(); loadContacts(); setStatus("Contact added.");
        } catch (SQLException ex) { showError("Database Error", ex.getMessage()); }
    }

    private void updateContact() {
        if (idField.getText().isEmpty()) { showWarning("Select a contact from the table first."); return; }
        if (!validateFields()) return;
        int id = Integer.parseInt(idField.getText());
        if (phoneExists(phoneField.getText().trim(), id)) { showWarning("This phone number is already used by another contact."); phoneField.requestFocus(); return; }
        String sql = "UPDATE contacts SET name=?, phone=?, email=?, address=? WHERE id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            setContactParameters(ps, 1); ps.setInt(5, id); ps.executeUpdate();
            showInfo("Success", "Contact updated successfully."); clearFields(); loadContacts(); setStatus("Contact updated.");
        } catch (SQLException ex) { showError("Database Error", ex.getMessage()); }
    }

    private void setContactParameters(PreparedStatement ps, int startIndex) throws SQLException {
        ps.setString(startIndex, nameField.getText().trim());
        ps.setString(startIndex + 1, phoneField.getText().trim());
        ps.setString(startIndex + 2, emptyToNull(emailField.getText().trim()));
        ps.setString(startIndex + 3, emptyToNull(addressField.getText().trim()));
    }

    private String emptyToNull(String value) { return value.isEmpty() ? null : value; }

    private boolean phoneExists(String phone, Integer excludeId) {
        String sql = excludeId == null ? "SELECT COUNT(*) FROM contacts WHERE phone=?" : "SELECT COUNT(*) FROM contacts WHERE phone=? AND id<>?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, phone); if (excludeId != null) ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) { rs.next(); return rs.getInt(1) > 0; }
        } catch (SQLException ex) { showError("Database Error", ex.getMessage()); return true; }
    }

    private void deleteContact() {
        if (idField.getText().isEmpty()) { showWarning("Select a contact from the table first."); return; }
        if (!confirm("Delete Contact", "Delete selected contact?\n\n" + nameField.getText())) return;
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM contacts WHERE id=?")) {
            ps.setInt(1, Integer.parseInt(idField.getText())); ps.executeUpdate();
            showInfo("Success", "Contact deleted successfully."); clearFields(); loadContacts(); setStatus("Contact deleted.");
        } catch (SQLException ex) { showError("Database Error", ex.getMessage()); }
    }

    private boolean validateFields() {
        String name = nameField.getText().trim(), phone = phoneField.getText().trim(), email = emailField.getText().trim();
        if (name.isEmpty()) { showWarning("Name is required."); nameField.requestFocus(); return false; }
        if (!name.matches("[\\p{L} .'-]{2,100}")) { showWarning("Enter a valid name."); nameField.requestFocus(); return false; }
        if (phone.isEmpty()) { showWarning("Phone number is required."); phoneField.requestFocus(); return false; }
        if (!phone.matches("\\+?[0-9][0-9 -]{6,19}")) { showWarning("Enter a valid phone number."); phoneField.requestFocus(); return false; }
        if (!email.isEmpty() && !email.matches("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) { showWarning("Enter a valid email address."); emailField.requestFocus(); return false; }
        return true;
    }

    private void showSelectedContact(Contact c) {
        idField.setText(String.valueOf(c.getId())); nameField.setText(c.getName()); phoneField.setText(c.getPhone());
        emailField.setText(c.getEmail() == null ? "" : c.getEmail()); addressField.setText(c.getAddress() == null ? "" : c.getAddress());
        setStatus("Contact selected. You can update or delete it.");
    }

    private void clearFields() {
        idField.clear(); nameField.clear(); phoneField.clear(); emailField.clear(); addressField.clear();
        table.getSelectionModel().clearSelection(); nameField.requestFocus(); setStatus("Fields cleared.");
    }

    private void updateCount() { countLabel.setText("Total Contacts: " + contacts.size()); }
    private void setStatus(String message) { statusLabel.setText(message); }

    // ========================= HELPERS =========================
    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #334e68;");
        return label;
    }

    private Separator separator() { return new Separator(); }

    private Button button(String text, String color, double width) {
        Button b = new Button(text);
        b.setPrefWidth(width); b.setPrefHeight(36);
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        return b;
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Telephone Directory");
        alert.setHeaderText("Telephone Directory Management System");
        alert.setContentText("JavaFX + MySQL\n\nFeatures:\n• Login authentication\n• Add, Update and Delete contacts\n• Search by name, phone, email or address\n• Input validation\n• Duplicate phone prevention\n• MySQL database storage");
        alert.showAndWait();
    }

    private boolean confirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle("Validation"); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }

    private void PlatformExit() {
        if (confirm("Exit", "Are you sure you want to close the application?")) System.exit(0);
    }

    public static void main(String[] args) { launch(args); }
}
