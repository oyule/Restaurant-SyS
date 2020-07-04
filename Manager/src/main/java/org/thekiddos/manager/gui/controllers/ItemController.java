package org.thekiddos.manager.gui.controllers;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.thekiddos.manager.Util;
import org.thekiddos.manager.models.Item;
import org.thekiddos.manager.repositories.Database;
import org.thekiddos.manager.transactions.DeleteItemTransaction;

public class ItemController extends Controller {
    public VBox root;
    public JFXButton addItemButton;
    public TableView<Item> itemTable;
    public TableColumn<Item, Long> itemIdColumn;
    public TableColumn<Item, String> itemNameColumn;
    public TableColumn<Item, Double> itemPriceColumn;
    public TableColumn<Item, String> itemDescriptionColumn;
    public JFXButton removeItemButton;

    public void initialize() {
        itemIdColumn.setCellValueFactory( new PropertyValueFactory<>( "id" ) );
        itemNameColumn.setCellValueFactory( new PropertyValueFactory<>( "name" ) );
        itemPriceColumn.setCellValueFactory( new PropertyValueFactory<>( "price" ) );
        itemDescriptionColumn.setCellValueFactory( new PropertyValueFactory<>( "description" ) );
        removeItemButton.disableProperty().bind( itemTable.getSelectionModel().selectedItemProperty().isNull() );
        fillItemTableView();
    }

    private void fillItemTableView() {
        itemTable.getItems().clear();
        for ( Item item : Database.getItems() ) {
            itemTable.getItems().add( item );
        }
    }

    public void addItem( ActionEvent actionEvent ) {
        Stage addItemStage = Util.getWindowContainer( "Add Item" ).getStage();
        addItemStage.setOnCloseRequest( e -> fillItemTableView() );
        addItemStage.show();
    }

    public void removeItem( ActionEvent actionEvent ) {
        Long itemId = itemTable.getSelectionModel().getSelectedItem().getId();

        new DeleteItemTransaction( itemId ).execute();
        fillItemTableView();
    }

    @Override
    public Node getRoot() {
        return root;
    }
}