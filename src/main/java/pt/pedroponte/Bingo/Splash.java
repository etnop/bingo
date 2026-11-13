package pt.pedroponte.Bingo;

import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/** Janela de arranque mostrada enquanto o Spring carrega (JavaFXApplication.init). */
public class Splash extends Preloader {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/fxml/ellipse21.png")));
        logo.setFitWidth(260);
        logo.setPreserveRatio(true);
        ProgressIndicator roda = new ProgressIndicator();
        roda.setPrefSize(36, 36);
        Label texto = new Label("A carregar…");
        texto.setTextFill(Color.web("#9aa0a6"));

        VBox root = new VBox(18, logo, roda, texto);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #1e1f22; -fx-background-radius: 16; -fx-padding: 32;"
                + "-fx-border-color: #3f4248; -fx-border-radius: 16;");
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setScene(scene);
        stage.setTitle("Bingo");
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        // fecha logo antes de a janela principal aparecer
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}
