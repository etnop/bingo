package pt.pedroponte.Bingo;


import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.Node;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFXApplication extends Application {

    static {
        // splash enquanto o Spring arranca; aqui para valer no javafx:run e no executável
        System.setProperty("javafx.preloader", Splash.class.getName());
    }

    public static ConfigurableApplicationContext springContext;
    public static List<List<List<String>>> cartoes = new ArrayList<>();
    // ponytail: uma sessão = um ficheiro de texto (um cartão por linha); trocar por JSON/BD se precisar de mais dados
    public static final Path PASTA_SESSOES = Path.of(System.getProperty("user.home"), ".bingo", "sessoes");
    public static Path sessaoAtual;

    /** Cria uma sessão nova (ficheiro com data/hora) com os cartões atuais. */
    public static void novaSessao() {
        String nome = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        sessaoAtual = PASTA_SESSOES.resolve(nome + ".txt");
        guardarCartoes();
    }

    public static List<Path> listarSessoes() {
        if (!Files.isDirectory(PASTA_SESSOES)) return List.of();
        try (var s = Files.list(PASTA_SESSOES)) {
            return s.filter(p -> p.toString().endsWith(".txt"))
                    .sorted(java.util.Comparator.comparing((Path p) -> p.toFile().lastModified()).reversed())
                    .toList();
        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static void guardarCartoes() {
        List<String> linhas = cartoes.stream()
                .map(c -> c.stream().map(l -> String.join(",", l)).collect(Collectors.joining(";")))
                .toList();
        try {
            Files.createDirectories(PASTA_SESSOES);
            Files.write(sessaoAtual, linhas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void carregarCartoes(Path sessao) {
        sessaoAtual = sessao;
        cartoes = new ArrayList<>();
        try {
            for (String linha : Files.readAllLines(sessao)) {
                if (linha.isBlank()) continue;
                List<List<String>> grade = new ArrayList<>();
                for (String l : linha.split(";")) grade.add(new ArrayList<>(Arrays.asList(l.split(","))));
                cartoes.add(grade);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Mostra um ecrã FXML na janela de {@code atual}. */
    public static void abrir(String fxml, Node atual) {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFXApplication.class.getResource("/fxml/" + fxml));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();
            // mesma janela, só troca o conteúdo: sem piscar nem saltar de posição
            Stage stage = (Stage) atual.getScene().getWindow();
            double cx = stage.getX() + stage.getWidth() / 2, cy = stage.getY() + stage.getHeight() / 2;
            stage.setScene(new Scene(root));
            if (!stage.isMaximized()) {
                stage.sizeToScene();
                stage.setX(cx - stage.getWidth() / 2); // mantém o centro onde estava
                stage.setY(Math.max(0, cy - stage.getHeight() / 2));
            }
            root.setOpacity(0);
            javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(180), root);
            fade.setToValue(1);
            fade.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws Exception {
        springContext = new SpringApplicationBuilder(BingoApplication.class).run();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        
        primaryStage.setTitle("Bingo");
        primaryStage.setScene(new Scene(root));
        // primaryStage.setMinWidth(710);
        // primaryStage.setMinHeight(484);
        // root.getStylesheets().add(getClass().getResource("/fxml/style.css").toExternalForm());

        setTheme(root, "default");

        // aplica o tema a todas as janelas, incluindo diálogos (Alert, TextInputDialog)
        String css = getClass().getResource("/fxml/default.css").toExternalForm();
        javafx.stage.Window.getWindows().addListener((javafx.collections.ListChangeListener<javafx.stage.Window>) c -> {
            while (c.next()) {
                for (javafx.stage.Window w : c.getAddedSubList()) {
                    if (w.getScene() != null && !w.getScene().getStylesheets().contains(css)) {
                        w.getScene().getStylesheets().add(css);
                    }
                }
            }
        });

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
