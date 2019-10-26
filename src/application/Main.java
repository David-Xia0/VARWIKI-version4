package application;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.scenebuilder.CreateMenuController;
import application.scenebuilder.MainMenuController;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class Main extends Application {

	private static ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private static Stage _stage;


	public enum SceneType{
		MainMenu("MainMenu.fxml"),
		CreateMenu("CreateMenu.fxml"),
		Search("Search.fxml"),
		QuizMenu("QuizMenu.fxml"),
		Loader("Loading.fxml");
		private String fileName;
		private SceneType(String name) {
			fileName = name;
		}
		private String getFile() {
			return fileName;
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		initiateFileSystem();
		//creates loading screen
		Stage loaderStage = new Stage();
		loaderStage.initStyle(StageStyle.TRANSPARENT);
		changeScene(SceneType.Loader, this, loaderStage);
		loaderStage.show();
		primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/resources/facebook.png")));
		PauseTransition pause = new PauseTransition(Duration.seconds(3));
		pause.setOnFinished(event -> {
				_stage=primaryStage;
				_stage.setResizable(false);
		        changeScene(SceneType.MainMenu,this);
		        _stage.show();
				primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

					@Override
					public void handle(WindowEvent t) {
						initiateFileSystem();
						System.exit(0);
					}
				});	
		        loaderStage.close();
		});
		pause.play();
/*
		Platform.runLater(new Runnable(){
			public void run() {
				changeScene(SceneType.MainMenu,this);
				//Sets the whole program to close when application window is closed

	
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//loaderStage.close();
				_stage.show();
			}	
		});
		*/

		//Initiate the first Scene




	}

	public static void main(String[] args) {
		launch();
	}

	/**
	 * used to change FXML scenebuilder scenes
	 * @param fxml
	 * @param location
	 */
	public static Object changeScene(SceneType sceneType, Object location) {
		return changeScene(sceneType,location,_stage);
	}

	/**
	 * used to change FXML scenebuilder scenes without destroying directories
	 * @param fxml
	 * @param location
	 */
	public static Object changeScene(SceneType sceneType, Object location, Stage stage) {
		//if(destroyFiles) {
		//	initiateFileSystem();
		//}
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(location.getClass().getResource(sceneType.getFile()));
			String fxmlDocPath = System.getProperty("user.dir") + "/src/application/scenebuilder/"+ sceneType.getFile();
			FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
			Parent layout = loader.load(fxmlStream);
			Scene scene = new Scene(layout);
			stage.setScene(scene);


			return loader.getController();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * method creates and clears any temp files used in the process of creation
	 */
	public static void initiateFileSystem() {
		_team.submit(new RunBash("rm -r ./resources/temp"));
		_team.submit(new RunBash("mkdir ./resources ./resources/VideoCreations ./resources/temp ./resources/temp/images ./resources/temp/audio ./resources/templates"));
	}


	public static String getPathToResources() {
		try {
			String dir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
			dir = dir.substring(0, dir.lastIndexOf("/"));
			return dir + "/resources";
		} catch (URISyntaxException e) {
			System.out.println("I/O issue, unexpected setup");
			e.printStackTrace();
		}
		return System.getProperty("user.dir") + "/bin/resources";
	}


	/**
	 * helper method that creates a popup when an error occurs
	 * @param msg
	 */
	public static void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR "+msg);
		alert.setHeaderText("ERROR");
		alert.setContentText(msg);
		alert.showAndWait();
	}

}
