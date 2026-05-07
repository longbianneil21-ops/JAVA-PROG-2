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

public class LoginScreen {

    private Stage stage;

    public LoginScreen(Stage stage) {
        this.stage = stage;
    }

    public void show() {
        // ── Logo ─────────────────────────────────────────────────────
        ImageView logo = new ImageView(new Image("file:images/logo.png"));
        logo.setFitWidth(80);
        logo.setFitHeight(80);

        Label schoolName = new Label("Quezon City University");
        schoolName.setFont(Font.font("System", FontWeight.BOLD, 18));
        schoolName.setTextFill(Color.web("#ffffff"));
        schoolName.setTextAlignment(TextAlignment.CENTER);

        Label subtitle = new Label("School Information Assistant");
        subtitle.setFont(Font.font("System", 12));
        subtitle.setTextFill(Color.web("#ffffff"));

        VBox logoBox = new VBox(8, logo, schoolName, subtitle);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setPadding(new Insets(0, 0, 20, 0));

        // ── Form ──────────────────────────────────────────────────────
        Label titleLabel = new Label("Login to your account");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.web("#333333"));

        // Username
        Label userLabel = new Label("Username");
        userLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        userLabel.setTextFill(Color.web("#444444"));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setStyle(fieldStyle());
        usernameField.setPrefHeight(38);

        // Password
        Label passLabel = new Label("Password");
        passLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        passLabel.setTextFill(Color.web("#444444"));

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setStyle(fieldStyle());
        passwordField.setPrefHeight(38);

        // Error label
        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("System", 11));
        errorLabel.setTextFill(Color.web("#C62828"));
        errorLabel.setWrapText(true);

        // Login button
        Button loginBtn = new Button("Login");
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(40);
        loginBtn.setStyle(primaryBtnStyle());
        loginBtn.setFont(Font.font("System", FontWeight.BOLD, 13));

        // Register link
        Label registerLabel = new Label("Don't have an account?");
        registerLabel.setFont(Font.font("System", 12));
        registerLabel.setTextFill(Color.web("#666666"));

        Button registerBtn = new Button("Register here");
        registerBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1565C0;" +
            "-fx-font-size: 12px;" +
            "-fx-cursor: hand;" +
            "-fx-underline: true;"
        );

        HBox registerBox = new HBox(4, registerLabel, registerBtn);
        registerBox.setAlignment(Pos.CENTER);

        // ── Actions ───────────────────────────────────────────────────
        loginBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("⚠ Please fill in all fields.");
                return;
            }

            try {
                int userId = Database.login(username, password);
                if (userId == -1) {
                    errorLabel.setText("⚠ Invalid username or password.");
                } else {
                    // Go to chat screen
                    ChatScreen chat = new ChatScreen(stage, userId, username);
                    chat.show();
                }
            } catch (Exception ex) {
                errorLabel.setText("⚠ Database error: " + ex.getMessage());
            }
        });

        passwordField.setOnAction(e -> loginBtn.fire());

        registerBtn.setOnAction(e -> {
            RegisterScreen register = new RegisterScreen(stage);
            register.show();
        });

        // ── Layout ────────────────────────────────────────────────────
        VBox form = new VBox(10,
            titleLabel,
            userLabel, usernameField,
            passLabel, passwordField,
            errorLabel,
            loginBtn,
            registerBox
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
root.setPadding(new Insets(40));
root.setStyle("-fx-background-color: transparent;");

// Background image
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

        Scene scene = new Scene(stackRoot, 420, 580);
        stage.setScene(scene);
        stage.setTitle("QCU Chatbot - Login");
        stage.show();
    }

    private String fieldStyle() {
        return "-fx-background-color: white;" +
               "-fx-border-color: #536da1;" +
               "-fx-border-radius: 8;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 8 12 8 12;" +
               "-fx-font-size: 13px;";
    }

    private String primaryBtnStyle() {
        return "-fx-background-color: #c01515;" +
               "-fx-text-fill: white;" +
               "-fx-background-radius: 8;" +
               "-fx-cursor: hand;";
    }
}
