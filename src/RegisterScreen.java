import javafx.scene.layout.StackPane;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class RegisterScreen {

    private Stage stage;

    public RegisterScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // ── Logo ─────────────────────────────────────────────────────
        ImageView logo = new ImageView(new Image("file:images/logo.png"));
        logo.setFitWidth(70);
        logo.setFitHeight(70);

        Label schoolName = new Label("Quezon City University");
        schoolName.setFont(Font.font("System", FontWeight.BOLD, 17));
        schoolName.setTextFill(Color.web("#ffffff"));
        schoolName.setTextAlignment(TextAlignment.CENTER);

        VBox logoBox = new VBox(6, logo, schoolName);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 16, 0));

        // ── Form ──────────────────────────────────────────────────────
        Label titleLabel = new Label("Create an account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#333333"));

        // Username
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        userLabel.setTextFill(Color.web("#444444"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.setStyle(fieldStyle());
        usernameField.setPrefHeight(38);

        // Student Number
        Label snLabel = new Label("Student Number");
        snLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        snLabel.setTextFill(Color.web("#444444"));

        TextField studentNumberField = new TextField();
        studentNumberField.setPromptText("e.g. A25-10405");
        studentNumberField.setStyle(fieldStyle());
        studentNumberField.setPrefHeight(38);

        // Password
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        passLabel.setTextFill(Color.web("#444444"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Create a password");
        passwordField.setStyle(fieldStyle());
        passwordField.setPrefHeight(38);

        // Confirm Password
        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        confirmLabel.setTextFill(Color.web("#444444"));

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Repeat your password");
        confirmField.setStyle(fieldStyle());
        confirmField.setPrefHeight(38);

        // Error / success label
        Label messageLabel = new Label("");
        messageLabel.setFont(Font.font("System", 11));
        messageLabel.setTextFill(Color.web("#C62828"));
        messageLabel.setWrapText(true);

        // Register button
        Button registerBtn = new Button("Register");
        registerBtn.setPrefWidth(Double.MAX_VALUE);
        registerBtn.setPrefHeight(40);
        registerBtn.setStyle(primaryBtnStyle());
        registerBtn.setFont(Font.font("System", FontWeight.BOLD, 13));

        // Back to login
        Label loginLabel = new Label("Already have an account?");
        loginLabel.setFont(Font.font("System", 12));
        loginLabel.setTextFill(Color.web("#666666"));

        Button loginBtn = new Button("Login here");
        loginBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1565C0;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-underline: true;"
        );

        HBox loginBox = new HBox(4, loginLabel, loginBtn);
        loginBox.setAlignment(Pos.CENTER);

        // ── Actions ───────────────────────────────────────────────────
        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String studentNumber = studentNumberField.getText().trim();
            String password = passwordField.getText().trim();
            String confirm = confirmField.getText().trim();

            // Validation
            if (username.isEmpty() || studentNumber.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                messageLabel.setTextFill(Color.web("#C62828"));
                messageLabel.setText("⚠ Please fill in all fields.");
                return;
            }

            if (!password.equals(confirm)) {
                messageLabel.setTextFill(Color.web("#C62828"));
                messageLabel.setText("⚠ Passwords do not match.");
                return;
            }

            if (password.length() < 6) {
                messageLabel.setTextFill(Color.web("#C62828"));
                messageLabel.setText("⚠ Password must be at least 6 characters.");
                return;
            }

            try {
                if (Database.usernameExists(username)) {
                    messageLabel.setTextFill(Color.web("#C62828"));
                    messageLabel.setText("⚠ Username already exists. Choose another.");
                    return;
                }

                if (Database.studentNumberExists(studentNumber)) {
                    messageLabel.setTextFill(Color.web("#C62828"));
                    messageLabel.setText("⚠ Student number already registered.");
                    return;
                }

                boolean success = Database.register(username, studentNumber, password);
                if (success) {
                    messageLabel.setTextFill(Color.web("#2E7D32"));
                    messageLabel.setText("✅ Account created! You can now login.");
                    usernameField.clear();
                    studentNumberField.clear();
                    passwordField.clear();
                    confirmField.clear();
                }
            } catch (Exception ex) {
                messageLabel.setTextFill(Color.web("#C62828"));
                messageLabel.setText("⚠ Error: " + ex.getMessage());
            }
        });

        loginBtn.setOnAction(e -> {
            LoginScreen login = new LoginScreen(stage);
            login.show();
        });

        // ── Layout ────────────────────────────────────────────────────
        VBox form = new VBox(10,
            titleLabel,
            userLabel, usernameField,
            snLabel, studentNumberField,
            passLabel, passwordField,
            confirmLabel, confirmField,
            messageLabel,
            registerBtn,
            loginBox
        );
        form.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-padding: 24;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);"
        );
        form.setMaxWidth(340);

       VBox root = new VBox(20, logoBox, form);
root.setAlignment(Pos.CENTER);
root.setPadding(new Insets(30));
root.setStyle("-fx-background-color: transparent;");

ImageView bg = new ImageView(new Image("file:images/background.png"));
bg.setPreserveRatio(false);
bg.fitWidthProperty().bind(stage.widthProperty());
bg.fitHeightProperty().bind(stage.heightProperty());

// Dark overlay so text is readable
javafx.scene.shape.Rectangle overlay = new javafx.scene.shape.Rectangle();
overlay.widthProperty().bind(stage.widthProperty());
overlay.heightProperty().bind(stage.heightProperty());
overlay.setFill(Color.rgb(0, 0, 0, 0.45));

StackPane stackRoot = new StackPane(bg, overlay, root);

Scene scene = new Scene(stackRoot, 420, 640);
        stage.setScene(scene);
        stage.setTitle("QCU Chatbot - Register");
        stage.show();
    }

    private String fieldStyle() {
        return "-fx-background-color: white;" +
               "-fx-border-color: #DADCE0;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 8 12 8 12;" +
               "-fx-font-size: 13px;";
    }

    private String primaryBtnStyle() {
        return "-fx-background-color: #035307;" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;";
    }
}
