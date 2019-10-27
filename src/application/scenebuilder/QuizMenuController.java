package application.scenebuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.Main;
import application.RunBash;
import application.Main.SceneType;
import application.creators.VideoCreator;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;

public class QuizMenuController {

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private MediaBox _player;
	private double randomCreation;
	private List<Double> _answerOptions = new ArrayList<Double>();
	private List<Button> _answerButtons;
	private boolean _threadRunning;
	private boolean _answered;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private Button _backButton;

	@FXML
	private HBox _hbox;

	@FXML
	private Button _guess1Button;

	@FXML
	private Button _guess2Button;

	@FXML
	private Button _guess3Button;

	@FXML
	private Button _guess4Button;

	@FXML
	private Button _nextQuestionButton;

	@FXML
	private Text _resultText;
	
	@FXML
	private HBox _answerBackground;




	@FXML
	void handleNextQuestion(ActionEvent event) {
		exit(SceneType.QuizMenu);
	}

	@FXML
	void handleCheckAnswer(ActionEvent event) {
		if(_threadRunning || _answered) {
			return;
		}

		String answer = ((Button)event.getSource()).getText();

		String data = Main.getPathToResources() + "/templates/" +  _creations.get((int)randomCreation) + "/info.class";
		try {
			FileInputStream fileIn = new FileInputStream(data);
			ObjectInputStream object = new ObjectInputStream(fileIn);
			TemplateData template = (TemplateData) object.readObject();
			if(template.getTerm().contentEquals(answer)) {
				_resultText.setText("Nice! You got it Correct!");
				_resultText.setFill(Paint.valueOf("#30d01b"));
				_resultText.setVisible(true);
			}else {
				_resultText.setText("Sorry! Correct Answer Was "+_creations.get((int)randomCreation));
				_resultText.setFill(Paint.valueOf("#d70606"));
				_resultText.setVisible(true);
			}
			object.close();
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
		_answerBackground.setVisible(true);
		_answered=true;
		_nextQuestionButton.setVisible(true);
	}


	@FXML
	void handleReturn(ActionEvent event) {
		if(!_threadRunning) {
			exit(SceneType.MainMenu);
		}
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
						object.close();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}





				//This adds the videoPlayer to the scene
				_player = new MediaBox();

				_hbox.getChildren().add(_player);	
				setNewMedia();
			}
		});
	}

	private void setNewMedia() {
		//setup(currentSelection);	
		URL mediaUrl;
		try {
			mediaUrl = new File(Main.getPathToResources() + "/VideoCreations/"+_creations.get((int)randomCreation)+".mp4").toURI().toURL();
			Media newMedia = new Media(mediaUrl.toExternalForm());
			_player.setMedia(newMedia);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/**
		try {
			FileInputStream fileIn = new FileInputStream(data);
			ObjectInputStream object = new ObjectInputStream(fileIn);
			TemplateData template = (TemplateData) object.readObject();
			object.close();

			VideoCreator video = new VideoCreator(template);
			_team.submit(video);
			video.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent event) {
					loadVideo();
				}
			});


		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */

	}

	private void loadVideo() {
		_player.pause();
		URL mediaUrl;
		try {
			mediaUrl = new File(Main.getPathToResources() + "/temp/matchingVideo.mp4").toURI().toURL();
			System.out.println(Main.getPathToResources());
			Media newMedia = new Media(mediaUrl.toExternalForm());
			_player.setMedia(newMedia);
		} catch (Exception e) {
			e.printStackTrace();
		}

	
	}

	private Object exit(SceneType location) {
		_player.pause();
		return Main.changeScene(location, this);
	}
}

