package application.scenebuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.Main;
import application.RunBash;
import application.VideoBar;
import application.Main.SceneType;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class QuizMenuController {

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private MediaBox player_;
	private double randomCreation;
	private List<Double> _answerOptions = new ArrayList<Double>();
	private List<Button> _answerButtons;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button _backButton;

	@FXML
	private Button _guess1Button;

	@FXML
	private Button _guess2Button;

	@FXML
	private Button _guess3Button;

	@FXML
	private Button _guess4Button;

	@FXML
	private HBox _hbox;

	@FXML
	void handleCheckAnswer(ActionEvent event) {
		 String answer = ((Button)event.getSource()).getText();
		 
		 String data = Main.getPathToResources() + "/templates/" +  _creations.get((int)randomCreation) + "/info.class";
			try {
				FileInputStream fileIn = new FileInputStream(data);
				ObjectInputStream object = new ObjectInputStream(fileIn);
				TemplateData template = (TemplateData) object.readObject();
				if(template.getTerm().contentEquals(answer)) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Correct");
					alert.setContentText("Nice! you got the answer correct");
					alert.showAndWait();
				}else {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Incorrect");
					alert.setContentText("Sorry! correct Answer was: "+ template.getTerm());
					alert.showAndWait();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		Main.changeScene(SceneType.QuizMenu, this);
	}


	@FXML
	void handleReturn(ActionEvent event) {
		exit(SceneType.MainMenu);
	}

	@FXML
	void initialize() {
		
		_answerButtons =  new ArrayList<>(Arrays.asList(_guess1Button, _guess2Button, _guess3Button, _guess4Button));
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
				for(int i=0;i<3;i++) {
					_answerOptions.add(Math.random()*_creations.size());
				}
				_answerOptions.add(randomCreation);
				

				System.out.println(_answerOptions);
				double position = Math.random()*(4);
				System.out.println(position);
				
				for(int i=0;i<4;i++) {
					String data = Main.getPathToResources() + "/templates/" +  _creations.get(_answerOptions.get(i).intValue()) + "/info.class";
					try {
						FileInputStream fileIn = new FileInputStream(data);
						ObjectInputStream object = new ObjectInputStream(fileIn);
						TemplateData template = (TemplateData) object.readObject();
						_answerButtons.get(((int)position)%4).setText(template.getTerm());
						position++;
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				

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
	
	private Object exit(SceneType location) {
		return Main.changeScene(location, this);
	}

}

