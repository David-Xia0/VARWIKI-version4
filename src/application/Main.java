package application;

import java.io.File;
import java.io.FileInputStream;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.scenebuilder.CreateMenuController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {

	private static ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private static Stage _stage;
	
	
	public enum SceneType{
		MainMenu("MainMenu.fxml"),
		CreateMenu("CreateMenu.fxml"),
		Search("Search.fxml"),
		QuizMenu("QuizMenu.fxml");
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

		//Initiate the first Scene
	
		_stage=primaryStage;
		changeScene(SceneType.MainMenu,this);
		//Sets the whole program to close when application window is closed
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent t) {
				System.exit(0);
			}
		});		
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
		return changeScene(sceneType,location,true);
	}
	
	/**
	 * used to change FXML scenebuilder scenes without destroying directories
	 * @param fxml
	 * @param location
	 */
	public static Object changeScene(SceneType sceneType, Object location, boolean destroyFiles) {
		if(destroyFiles) {
			initiateFileSystem();
		}
		try {
			 FXMLLoader loader = new FXMLLoader();
		        loader.setLocation(location.getClass().getResource(sceneType.getFile()));
		        String fxmlDocPath = System.getProperty("user.dir") + "/src/application/scenebuilder/"+ sceneType.getFile();
		        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
		        Parent layout = loader.load(fxmlStream);
		        Scene scene = new Scene(layout);
		        _stage.setScene(scene);
		        _stage.show();
		        
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
