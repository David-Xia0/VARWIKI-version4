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
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
	private HBox _resultBackground;

	@FXML 
	private Text _score;
	private int _total=0;
	private int _correct=0;


	/**
	 * set the counting score board values
	 */
	public void setScore(String score,int correct,int total) {
		_score.setText(score);
		_total = total;
		_correct = correct;
	}

	/**
	 * reloads quiz menu and gives the user another quesiton to answer
	 * @param event
	 */
	@FXML
	void handleNextQuestion(ActionEvent event) {
		QuizMenuController controller = (QuizMenuController)exit(SceneType.QuizMenu);
		controller.setScore(_score.getText(),_correct,_total);
	}

	/**
	 * After the user presses one of the four buttons, this method is called
	 * The button is checked to see if it is the correct one
	 * @param event
	 */
	@FXML
	void handleCheckAnswer(ActionEvent event) {
		if(_threadRunning || _answered) {
			return;
		}
		_total++;
		String answer = ((Button)event.getSource()).getText();

		String data = Main.getPathToResources() + "/templates/" +  _creations.get((int)randomCreation) + "/info.class";
		try {
			FileInputStream fileIn = new FileInputStream(data);
			ObjectInputStream object = new ObjectInputStream(fileIn);
			TemplateData template = (TemplateData) object.readObject();
			_resultBackground.setVisible(true);
			if(template.getTerm().contentEquals(answer)) {
				_resultText.setText("Nice! You got it Correct!");
				_resultText.setFill(Paint.valueOf("#09ff0d"));
				_resultText.setVisible(true);
				_correct++;
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
		
		//Set indicators to tell the app the user has answered the question
		_resultText.setVisible(true);
		_answered=true;
		_nextQuestionButton.setVisible(true);
		_score.setText(_correct + "/" + _total);
	}


	/**
	 * Returns to the main menu screen
	 * @param event
	 */
	@FXML
	void handleReturn(ActionEvent event) {
		if(!_threadRunning) {
			exit(SceneType.MainMenu);
		}
	}

	
	@FXML
	void initialize() {
		_answerButtons =  new ArrayList<>(Arrays.asList(_guess1Button, _guess2Button, _guess3Button, _guess4Button));


		/*
		 * finds all avaliable video creations
		 */
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
				
				/*
				 * finds 4 random creations and sets one as the correct answer
				 */
				randomCreation = Math.random()*_creations.size();
				for(int i=0;i<3;i++) {
					_answerOptions.add(Math.random()*_creations.size());
				}
				_answerOptions.add(randomCreation);


				double position = Math.random()*(4);
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





				//This adds the videoPlayer to the scene and sets the video
				_player = new MediaBox();
				_hbox.getChildren().add(_player);	
				setNewMedia();
			}
		});
	}
	
	/**
	 * method that sets the video for the correct answer
	 */
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
	}

	private Object exit(SceneType location) {
		_player.pause();
		return Main.changeScene(location, this);
	}
}

