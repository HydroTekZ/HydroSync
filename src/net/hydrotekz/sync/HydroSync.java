package net.hydrotekz.sync;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class HydroSync extends Application {

	public static double version = 1.0;

	public static void main(String[] args){
		// Launch graphics
//		launch(args);

		// Start internal processes
		MainCore.startApplication();
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("HydroSync v" + version);

		BorderPane componentLayout = new BorderPane();

		Scene appScene = new Scene(componentLayout,500,500);

		primaryStage.setScene(appScene);
		primaryStage.show();
	}
}