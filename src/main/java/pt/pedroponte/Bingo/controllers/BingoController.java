package pt.pedroponte.Bingo.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import pt.pedroponte.Bingo.JavaFXApplication;

@Controller
public class BingoController {

    @FXML
    private VBox main;
    @FXML
    private HBox numeros_altura;
    @FXML
    private Label label_numero;
    @FXML
    private Label ultimoNumero;
    @FXML
    private Label contador;
    @FXML
    private Label timer_label;
    @FXML
    private Spinner<Integer> txtac;
    @FXML
    private ToggleButton toggleButton;

    @FXML
    private FlowPane numeros;

    public LinkedList list = new LinkedList();
    private int last = 0;
    private double width = 0;
    private double height = 0;
    private int tirar = 0;
    private Timeline timeline;
    private int count = 0;

    public void initialize() {
        // Configuração do Spinner com valor mínimo, máximo e inicial
        txtac.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 10));
        // numeros.setHgap(10); // Set horizontal gap between labels
        // numeros.setVgap(10);
        label_numero.setText("");
        main.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
            numeros.requestLayout();
            main.requestLayout();
            width = numeros.getWidth() / 10;
            height = numeros.getHeight() / 10;
            updateLabelSizes();
        });

        // Listeners para redimensionamento
        numeros.widthProperty().addListener((observable, oldValue, newValue) -> {
            width = newValue.intValue() / 10;
            updateLabelSizes();
        });

        numeros.heightProperty().addListener((observable, oldValue, newValue) -> {
            height = newValue.intValue() / 10;
            updateLabelSizes();
        });
        populateLabels();

        toggleButton.setOnAction(event -> handleToggleButton(event));
    }

    private void populateLabels() {
        for (int i = 1; i <= 90; i++) {
            this.list.add(i);

            Label label = new Label(String.valueOf(i));
            label.setFont(javafx.scene.text.Font.font(37));
            label.setId("label_numero" + i);
            label.setCenterShape(true);
            label.getStyleClass().add("label_numero");

            label.setAlignment(javafx.geometry.Pos.CENTER);
            label.setPadding(new javafx.geometry.Insets(5)); // Add padding to the label
            numeros.getChildren().add(label);
        }
    }

    private void updateLabelSizes() {

        double labelWidth = (width) / 10; // Adjust width for gaps
        double labelHeight = (height) / 10; // Adjust height for gaps

        for (Label label : numeros.getChildren().stream()
                .filter(node -> node instanceof Label)
                .map(node -> (Label) node)
                .collect(Collectors.toList())) {
            label.setPrefWidth(width);
            label.setPrefHeight(height);

        }
        System.out.println("width: " + width + " height: " + height);
        numeros.requestLayout();
        main.requestLayout();

    }

    @FXML
    public void callbingo() {
        TextField textField = new TextField();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Bingo");
        alert.setContentText("Numero do cartão:");
        alert.getDialogPane().setContent(textField);
        alert.showAndWait();

        int playerName = Integer.parseInt(textField.getText());
        final boolean[] bingo = { true };
        try {
                 

            List<List<String>> list2 = JavaFXApplication.cartoes.get(playerName - 1);
            list2.get(2).remove(2);
            list2.forEach(linha -> {
                linha.forEach(numero -> {
                    System.out.println(numero);
                    if (list.contains(Integer.parseInt(numero))) {
                        bingo[0] = false;

                    }
                });
                System.out.println("-----------------");
            });
            if (bingo[0]) {
                pararPiscar();
                Alert alertBingo = new Alert(Alert.AlertType.INFORMATION);
                alertBingo.setTitle("Bingo");
                alertBingo.setContentText("Parabéns! Você fez Bingo!");
                alertBingo.showAndWait();
                list2.forEach(linha -> {
                    linha.forEach(numero -> {
                        Label label = (Label) numeros.lookup("#label_numero" + numero);
                        if (label != null) {
                            Timeline timeline3 = timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> label.setStyle("-fx-background-color: #458f22;")),
                new KeyFrame(Duration.millis(500), e -> label.setStyle("-fx-background-color: #ff0000;")));
                timeline3.setCycleCount(Timeline.INDEFINITE);
                timeline3.setAutoReverse(true);
                timeline3.play();
                        }
                    });
                });
            } else if (!bingo[0]) {
                
            
                Alert alertBingo = new Alert(Alert.AlertType.INFORMATION);
                alertBingo.setTitle("Bingo");
                alertBingo.setContentText("Você ainda não fez Bingo!");
                alertBingo.showAndWait();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: handle exception
        }

    }

    @FXML
    public void updateLabel() {

        System.out.println("list size: " + this.list.size());
        if (this.list.size() == 0) {
            return;
        }
        last = tirar;
        tirar = tirar();
        int x = (int) (count++) + 1;
        contador.setText("" + x);

        ultimoNumero.setText("" + last);
        label_numero.setText("" + tirar);
        Label lastLabel = (Label) numeros.lookup("#label_numero" + last);
        if (lastLabel != null) {
            System.out.println("lastLabel: " + lastLabel);
            pararPiscar();

        }
        Label label = (Label) numeros.lookup("#label_numero" + tirar);
        label.setStyle("-fx-background-color: #458f22;");
        iniciarPiscar(label);

        numeros.requestLayout();
    }

    private void iniciarPiscar(Label label) {
        pararPiscar(); // Garante que não há outra animação em execução
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> label.setStyle("-fx-background-color: #458f22;")),
                new KeyFrame(Duration.millis(500), e -> label.setStyle("-fx-background-color: #ff0000;")));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setAutoReverse(true);
        timeline.play();
    }

    private void pararPiscar() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    public int tirar() {
        int max = this.list.size();
        int min = 0;
        Random rand = new Random();
        int n = rand.nextInt(max - min + min);
        int x = (Integer) this.list.get(n);
        this.list.remove(n);
        return x;
    }

 

    private Timeline timeline2;

    public void autimaticCalling() {

        timeline2 = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> {
                    String currentTime = timer_label.getText();
                    int time = currentTime.isEmpty() ? 0 : Integer.parseInt(currentTime);
                    if (time == txtac.getValue().intValue()) {
                        updateLabel();
                        timer_label.setText("0");
                        return;

                    }
                    timer_label.setText(String.valueOf(time + 1));
                }));
        timeline2.setCycleCount(Timeline.INDEFINITE); // Repetir indefinidamente
        timeline2.play();
    }

    public void stopCalling() {
        if (timeline2 != null) {

            timeline2.stop(); // Para a animação
            timer_label.setText("0");

        }
    }

    @FXML
    public void handleToggleButton(Event event) {
        if (toggleButton.isSelected()) {
            autimaticCalling();
            toggleButton.setText("Stop");
        } else {
            stopCalling();
            toggleButton.setText("Start");
        }
    }
}