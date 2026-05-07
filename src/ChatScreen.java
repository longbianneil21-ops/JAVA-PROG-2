import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

public class ChatScreen {

    private Stage stage;
    private int userId;
    private String username;
    private Chatbot bot;

    private VBox messageContainer;
    private ScrollPane scrollPane;
    private TextField inputField;
    private Button sendButton;
    private HBox typingIndicator;
    private Timeline dotsTimeline;

    private static final String PRIMARY_COLOR = "#c01515";
    private static final String BOT_BUBBLE    = "#F1F3F4";
    private static final String USER_BUBBLE   = "#c01515";
    private static final String BG_COLOR      = "#FAFAFA";

    public ChatScreen(Stage stage, int userId, String username) {
        this.stage    = stage;
        this.userId   = userId;
        this.username = username;
    }

    public void show() {
        try {
            bot = new Chatbot();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
            return;
        }

        // ── Header ───────────────────────────────────────────────────
        ImageView logo = new ImageView(new Image("file:images/logo.png"));
        logo.setFitWidth(38);
        logo.setFitHeight(38);
        Circle clip = new Circle(19, 19, 19);
        logo.setClip(clip);

        Label schoolName = new Label("Quezon City University");
        schoolName.setFont(Font.font("System", FontWeight.BOLD, 15));
        schoolName.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("School Information Assistant");
        subtitleLabel.setFont(Font.font("System", 11));
        subtitleLabel.setTextFill(Color.web("#ffcccc"));

        VBox headerText = new VBox(2, schoolName, subtitleLabel);
        headerText.setAlignment(Pos.CENTER_LEFT);

        Label userLabel = new Label("👤 " + username);
        userLabel.setFont(Font.font("System", 11));
        userLabel.setTextFill(Color.web("#48ff00"));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-font-size: 11px;" +
            "-fx-cursor: hand;"
        );

        Button resetBtn = new Button("↺ Reset");
        resetBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-border-color: white;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-font-size: 11px;" +
            "-fx-cursor: hand;"
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, logo, headerText, spacer, userLabel, resetBtn, logoutBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");

        // ── Messages area ────────────────────────────────────────────
        messageContainer = new VBox(12);
        messageContainer.setPadding(new Insets(16));
        messageContainer.setStyle("-fx-background-color: " + BG_COLOR + ";");

        scrollPane = new ScrollPane(messageContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
            "-fx-background: " + BG_COLOR + ";" +
            "-fx-background-color: " + BG_COLOR + ";" +
            "-fx-border-color: transparent;"
        );
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // ── Quick chips ──────────────────────────────────────────────
        HBox chips = new HBox(8,
            makeChip("School Hours"),
            makeChip("Enrollment"),
            makeChip("Programs"),
            makeChip("Tuition"),
            makeChip("Staff")
        );
        chips.setPadding(new Insets(8, 16, 8, 16));
        chips.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // ── Input row (rounded modern bar) ───────────────────────────
        inputField = new TextField();
        inputField.setPromptText("Ask about QCU...");
        inputField.setFont(Font.font("System", 13));
        inputField.setStyle(
            "-fx-background-color: #f5f5f5;" +
            "-fx-border-color: transparent;" +
            "-fx-border-radius: 24;" +
            "-fx-background-radius: 24;" +
            "-fx-padding: 10 18 10 18;" +
            "-fx-font-size: 13px;"
        );
        inputField.setOnAction(e -> sendMessage());
        HBox.setHgrow(inputField, Priority.ALWAYS);

        sendButton = new Button("➤");
        sendButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        sendButton.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 42px;" +
            "-fx-min-height: 42px;" +
            "-fx-max-width: 42px;" +
            "-fx-max-height: 42px;" +
            "-fx-cursor: hand;"
        );
        sendButton.setOnAction(e -> {
            pulseSendButton();
            sendMessage();
        });

        HBox inputRow = new HBox(10, inputField, sendButton);
        inputRow.setAlignment(Pos.CENTER);
        inputRow.setPadding(new Insets(12, 16, 16, 16));
        inputRow.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-width: 1 0 0 0;"
        );

        // ── Root layout ──────────────────────────────────────────────
        VBox root = new VBox(header, scrollPane, chips, inputRow);
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Scene scene = new Scene(root, 560, 700);
        stage.setScene(scene);
        stage.setTitle("QCU Chatbot - " + username);
        stage.show();

        // ── Button actions ───────────────────────────────────────────
        logoutBtn.setOnAction(e -> {
            bot.resetHistory();
            LoginScreen login = new LoginScreen(stage);
            login.show();
        });

        resetBtn.setOnAction(e -> {
            try {
                Database.clearChatHistory(userId);
            } catch (Exception ex) {
                System.out.println("[DB] Failed to clear history: " + ex.getMessage());
            }
            bot.resetHistory();
            messageContainer.getChildren().clear();
            addBotMessageAnimated("Chat reset! How can I help you? 😊");
        });

        loadChatHistory();
        inputField.requestFocus();
    }

    // ── Load previous chat history from DB ───────────────────────────
    private void loadChatHistory() {
        try {
            ResultSet rs = Database.getChatHistory(userId);
            boolean hasHistory = false;

            while (rs.next()) {
                String role    = rs.getString("role");
                String message = rs.getString("message");
                String time    = rs.getString("timestamp");

                if (role.equals("user")) {
                    addUserMessage(message, time);
                    bot.addToHistory("user", message);
                } else {
                    addBotMessage(message, time);
                    bot.addToHistory("assistant", message);
                }
                hasHistory = true;
            }
            rs.close();

            if (!hasHistory) {
                addBotMessageAnimated("Hello, " + username + "! 👋 Welcome to Quezon City University. How can I help you today?");
            } else {
                addBotMessageAnimated("Welcome back, " + username + "! 😊 How can I help you today?");
            }

        } catch (Exception e) {
            addBotMessageAnimated("Hello, " + username + "! 👋 Welcome to QCU. How can I help you today?");
        }
    }

    // ── Send message ─────────────────────────────────────────────────
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isBlank() || sendButton.isDisabled()) return;

        inputField.clear();
        addUserMessage(text, null);

        try {
            Database.saveMessage(userId, "user", text);
        } catch (Exception e) {
            System.out.println("[DB] Failed to save message: " + e.getMessage());
        }

        setLoading(true);

        CompletableFuture.supplyAsync(() -> {
            try {
                return bot.chat(text);
            } catch (Exception e) {
                return "ERROR: " + e.getMessage();
            }
        }).thenAccept(reply -> Platform.runLater(() -> {
            setLoading(false);
            if (reply.startsWith("ERROR:")) {
                addBotMessageAnimated("⚠️ " + reply.substring(6));
            } else {
                addBotMessageAnimated(reply);
                try {
                    Database.saveMessage(userId, "assistant", reply);
                } catch (Exception e) {
                    System.out.println("[DB] Failed to save reply: " + e.getMessage());
                }
            }
        }));
    }

    // ── Add user bubble with fade-in ──────────────────────────────────
    private void addUserMessage(String text, String time) {
        String timestamp = (time != null) ? time.substring(11, 16) : nowTime();

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(320);
        bubble.setFont(Font.font("System", 13));
        bubble.setTextFill(Color.WHITE);
        bubble.setStyle(
            "-fx-background-color: " + USER_BUBBLE + ";" +
            "-fx-background-radius: 18 18 4 18;" +
            "-fx-padding: 10 14 10 14;"
        );

        Label timeLabel = new Label(timestamp);
        timeLabel.setFont(Font.font("System", 9));
        timeLabel.setTextFill(Color.web("#999999"));

        VBox bubbleBox = new VBox(3, bubble, timeLabel);
        bubbleBox.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(bubbleBox);
        row.setAlignment(Pos.CENTER_RIGHT);
        row.setPadding(new Insets(2, 4, 2, 80));

        fadeIn(row);
        messageContainer.getChildren().add(row);
        scrollToBottom();
    }

    // ── Add bot bubble (no animation — for history load) ─────────────
    private void addBotMessage(String text, String time) {
        String timestamp = (time != null) ? time.substring(11, 16) : nowTime();

        ImageView icon = makeBotIcon();

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(320);
        bubble.setFont(Font.font("System", 13));
        bubble.setTextFill(Color.web("#202124"));
        bubble.setStyle(
            "-fx-background-color: " + BOT_BUBBLE + ";" +
            "-fx-background-radius: 18 18 18 4;" +
            "-fx-padding: 10 14 10 14;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 18 18 18 4;" +
            "-fx-border-width: 1;"
        );

        Label timeLabel = new Label(timestamp);
        timeLabel.setFont(Font.font("System", 9));
        timeLabel.setTextFill(Color.web("#999999"));

        VBox bubbleBox = new VBox(3, bubble, timeLabel);

        HBox row = new HBox(8, icon, bubbleBox);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(2, 80, 2, 4));

        messageContainer.getChildren().add(row);
        scrollToBottom();
    }

    // ── Add bot bubble with typing animation ─────────────────────────
    private void addBotMessageAnimated(String fullText) {
        String timestamp = nowTime();

        ImageView icon = makeBotIcon();

        Label bubble = new Label("");
        bubble.setWrapText(true);
        bubble.setMaxWidth(320);
        bubble.setFont(Font.font("System", 13));
        bubble.setTextFill(Color.web("#202124"));
        bubble.setStyle(
            "-fx-background-color: " + BOT_BUBBLE + ";" +
            "-fx-background-radius: 18 18 18 4;" +
            "-fx-padding: 10 14 10 14;" +
            "-fx-border-color: #E0E0E0;" +
            "-fx-border-radius: 18 18 18 4;" +
            "-fx-border-width: 1;"
        );

        Label timeLabel = new Label(timestamp);
        timeLabel.setFont(Font.font("System", 9));
        timeLabel.setTextFill(Color.web("#999999"));
        timeLabel.setOpacity(0);

        VBox bubbleBox = new VBox(3, bubble, timeLabel);

        HBox row = new HBox(8, icon, bubbleBox);
        row.setAlignment(Pos.TOP_LEFT);
        row.setPadding(new Insets(2, 80, 2, 4));

        fadeIn(row);
        messageContainer.getChildren().add(row);
        scrollToBottom();

        // Typing animation — letter by letter
        final int[] index = {0};
        int delay = Math.max(12, 800 / Math.max(fullText.length(), 1)); // faster for long text

        Timeline typingTimeline = new Timeline();
        typingTimeline.setCycleCount(fullText.length());

        KeyFrame kf = new KeyFrame(Duration.millis(delay), e -> {
            index[0]++;
            bubble.setText(fullText.substring(0, index[0]));
            scrollToBottom();
        });
        typingTimeline.getKeyFrames().add(kf);
        typingTimeline.setOnFinished(e -> {
            // Fade in timestamp after typing finishes
            FadeTransition ft = new FadeTransition(Duration.millis(400), timeLabel);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
        typingTimeline.play();
    }

    // ── Animated ● ● ● typing indicator ──────────────────────────────
    private void setLoading(boolean loading) {
        sendButton.setDisable(loading);
        inputField.setDisable(loading);

        if (loading) {
            ImageView icon = makeBotIcon();

            Label dot1 = makeDot();
            Label dot2 = makeDot();
            Label dot3 = makeDot();

            HBox dotsBox = new HBox(5, dot1, dot2, dot3);
            dotsBox.setAlignment(Pos.CENTER_LEFT);
            dotsBox.setStyle(
                "-fx-background-color: " + BOT_BUBBLE + ";" +
                "-fx-background-radius: 18;" +
                "-fx-padding: 12 16 12 16;" +
                "-fx-border-color: #E0E0E0;" +
                "-fx-border-radius: 18;" +
                "-fx-border-width: 1;"
            );

            typingIndicator = new HBox(8, icon, dotsBox);
            typingIndicator.setAlignment(Pos.CENTER_LEFT);
            typingIndicator.setPadding(new Insets(2, 80, 2, 4));

            fadeIn(typingIndicator);
            messageContainer.getChildren().add(typingIndicator);
            scrollToBottom();

            // Animate each dot with staggered bounce
            dotsTimeline = new Timeline();
            animateDot(dot1, 0);
            animateDot(dot2, 200);
            animateDot(dot3, 400);
            dotsTimeline.setCycleCount(Animation.INDEFINITE);
            dotsTimeline.play();

        } else {
            if (dotsTimeline != null) {
                dotsTimeline.stop();
                dotsTimeline = null;
            }
            if (typingIndicator != null) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), typingIndicator);
                ft.setFromValue(1);
                ft.setToValue(0);
                ft.setOnFinished(e -> messageContainer.getChildren().remove(typingIndicator));
                ft.play();
                typingIndicator = null;
            }
        }
    }

    private Label makeDot() {
        Label dot = new Label("●");
        dot.setFont(Font.font("System", 10));
        dot.setTextFill(Color.web("#c01515"));
        return dot;
    }

    private void animateDot(Label dot, int delayMs) {
        TranslateTransition up = new TranslateTransition(Duration.millis(300), dot);
        up.setByY(-6);
        up.setAutoReverse(true);
        up.setCycleCount(Animation.INDEFINITE);
        up.setDelay(Duration.millis(delayMs));
        up.play();
    }

    // ── Send button pulse animation ───────────────────────────────────
    private void pulseSendButton() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), sendButton);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.25);
        pulse.setToY(1.25);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    // ── Fade-in helper ────────────────────────────────────────────────
    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    // ── Quick chip button ─────────────────────────────────────────────
    private Button makeChip(String label) {
        Button chip = new Button(label);
        chip.setFont(Font.font("System", 11));
        chip.setStyle(
            "-fx-background-color: white;" +
            "-fx-text-fill: #c01515;" +
            "-fx-border-color: #ffcccc;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-padding: 4 12 4 12;" +
            "-fx-cursor: hand;"
        );
        chip.setOnAction(e -> {
            inputField.setText(label);
            sendMessage();
        });
        return chip;
    }

    // ── Bot icon helper ───────────────────────────────────────────────
    private ImageView makeBotIcon() {
        ImageView icon = new ImageView(new Image("file:images/logo.png"));
        icon.setFitWidth(28);
        icon.setFitHeight(28);
        Circle c = new Circle(14, 14, 14);
        icon.setClip(c);
        return icon;
    }

    // ── Current time string ───────────────────────────────────────────
    private String nowTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    // ── Scroll to bottom ──────────────────────────────────────────────
    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ── Alert ─────────────────────────────────────────────────────────
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}