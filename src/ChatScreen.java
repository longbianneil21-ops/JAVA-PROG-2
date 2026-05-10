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
import java.time.LocalDate;
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
    private Label statusLabel;
    private VBox welcomeState;
    private VBox sidebarList;

    private int currentSessionId = -1;
    private Button activeSessionBtn = null;

    private static final String PRIMARY_COLOR = "#c01515";
    private static final String BOT_BUBBLE   = "#F1F3F4";
    private static final String USER_BUBBLE  = "#c01515";
    private static final String BG_COLOR     = "#FFFFFF";
    private static final String SIDEBAR_COLOR = "#FAFAFA";
    private static final String BORDER_COLOR  = "#E8E8E8";

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
        logo.setFitWidth(38); logo.setFitHeight(38);
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

        Circle statusDot = new Circle(5);
        statusDot.setFill(Color.web("#4CAF50"));
        statusLabel = new Label("Ready");
        statusLabel.setFont(Font.font("System", 11));
        statusLabel.setTextFill(Color.WHITE);
        HBox statusBox = new HBox(5, statusDot, statusLabel);
        statusBox.setAlignment(Pos.CENTER);

        Label userLabel = new Label("● " + username);
        userLabel.setFont(Font.font("System", 11));
        userLabel.setTextFill(Color.web("#48ff00"));

        Button resetBtn = new Button("↺ Reset");
        resetBtn.setStyle(headerBtnStyle());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(headerBtnStyle());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(10, logo, headerText, spacer, statusBox, userLabel, resetBtn, logoutBtn);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 16, 12, 16));
        header.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");

        // ── Sidebar ──────────────────────────────────────────────────
        Button newChatBtn = new Button("＋  New Chat");
        newChatBtn.setMaxWidth(Double.MAX_VALUE);
        newChatBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        newChatBtn.setStyle(
            "-fx-background-color: " + PRIMARY_COLOR + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 8 12 8 12;" +
            "-fx-cursor: hand;"
        );
        newChatBtn.setOnAction(e -> startNewChat());
        VBox.setMargin(newChatBtn, new Insets(12, 10, 8, 10));

        Label sidebarTitle = new Label("Chat History");
        sidebarTitle.setFont(Font.font("System", FontWeight.BOLD, 11));
        sidebarTitle.setTextFill(Color.web("#999999"));
        sidebarTitle.setPadding(new Insets(0, 16, 6, 16));

        sidebarList = new VBox(2);
        sidebarList.setPadding(new Insets(0, 8, 8, 8));

        ScrollPane sidebarScroll = new ScrollPane(sidebarList);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sidebarScroll.setStyle(
            "-fx-background: " + SIDEBAR_COLOR + ";" +
            "-fx-background-color: " + SIDEBAR_COLOR + ";" +
            "-fx-border-color: transparent;"
        );
        VBox.setVgrow(sidebarScroll, Priority.ALWAYS);

        VBox sidebar = new VBox(newChatBtn, sidebarTitle, sidebarScroll);
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setMaxWidth(200);
        sidebar.setStyle(
            "-fx-background-color: " + SIDEBAR_COLOR + ";" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 0 1 0 0;"
        );

        // ── Welcome empty state ──────────────────────────────────────
        ImageView welcomeLogo = new ImageView(new Image("file:images/logo.png"));
        welcomeLogo.setFitWidth(80); welcomeLogo.setFitHeight(80);

        Label welcomeTitle = new Label("Welcome to QCU Assistant! 👋");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        welcomeTitle.setTextFill(Color.web("#222222"));

        Label welcomeSub = new Label("How can I help you today?");
        welcomeSub.setFont(Font.font("System", 13));
        welcomeSub.setTextFill(Color.web("#777777"));

        HBox cards1 = new HBox(12,
            makeSuggestionCard("📚", "What programs does QCU offer?"),
            makeSuggestionCard("💰", "How much is the tuition fee?")
        );
        cards1.setAlignment(Pos.CENTER);

        HBox cards2 = new HBox(12,
            makeSuggestionCard("🕐", "What are the school hours?"),
            makeSuggestionCard("📋", "How do I enroll at QCU?")
        );
        cards2.setAlignment(Pos.CENTER);

        welcomeState = new VBox(12, welcomeLogo, welcomeTitle, welcomeSub, cards1, cards2);
        welcomeState.setAlignment(Pos.CENTER);
        welcomeState.setPadding(new Insets(40));
        welcomeState.setStyle("-fx-background-color: " + BG_COLOR + ";");

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

        StackPane chatArea = new StackPane(scrollPane, welcomeState);
        VBox.setVgrow(chatArea, Priority.ALWAYS);

        // ── Quick chips ──────────────────────────────────────────────
        HBox chips = new HBox(8,
            makeChip("School Hours"),
            makeChip("Enrollment"),
            makeChip("Programs"),
            makeChip("Tuition"),
            makeChip("Staff")
        );
        chips.setPadding(new Insets(8, 16, 4, 16));
        chips.setStyle("-fx-background-color: " + BG_COLOR + ";");

        // ── Input row ────────────────────────────────────────────────
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

        Label charCounter = new Label("0/500");
        charCounter.setFont(Font.font("System", 10));
        charCounter.setTextFill(Color.web("#999999"));
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            int len = newVal.length();
            charCounter.setText(len + "/500");
            charCounter.setTextFill(len > 450 ? Color.web("#c01515") : Color.web("#999999"));
            if (len > 500) inputField.setText(oldVal);
        });

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
        sendButton.setOnAction(e -> { pulseSendButton(); sendMessage(); });

        HBox inputControls = new HBox(10, inputField, sendButton);
        inputControls.setAlignment(Pos.CENTER);

        VBox inputRow = new VBox(4, inputControls, charCounter);
        inputRow.setPadding(new Insets(10, 16, 14, 16));
        inputRow.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-width: 1 0 0 0;"
        );

        // ── Main chat column ─────────────────────────────────────────
        VBox chatColumn = new VBox(chatArea, chips, inputRow);
        chatColumn.setStyle("-fx-background-color: " + BG_COLOR + ";");
        HBox.setHgrow(chatColumn, Priority.ALWAYS);

        HBox body = new HBox(sidebar, chatColumn);
        VBox.setVgrow(body, Priority.ALWAYS);

        VBox root = new VBox(header, body);
        root.setStyle("-fx-background-color: " + BG_COLOR + ";");

        Scene scene = new Scene(root, 900, 680);
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
                Database.clearAllSessions(userId);
            } catch (Exception ex) {
                System.out.println("[DB] Failed to clear sessions: " + ex.getMessage());
            }
            currentSessionId = -1;
            activeSessionBtn = null;
            bot.resetHistory();
            messageContainer.getChildren().clear();
            sidebarList.getChildren().clear();
            showEmptySidebar();
            showWelcomeState(true);
        });

        loadSessions();
        inputField.requestFocus();
    }

    // ── Start a brand new chat session ───────────────────────────────
    private void startNewChat() {
        try {
            currentSessionId = Database.createSession(userId, "New Chat");
        } catch (Exception e) {
            System.out.println("[DB] Failed to create session: " + e.getMessage());
            return;
        }

        bot.resetHistory();
        messageContainer.getChildren().clear();
        showWelcomeState(true);
        loadSessions();
        inputField.requestFocus();
    }

    // ── Load a session into the chat area ────────────────────────────
    private void loadSession(int sessionId, Button btn) {
        // Deactivate previous button
        if (activeSessionBtn != null) {
            activeSessionBtn.setStyle(sessionBtnStyle(false));
        }
        activeSessionBtn = btn;
        btn.setStyle(sessionBtnStyle(true));

        currentSessionId = sessionId;
        bot.resetHistory();
        messageContainer.getChildren().clear();
        showWelcomeState(false);

        try {
            ResultSet rs = Database.getSessionMessages(sessionId);
            boolean hasMessages = false;

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
                hasMessages = true;
            }
            rs.close();

            if (!hasMessages) {
                showWelcomeState(true);
            }

        } catch (Exception e) {
            System.out.println("[DB] Failed to load session: " + e.getMessage());
        }

        inputField.requestFocus();
    }

    // ── Load sidebar sessions list ────────────────────────────────────
    private void loadSessions() {
        sidebarList.getChildren().clear();
        try {
            ResultSet rs = Database.getSessions(userId);
            boolean hasSessions = false;
            String lastDate = "";

            while (rs.next()) {
                int    sessionId = rs.getInt("id");
                String title     = rs.getString("title");
                String createdAt = rs.getString("created_at");
                String date      = createdAt.substring(0, 10);

                // Date separator
                if (!date.equals(lastDate)) {
                    lastDate = date;
                    Label dl = new Label(formatSidebarDate(date));
                    dl.setFont(Font.font("System", FontWeight.BOLD, 10));
                    dl.setTextFill(Color.web("#aaaaaa"));
                    dl.setPadding(new Insets(8, 8, 2, 8));
                    sidebarList.getChildren().add(dl);
                }

                // Session button with delete X
                boolean isActive = (sessionId == currentSessionId);

                Button sessionBtn = new Button(title);
                sessionBtn.setMaxWidth(Double.MAX_VALUE);
                sessionBtn.setAlignment(Pos.CENTER_LEFT);
                sessionBtn.setFont(Font.font("System", 12));
                sessionBtn.setStyle(sessionBtnStyle(isActive));

                if (isActive) activeSessionBtn = sessionBtn;

                final int sid = sessionId;
                sessionBtn.setOnAction(e -> loadSession(sid, sessionBtn));
                sessionBtn.setOnMouseEntered(e -> {
                    if (sessionBtn != activeSessionBtn)
                        sessionBtn.setStyle(sessionBtnHoverStyle());
                });
                sessionBtn.setOnMouseExited(e -> {
                    if (sessionBtn != activeSessionBtn)
                        sessionBtn.setStyle(sessionBtnStyle(false));
                });

                // Delete button
                Button deleteBtn = new Button("✕");
                deleteBtn.setFont(Font.font("System", 10));
                deleteBtn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #aaaaaa;" +
                    "-fx-padding: 2 4 2 4;" +
                    "-fx-cursor: hand;"
                );
                deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #c01515;" +
                    "-fx-padding: 2 4 2 4;" +
                    "-fx-cursor: hand;"
                ));
                deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #aaaaaa;" +
                    "-fx-padding: 2 4 2 4;" +
                    "-fx-cursor: hand;"
                ));
                deleteBtn.setOnAction(e -> {
                    try {
                        Database.deleteSession(sid);
                        if (currentSessionId == sid) {
                            currentSessionId = -1;
                            activeSessionBtn = null;
                            bot.resetHistory();
                            messageContainer.getChildren().clear();
                            showWelcomeState(true);
                        }
                        loadSessions();
                    } catch (Exception ex) {
                        System.out.println("[DB] Failed to delete session: " + ex.getMessage());
                    }
                });

                HBox row = new HBox(sessionBtn, deleteBtn);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(sessionBtn, Priority.ALWAYS);

                sidebarList.getChildren().add(row);
                hasSessions = true;
            }
            rs.close();

            if (!hasSessions) showEmptySidebar();

        } catch (Exception e) {
            System.out.println("[Sidebar] Failed to load sessions: " + e.getMessage());
        }
    }

    // ── Empty sidebar state ───────────────────────────────────────────
    private void showEmptySidebar() {
        Label empty = new Label("No chats yet.\nClick '＋ New Chat'\nto get started!");
        empty.setFont(Font.font("System", 11));
        empty.setTextFill(Color.web("#bbbbbb"));
        empty.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        empty.setPadding(new Insets(20, 10, 10, 10));
        sidebarList.getChildren().add(empty);
    }

    // ── Send message ──────────────────────────────────────────────────
    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isBlank() || sendButton.isDisabled()) return;

        // Auto-create session if none active
        if (currentSessionId == -1) {
            try {
                String title = text.length() > 40 ? text.substring(0, 40) + "..." : text;
                currentSessionId = Database.createSession(userId, title);
                loadSessions();
            } catch (Exception e) {
                System.out.println("[DB] Failed to create session: " + e.getMessage());
                return;
            }
        }

        showWelcomeState(false);
        inputField.clear();
        addUserMessage(text, null);

        // Update session title to first message if still "New Chat"
        final int sid = currentSessionId;
        try {
            String title = text.length() > 40 ? text.substring(0, 40) + "..." : text;
            Database.updateSessionTitle(sid, title);
            Database.saveMessage(userId, sid, "user", text);
        } catch (Exception e) {
            System.out.println("[DB] Failed to save message: " + e.getMessage());
        }

        setLoading(true);
        setStatus(true);

        CompletableFuture.supplyAsync(() -> {
            try { return bot.chat(text); }
            catch (Exception e) { return "ERROR: " + e.getMessage(); }
        }).thenAccept(reply -> Platform.runLater(() -> {
            setLoading(false);
            setStatus(false);
            if (reply.startsWith("ERROR:")) {
                addBotMessageAnimated("⚠️ " + reply.substring(6));
            } else {
                addBotMessageAnimated(reply);
                try { Database.saveMessage(userId, sid, "assistant", reply); }
                catch (Exception e) { System.out.println("[DB] Failed to save reply: " + e.getMessage()); }
            }
            loadSessions();
        }));
    }

    // ── Welcome state ─────────────────────────────────────────────────
    private void showWelcomeState(boolean show) {
        welcomeState.setVisible(show);
        welcomeState.setManaged(show);
    }

    // ── Status indicator ──────────────────────────────────────────────
    private void setStatus(boolean thinking) {
        Platform.runLater(() -> {
            statusLabel.setText(thinking ? "Thinking..." : "Ready");
            statusLabel.setTextFill(thinking ? Color.web("#FFD54F") : Color.WHITE);
        });
    }

    // ── User bubble ───────────────────────────────────────────────────
    private void addUserMessage(String text, String time) {
        String timestamp = (time != null) ? time.substring(11, 16) : nowTime();

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(360);
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

    // ── Bot bubble (no animation) ─────────────────────────────────────
    private void addBotMessage(String text, String time) {
        String timestamp = (time != null) ? time.substring(11, 16) : nowTime();
        ImageView icon = makeBotIcon();

        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(360);
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

    // ── Bot bubble with typing animation ─────────────────────────────
    private void addBotMessageAnimated(String fullText) {
        String timestamp = nowTime();
        ImageView icon = makeBotIcon();

        Label bubble = new Label("");
        bubble.setWrapText(true);
        bubble.setMaxWidth(360);
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

        final int[] index = {0};
        int delay = Math.max(12, 800 / Math.max(fullText.length(), 1));
        Timeline tl = new Timeline();
        tl.setCycleCount(fullText.length());
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(delay), e -> {
            index[0]++;
            bubble.setText(fullText.substring(0, index[0]));
            scrollToBottom();
        }));
        tl.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(400), timeLabel);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        });
        tl.play();
    }

    // ── Animated dots ─────────────────────────────────────────────────
    private void setLoading(boolean loading) {
        sendButton.setDisable(loading);
        inputField.setDisable(loading);

        if (loading) {
            ImageView icon = makeBotIcon();
            Label dot1 = makeDot(); Label dot2 = makeDot(); Label dot3 = makeDot();

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

            dotsTimeline = new Timeline();
            animateDot(dot1, 0); animateDot(dot2, 200); animateDot(dot3, 400);
            dotsTimeline.setCycleCount(Animation.INDEFINITE);
            dotsTimeline.play();

        } else {
            if (dotsTimeline != null) { dotsTimeline.stop(); dotsTimeline = null; }
            if (typingIndicator != null) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), typingIndicator);
                ft.setFromValue(1); ft.setToValue(0);
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
        up.setByY(-6); up.setAutoReverse(true);
        up.setCycleCount(Animation.INDEFINITE);
        up.setDelay(Duration.millis(delayMs));
        up.play();
    }

    // ── Suggestion card ───────────────────────────────────────────────
    private VBox makeSuggestionCard(String emoji, String text) {
        Label emojiLabel = new Label(emoji);
        emojiLabel.setFont(Font.font("System", 22));

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("System", 12));
        textLabel.setTextFill(Color.web("#444444"));
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(160);

        VBox card = new VBox(6, emojiLabel, textLabel);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(170); card.setPrefHeight(90);
        card.setPadding(new Insets(14));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        );
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: #fff5f5;" +
            "-fx-border-color: #ffcccc;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: " + BORDER_COLOR + ";" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);"
        ));
        card.setOnMouseClicked(e -> { inputField.setText(text); sendMessage(); });
        return card;
    }

    // ── Send button pulse ─────────────────────────────────────────────
    private void pulseSendButton() {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(120), sendButton);
        pulse.setFromX(1.0); pulse.setFromY(1.0);
        pulse.setToX(1.25); pulse.setToY(1.25);
        pulse.setAutoReverse(true); pulse.setCycleCount(2);
        pulse.play();
    }

    private void fadeIn(javafx.scene.Node node) {
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

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
        chip.setOnAction(e -> { inputField.setText(label); sendMessage(); });
        return chip;
    }

    private ImageView makeBotIcon() {
        ImageView icon = new ImageView(new Image("file:images/logo.png"));
        icon.setFitWidth(28); icon.setFitHeight(28);
        Circle c = new Circle(14, 14, 14); icon.setClip(c);
        return icon;
    }

    private String formatSidebarDate(String dateStr) {
        try {
            LocalDate date      = LocalDate.parse(dateStr);
            LocalDate today     = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            if (date.equals(today)) return "Today";
            if (date.equals(yesterday)) return "Yesterday";
            return date.format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (Exception e) { return dateStr; }
    }

    private String nowTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    // ── Style helpers ─────────────────────────────────────────────────
    private String sessionBtnStyle(boolean active) {
        return active
            ? "-fx-background-color: #fde8e8;" +
              "-fx-text-fill: #c01515;" +
              "-fx-background-radius: 8;" +
              "-fx-padding: 7 10 7 10;" +
              "-fx-cursor: hand;" +
              "-fx-alignment: center-left;" +
              "-fx-text-overrun: ellipsis;"
            : "-fx-background-color: transparent;" +
              "-fx-text-fill: #333333;" +
              "-fx-background-radius: 8;" +
              "-fx-padding: 7 10 7 10;" +
              "-fx-cursor: hand;" +
              "-fx-alignment: center-left;" +
              "-fx-text-overrun: ellipsis;";
    }

    private String sessionBtnHoverStyle() {
        return "-fx-background-color: #f0f0f0;" +
               "-fx-text-fill: #111111;" +
               "-fx-background-radius: 8;" +
               "-fx-padding: 7 10 7 10;" +
               "-fx-cursor: hand;" +
               "-fx-alignment: center-left;" +
               "-fx-text-overrun: ellipsis;";
    }

    private String headerBtnStyle() {
        return "-fx-background-color: transparent;" +
               "-fx-text-fill: white;" +
               "-fx-border-color: rgba(255,255,255,0.6);" +
               "-fx-border-radius: 12;" +
               "-fx-background-radius: 12;" +
               "-fx-font-size: 11px;" +
               "-fx-cursor: hand;";
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title); alert.setHeaderText(null);
        alert.setContentText(msg); alert.showAndWait();
    }
}