package pt.pedroponte.Bingo;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BingoApplication {
    public static void main(String[] args) {
        // ponto de entrada do .jar: arranca o JavaFX, que por sua vez arranca o Spring (JavaFXApplication.init)
        System.setProperty("javafx.preloader", Splash.class.getName());
        javafx.application.Application.launch(JavaFXApplication.class, args);
    }
}
