package pt.pedroponte.Bingo.controllers;

import org.springframework.stereotype.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import pt.pedroponte.Bingo.models.Cartao;

@Controller
public class CartoesController {
    @FXML
    private TableView<Cartao> tbl_cartoes = new TableView<>();
    @FXML
    private TableColumn<Cartao, String> nameColumn= new TableColumn<>(); // Ajuste o tipo conforme o modelo
    @FXML
    private TableColumn<Cartao, Integer> ageColumn = new TableColumn<>(); // Ajuste o tipo conforme o modelo
    @FXML
    private TableColumn<Cartao, Void> actionColumn;

    public void initialize() {
        // Configurar colunas
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));

        // Configurar mensagem para tabela vazia
        tbl_cartoes.setPlaceholder(new Label("Nenhum dado disponível"));

        // Dados de exemplo
        ObservableList<Cartao> data = FXCollections.observableArrayList(
            new Cartao("Alice", 25),
            new Cartao("Bob", 35)
        );

        // Adicionar dados à tabela
        tbl_cartoes.setItems(data);

        // Adicionar coluna de ações
        addButtonToTable();
    }

    private void addButtonToTable() {
        actionColumn = new TableColumn<>("Actions");

        Callback<TableColumn<Cartao, Void>, TableCell<Cartao, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Cartao, Void> call(final TableColumn<Cartao, Void> param) {
                final TableCell<Cartao, Void> cell = new TableCell<>() {

                    private final Button btnDelete = new Button("Delete");

                    {
                       

                        btnDelete.setOnAction(event -> {
                            Cartao cartao = getTableView().getItems().get(getIndex());
                            // Handle delete action
                            System.out.println("Delete: " + cartao.getName());
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hBox = new HBox( btnDelete);
                            setGraphic(hBox);
                        }
                    }
                };
                return cell;
            }
        };

        actionColumn.setCellFactory(cellFactory);
        tbl_cartoes.getColumns().add(actionColumn);
    }
}
