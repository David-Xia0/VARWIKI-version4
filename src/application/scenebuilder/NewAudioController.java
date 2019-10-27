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

	public void setMe() {
		Main.getMainStage().setScene(_displayTextArea.getScene());
	}

	@FXML
	void handleReturn() {
		Main.getMainStage().setScene(_parent);
	}


	@FXML
	void handleSaveAudio(ActionEvent event) {
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

	@FXML
	void handleTestAudio(ActionEvent event) {
		String selectedText  = _displayTextArea.getSelectedText();
		if(selectedText.isEmpty()||_playing) {
			return;
		}

		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
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

	@FXML
	void saveAllAudio(ActionEvent event) {
		handleReturn();
	}
	
	public String getText() {
		return _displayTextArea.getText();
	}

}
