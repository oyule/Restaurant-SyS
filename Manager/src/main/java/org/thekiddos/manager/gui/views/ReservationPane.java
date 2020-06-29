package org.thekiddos.manager.gui.views;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.thekiddos.manager.Util;
import org.thekiddos.manager.gui.Remover;
import org.thekiddos.manager.models.Reservation;
import org.thekiddos.manager.transactions.ActivateReservationTransaction;

public class ReservationPane extends TitledPane {
    private static int reservationNumber = 0;
    private final Reservation reservation;
    private final GridPane content = new GridPane();
    private final Label totalLabel = new Label();
    private final JFXCheckBox isReservationActiveCheckBox = new JFXCheckBox( "Active" );
    private final Remover remover;
    private JFXButton viewOrderButton,
            activateButton,
            checkOutButton;

    public ReservationPane( Reservation reservation, Remover remover ) {
        this.reservation = reservation;
        this.remover = remover;
        setup();
    }

    private void setup() {
        setText( nextReservationNumber() );
        setExpanded( false );
        setContent( content );
        isReservationActiveCheckBox.setDisable( true );
        isReservationActiveCheckBox.setSelected( reservation.isActive() );
        totalLabel.setText( String.valueOf( reservation.getTotal() ) );
        viewOrderButton = Util.createButton( "View Order", "static/images/order.png" );
        activateButton = Util.createButton( "Customer Arrived", "static/images/arrived.png" );
        checkOutButton = Util.createButton( "Check", "static/images/check.png" );

        if ( isReservationActiveCheckBox.isSelected() )
            activateButton.setDisable( true );

        viewOrderButton.setOnAction( this::viewOrder );
        activateButton.setOnAction( this::activateReservation );
        checkOutButton.setOnAction( this::checkOut );

        createLayout();
    }

    private String nextReservationNumber() {
        ++reservationNumber;
        return "Reservation #" + reservationNumber;
    }

    private void activateReservation( ActionEvent actionEvent ) {
        new ActivateReservationTransaction( reservation.getTableId() ).execute();

        activateButton.setDisable( true );
        isReservationActiveCheckBox.setSelected( true );
    }

    private void viewOrder( ActionEvent actionEvent ) {
    }

    private void checkOut( ActionEvent actionEvent ) {
        remover.remove( this );
    }

    private void createLayout() {
        content.setHgap( 30 );
        content.setVgap( 30 );
        content.setAlignment( Pos.CENTER );
        content.add( new Label( "Customer Id: " + reservation.getCustomerId() ), 0, 0 );
        content.add( new Label( "Table Id: " + reservation.getTableId() ), 1, 0 );
        content.add( isReservationActiveCheckBox, 2, 0 );
        content.add( new Label( "Reservation Date: " + reservation.getDate() ), 0, 1 );
        content.add( new Label( "Reservation Time: " + reservation.getTime().getHour() + ":" + reservation.getTime().getMinute() ), 1, 1 );
        content.add( new HBox( new Label( "Total: " ), totalLabel ), 2, 1 );
        content.add( viewOrderButton, 0, 2 );
        content.add( activateButton, 1, 2 );
        content.add( checkOutButton, 2, 2 );
    }
}