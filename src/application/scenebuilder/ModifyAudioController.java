package application.scenebuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import application.AudioBar;
import application.Main;
import application.Main.SceneType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

/**
 * this scene is used to handle already created audio files.
 * The user can play or delete and also move the position of audio file
 * @author student
 *
 */
public class ModifyAudioController {

	@FXML
	private ListView<HBox> _audioBox;
	private int audioCount = 0;
	private ObservableList<HBox> _audioList = FXCollections.observableArrayList();
	private TemplateData _data;
	private Scene _parent;
	private NewAudioController _newAudioScene;

	/**
	 * finds presetn audio files and loads them into a list view to display
	 * @param parent
	 * @param data
	 */
	public void setup(Scene parent, TemplateData data) {
		// TODO Auto-generated method stub
		
		_data=data;
		_parent = parent;
		if(!data.isTemplate()) {
			return;
		}
		for(String file: data.getOrder()) {
			new AudioBar(data.getText(audioCount),file,_audioList);
			audioCount++;
		}
		_audioBox.setItems(_audioList);
	}

	/**
	 * gets selected text from previous creation screen
	 * @return
	 */
	public String getText() {
		if(_newAudioScene!=null) {
			return _newAudioScene.getText();
		}
		return _data.getText();
	}
	
	

	/**
	 * switches to audio creation screen
	 */
	@FXML
	void handleNewAudio(){
		if(_newAudioScene == null) {
			_newAudioScene = (NewAudioController)Main.changeScene(SceneType.newAudio, this);
			_newAudioScene.setup(_audioBox.getScene(),_data,audioCount,this);
		}else {
			_newAudioScene.setMe();
		}
	}
	
	/* 
	 * The following five methods are used to edit play existing audio chunks
	 */
	private boolean checkAudioBar() {
		if(_audioBox.getSelectionModel().getSelectedItem()==null) {
			return false;
		}
		return true;
	}

	@FXML
	void handlePlayAudio(ActionEvent event) {
		if(checkAudioBar()) {
			HBox audio = _audioBox.getSelectionModel().getSelectedItem();
			((AudioBar) audio).playAudio();
		}
	}

	@FXML
	void handleDeleteAudio(ActionEvent event) {
		if(checkAudioBar()) {
			HBox audio = _audioBox.getSelectionModel().getSelectedItem();
			((AudioBar) audio).delete();
		}
	}

	@FXML
	void handleMoveAudioDown(ActionEvent event) {
		if(checkAudioBar()) {
			HBox audio = _audioBox.getSelectionModel().getSelectedItem();
			((AudioBar) audio).moveDown();
			_audioBox.getSelectionModel().clearSelection();
			_audioBox.getSelectionModel().select(audio);
		}
	}

	@FXML
	void handleMoveAudioUp(ActionEvent event) {
		if(checkAudioBar()) {
			HBox audio = _audioBox.getSelectionModel().getSelectedItem();
			((AudioBar) audio).moveUp();
			_audioBox.getSelectionModel().clearSelection();
			_audioBox.getSelectionModel().select(audio);
		}
	}

	@FXML
	void handleReturn(ActionEvent event) {
		Main.getMainStage().setScene(_parent);
	}

	/**
	 * adds a audio files to the list view
	 * @param selectedText
	 * @param success
	 */
	public void addBar(String selectedText,boolean success) {
			if(success) {
				new AudioBar(selectedText,audioCount+"",_audioList);
				_audioBox.setItems(_audioList);
				audioCount++;
			}
	}

	/**
	 * gets all audio text from already saved audio files
	 * @return
	 */
	public List<String> getAudioText() {
		List<String> audioText = new ArrayList<String>();
		for(Node node : _audioList) {
			audioText.add(((AudioBar) node).getText());
		}
		return audioText;
	}
	
	/**
	 * returns all audio files displayed in the list view
	 * @return
	 */
	public ObservableList<HBox> getAudioList(){
		return _audioList;
	}

	/**
	 * sets current scene as viewable
	 */
	public void setMe() {
		Main.getMainStage().setScene(_audioBox.getScene());
	}

}
