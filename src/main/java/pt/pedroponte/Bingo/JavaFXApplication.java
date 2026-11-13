package pt.pedroponte.Bingo;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    public static ConfigurableApplicationContext springContext;
    public static     List<List<List<String>>> cartoes;

    @Override
    public void init() throws Exception {
        springContext = new SpringApplicationBuilder(BingoApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        
        primaryStage.setTitle("JavaFX com Spring Boot");
        primaryStage.setScene(new Scene(root));
        // primaryStage.setMinWidth(710);
        // primaryStage.setMinHeight(484);
        // root.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());

        setTheme(root, "default");

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        springContext.close();
        Platform.exit();
    }

    private void setTheme(Parent root, String themeName) {
        root.getStylesheets().clear();
        root.getStylesheets().add(getClass().getResource("/fxml/" + themeName + ".css").toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
