package application.scenebuilder;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.Main;
import application.RunBash;
import application.VideoBar;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class QuizMenuController {

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private MediaBox player_;
	private double randomCreation;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button _backButton;

	@FXML
	private Button _checkButton;

	@FXML
	private Button _giveUpButton;

	@FXML
	private TextField _answerField;

	@FXML
	private HBox _hbox;

	@FXML
	void handleCheckAnswer(ActionEvent event) {

	}

	@FXML
	void handleGiveUp(ActionEvent event) {

	}


	@FXML
	void handleReturn(ActionEvent event) {
		Main.changeScene("MainMenu.fxml", this);
	}

	@FXML
	void initialize() {
		RunBash bash = new RunBash("List=`ls ./resources/VideoCreations` ; List=${List//.???/} ; printf \"${List// /.\\\\n}\\n\"");
		_team.submit(bash);
		bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {


				try {
					_creations = bash.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
				randomCreation = Math.random()*_creations.size();


				//This adds the videoPlayer to the scene
				player_ = new MediaBox();

				_hbox.getChildren().add(player_);	
				setNewMedia();
			}
		});
	}

	private void setNewMedia() {
		//setup(currentSelection);	
		String creation = _creations.get((int) randomCreation);

		URL mediaUrl;
		try {
			mediaUrl = new File(Main.getPathToResources() + "/VideoCreations/"+creation+".mp4").toURI().toURL();
			Media newMedia = new Media(mediaUrl.toExternalForm());
			player_.setMedia(newMedia);
		} catch (Exception e) {
			//e.printStackTrace();
			
		}
	}
}

