import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.image.Image;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Initialize database tables
        stage.getIcons().add(new Image("file:images/logo.png"));
        try {
            Database.initialize();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Database Error");
            alert.setHeaderText("Cannot connect to database!");
            alert.setContentText(
                "Make sure XAMPP is running and MySQL is started.\n\n" +
                "Error: " + e.getMessage()
            );
            alert.showAndWait();
            return;
        }

        // Show login screen
        LoginScreen login = new LoginScreen(stage);
        login.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
