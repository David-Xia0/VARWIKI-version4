package application.scenebuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AudioBar;
import application.Main;
import application.RunBash;
import application.creators.AudioCreator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;


/**
 * this class is used for new audio creation
 * It turns highlighted text into speech
 * @author student
 *
 */
public class NewAudioController {

	@FXML
	private TextArea _displayTextArea;

	@FXML
	private ChoiceBox<String> _festivalVoice;

	@FXML
	private ProgressIndicator _loading;

	
	private Scene _parent;
	private boolean _playing=false;
	private ExecutorService _team = Executors.newSingleThreadExecutor();
	private int _numAudioFiles;
	private ModifyAudioController _parentController;



	/**
	 * sets the defaults
	 * @param scene
	 * @param data
	 * @param current
	 * @param parentController
	 */
	public void setup(Scene scene, TemplateData data, int current, ModifyAudioController parentController) {
		// TODO Auto-generated method stub
		ObservableList<String> voices = FXCollections.observableArrayList();
		voices.addAll("Default","(voice_akl_nz_cw_cg_cg)","(voice_akl_nz_jdt_diphone)");
		_festivalVoice.setItems(voices);
		_festivalVoice.getSelectionModel().select(0);
		_parent = scene;
		_displayTextArea.setText(data.getText());
		_numAudioFiles = current;
		_parentController =  parentController;
	}

	/**
	 * sets this as the current scene
	 */
	public void setMe() {
		Main.getMainStage().setScene(_displayTextArea.getScene());
	}

	/**
	 * returns to previous scene
	 */
	@FXML
	void handleReturn() {
		Main.getMainStage().setScene(_parent);
	}


	/**
	 * calls the create audio class with the selected text
	 */
	@FXML
	void handleSaveAudio() {
		String selectedText=_displayTextArea.getSelectedText();

		if(selectedText.isEmpty()) {
			return;
		}

		AudioCreator createAudio = new AudioCreator(selectedText,_festivalVoice.getSelectionModel().getSelectedItem(),_numAudioFiles);
		_numAudioFiles++;
		_team.submit(createAudio);
		createAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				try {
					_parentController.addBar(_displayTextArea.getSelectedText(),createAudio.get());
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		});
		handleReturn();
	}

	/**
	 * uses festival to test the selected audio
	 * No files are create by this method
	 * @param event
	 */
	@FXML
	void handleTestAudio(ActionEvent event) {
		String selectedText  = _displayTextArea.getSelectedText();
		if(selectedText.isEmpty()||_playing) {
			return;
		}

		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		
		//if there is a voice package used, a differenet bash command needs to be called
		if(voice == null ||voice.contentEquals("Default")) {
			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | festival --tts");
			_team.submit(audioCreation);
			_playing = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_playing=false;
				}
			});

		}else {
			RunBash audioCreation = new RunBash("echo {\""+voice+"\",'(SayText \""+selectedText+"\")'} | bash -c festival");
			_team.submit(audioCreation);
			_playing = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_playing=false;
					if(audioCreation.returnError() != null && audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						Main.error("some words selected cannot be converted by selcted voice package");
						return;
					}
				}
			});
		}
	}

	//highlights all text and saves it, THis is a very time consuming function
	@FXML
	void saveAllAudio(ActionEvent event) {
		_displayTextArea.selectAll();
		handleSaveAudio();
	}
	
	public String getText() {
		return _displayTextArea.getText();
	}

}
