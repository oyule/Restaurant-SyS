package org.thekiddos.manager.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.thekiddos.manager.ManagerApplication;
import org.thekiddos.manager.Util;
import org.thekiddos.manager.gui.controllers.*;
import org.thekiddos.manager.gui.models.WindowContainer;

// TODO create refreshable interface or something
// TODO a lot of the controllers share the same logic abstract it
public final class GUIApplication extends Application {
    private ConfigurableApplicationContext applicationContext;
    private Thread refreshThread;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray( new String[0] );

        this.applicationContext = new SpringApplicationBuilder()
                .sources( ManagerApplication.class )
                .run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Util.ICON = new Image( Util.getResource( "static/images/icon.png" ).toExternalForm() );
        Stage invoiceStage = Util.createWindowContainer( InvoiceController.class, null, "Invoice Summary" ).getStage();
        invoiceStage.initModality( Modality.NONE );

        Stage payCheckStage = Util.createWindowContainer( PayCheckController.class, null, "Pay Check" ).getStage();
        payCheckStage.initModality( Modality.NONE );

        Stage orderStage = Util.createWindowContainer( OrderController.class, null, "Order Summary" ).getStage();
        orderStage.initModality( Modality.NONE );

        Stage addReservationGUIStage = Util.createWindowContainer( AddReservationGUIController.class, null, "Add Reservation" ).getStage();
        addReservationGUIStage.initModality( Modality.NONE );

        Stage addItemGUIStage = Util.createWindowContainer( AddItemController.class, null, "Add Item" ).getStage();
        addItemGUIStage.initModality( Modality.NONE );

        Stage addEmployeeStage = Util.createWindowContainer( AddEmployeeController.class, null, "Add Employee" ).getStage();
        addEmployeeStage.initModality( Modality.NONE );

        Util.createWindowContainer( TableController.class, null, "Tables" );
        Util.createWindowContainer( CustomerController.class, null, "Customers" );
        Util.createWindowContainer( ItemController.class, null, "Items" );
        Util.createWindowContainer( EmployeeController.class, null, "Employees" );
        Util.createWindowContainer( PayrollController.class, null,"Payroll" );

        primaryStage = Util.createWindowContainer( GUIController.class, null, "Digital Restaurant Manager" ).getStage();
        primaryStage.setOnCloseRequest( e -> Platform.exit() );
        primaryStage.show();

        refreshThread = new Thread( () -> {
            while ( true ) {
                Platform.runLater( () -> refresh() );
                try {
                    Thread.sleep( 3000 );
                } catch ( InterruptedException e ) {
                    refreshThread = null;
                    break;
                }
            }
        });

        refreshThread.start();
    }

    private void refresh() {
        for ( WindowContainer windowContainer : Util.getWindowContainers() ) {
            windowContainer.getController().refresh();
        }
    }

    @Override
    public void stop() {
        refreshThread.interrupt();
        this.applicationContext.close();
        Platform.exit();
    }

    public static void main( String[] args) {
        launch(args);
    }
}
