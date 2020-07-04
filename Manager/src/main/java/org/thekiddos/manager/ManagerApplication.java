package org.thekiddos.manager;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thekiddos.manager.gui.GUIApplication;

@SpringBootApplication
public class ManagerApplication {
    public static void main( String[] args ) {
        Application.launch( GUIApplication.class, args );
    }
}