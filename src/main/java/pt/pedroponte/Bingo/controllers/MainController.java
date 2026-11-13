package pt.pedroponte.Bingo.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pt.pedroponte.Bingo.JavaFXApplication;
import pt.pedroponte.Bingo.controllers.CartoesController;

@Controller
public class MainController {

    @Autowired
    private CartoesController cartoesController;

    @FXML
    private VBox main;
    @FXML
    private GridPane novogrid;
    @FXML
    private Pane pane_init;
    @FXML
    private Pane pane_novo;

    @FXML
    private Spinner quantidade;

    public void initialize() {
        quantidade.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 10));

    }

    public void novo() {
        pane_init.setVisible(false);
        pane_novo.setVisible(true);
        pane_init.setLayoutX(703); // Change the x position of pane_init
        pane_novo.setLayoutX(406); // Change the x position of pane_init

    }

    // Método para gerar um cartão

    public static List<List<String>> gerarCartao() {

        Map<String, int[]> intervalos = Map.of(
                "B", new int[] { 1, 15 },
                "I", new int[] { 16, 30 },
                "N", new int[] { 31, 45 },
                "G", new int[] { 46, 60 },
                "O", new int[] { 61, 75 });

        Map<String, List<String>> cartao = new HashMap<>();
        Random rand = new Random();

        for (String coluna : intervalos.keySet()) {
            int min = intervalos.get(coluna)[0];
            int max = intervalos.get(coluna)[1];
            int count = coluna.equals("N") ? 4 : 5;
            System.out.println("min: " + min + " max: " + max + " count: " + count);
            List<String> numeros = new LinkedList<>();
            while (numeros.size() < count) {
                int n = rand.nextInt(max - min + 1) + min;
                if (!numeros.contains(String.valueOf(n))) {
                    numeros.add(String.valueOf(n));
                }
            }
            cartao.put(coluna, numeros);

        }
        cartao.get("N").add(2, "Livre");

        // Montar a grade do cartão (lista de listas)
        List<List<String>> grade = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<String> linha = new ArrayList<>();
            for (String coluna : "BINGO".split("")) {
                linha.add(cartao.get(coluna).get(i));
            }
            grade.add(linha);
        }

        return grade;
    }

    // Método para gerar múltiplos cartões únicos
    public static List<List<List<String>>> gerarCartoesUnicos(int quantidade) {

        Set<List<List<String>>> cartoesUnicos = new HashSet<>();

        while (cartoesUnicos.size() < quantidade) {
            List<List<String>> novoCartao = gerarCartao();
            if (!cartoesUnicos.contains(novoCartao)) {
                cartoesUnicos.add(novoCartao);
            }
        }

        return new ArrayList<>(cartoesUnicos);
    }

    public void gerarCartoes() {
        JavaFXApplication.cartoes = gerarCartoesUnicos((int) quantidade.getValue());

        // Exibir os cartões gerados
        for (int i = 0; i < JavaFXApplication.cartoes.size(); i++) {
            System.out.println("Cartão " + (i + 1) + ":");
            for (List<String> linha : JavaFXApplication.cartoes
                    .get(i)) {
                System.out.println(linha);
            }
            System.out.println();
        }
        ConfigurableApplicationContext springContext = JavaFXApplication.springContext;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/bingo.fxml"));
            fxmlLoader.setControllerFactory(springContext::getBean);
            Parent root = fxmlLoader.load();
            Stage primaryStage = new Stage();
            primaryStage.setTitle("JavaFX com Spring Boot");
            primaryStage.setScene(new Scene(root));
           
            main.getScene().getWindow().hide();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void gestaoCartoes() {
        ConfigurableApplicationContext springContext = JavaFXApplication.springContext;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/cartoes.fxml"));
            fxmlLoader.setControllerFactory(springContext::getBean);
            Parent root = fxmlLoader.load();
            Stage primaryStage = new Stage();
            primaryStage.setTitle("JavaFX com Spring Boot");
            primaryStage.setScene(new Scene(root));
           
            main.getScene().getWindow().hide();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
