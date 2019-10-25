package application.scenebuilder;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ProcessHandle.Info;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.Main.SceneType;
import application.creators.AudioCreator;
import application.creators.VideoCreator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import application.*;


/**
 * 
 * @author Student
 * This contains all functionality for creating a video.
 * 
 *
 */
public class CreateMenuController implements Initializable{



	private ObservableList<HBox> _audioList = FXCollections.observableArrayList();
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private int audioCount=0;
	private SetImagesController _controller;
	private String __videoName;
	private boolean _cancel;
	private RunBash _audioPlaying;
	private String _term;
	private Stage _stage;
	private boolean _saving=false;
	private boolean _defaultImages=true;
	private int _i;
	private TemplateData _template;
	private String _selectedText;


	@FXML
	private Button _playButton;

	@FXML
	private Button _saveButton;


	@FXML 
	private ProgressIndicator _loading;

	@FXML
	private Button _imageButton;

	@FXML
	private Button _deleteButton;

	@FXML
	private ListView<HBox> _audioBox;

	@FXML
	private Button _upButton;

	@FXML
	private Button _downButton;

	@FXML
	private ChoiceBox<String> _musicChoiceBox;

	@FXML 
	private CheckBox _imageSelection;

	@FXML
	private Button _testButton;

	@FXML
	private Button _saveTemplateButton;

	@FXML
	private TextArea _displayTextArea;

	@FXML
	private ChoiceBox<String> _festivalVoice;

	@FXML
	private Button _createButton;

	@FXML
	private Button _returnButton;

	@FXML
	private Button _newSearchButton;

	@FXML
	private TextField _videoName;



	/**
	 * this initialises choice box to allow for the selection of different festival voices
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ObservableList<String> voices = FXCollections.observableArrayList();
		voices.addAll("Default","(voice_akl_nz_cw_cg_cg)","(voice_akl_nz_jdt_diphone)");
		_festivalVoice.setItems(voices);

		ObservableList<String> music = FXCollections.observableArrayList();
		music.addAll("No Music","victor_-_Calling_on_Dolphins.mp3");
		_musicChoiceBox.setItems(music);	
	}



	/**
	 * creation button used to build the final video
	 */
	@FXML
	void handleCreate(ActionEvent event) {

		//ERROR checking
		if(_runningThread) {
			error("Please Wait for Processes to Finish");
			return;
		}
		if(_defaultImages && _imageSelection.isSelected()) {
			boolean confirm  = defaultImages();
			if(!confirm) {
				return;
			}
		}

		if(_videoName.getText().isBlank()) {
			error("No Name set");
			return;
		}

		if(_audioList.isEmpty()){
			boolean confirmed = noText();
			if (!confirmed){
				return;
			}
			saveAllAudio();
		}

		if (_template != null) {

			if (!_template.getName().contentEquals(_videoName.getText())) {

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Are you sure?");
				alert.setHeaderText("Name change detected");
				alert.setContentText("Do you want to create a new creation?");
				Optional<ButtonType> result = alert.showAndWait();
				if(result.get() != ButtonType.OK) {
					return;
				} else {

					Alert alert2 = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("Save changes?");
					alert.setHeaderText("This will overwrite your current Creation");
					alert.setContentText("Do you want to save your changes?");
					Optional<ButtonType> result2 = alert2.showAndWait();
					if(result2.get() != ButtonType.OK) {
						return;
					}
				}
			}
		}


		_loading.setVisible(true);
		_createButton.setVisible(false);

		__videoName = _videoName.getText();

		if((!__videoName.matches("[a-zA-Z0-9_-]*"))) {
			error("name can only contain letter, numbers, _ and - ");
			return;
		}else{
			//checks if file already exists
			RunBash f = new RunBash("[ -e ./resources/VideoCreations/"+__videoName+".mp4 ]");
			_team.submit(f);
			f.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent arg0) {
					if(f.getExitStatus()== 0 ) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Save changes?");
						alert.setHeaderText("This will overwrite your current Creation");
						alert.setContentText("Do you want to save your changes?");
						Optional<ButtonType> result = alert.showAndWait();
						if(result.get() != ButtonType.OK) {
							_loading.setVisible(false);
							_createButton.setVisible(true);
							return;
						}else {
							RunBash remove = new RunBash("rm ./resources/VideoCreations/"+__videoName+".mp4");
							_team.submit(remove);
							handleSaveTemplate();
							createVideo();
						}
					}else {
						handleSaveTemplate();
						createVideo();
					}
				}


			});
			return;
		}
	}


	/**
	 * THis method contains most/all of the bash and ffmpeg commands used in video creation
	 */
	public void createVideo() {
		_runningThread=true;
		VideoCreator creator = new VideoCreator(this);
		_team.submit(creator);
		creator.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				exit();
				_runningThread=false;
			}
		});
	}


	/**
	 * if the user wishes to return to the main menu they can, but are prompted with a confirmation msg
	 */
	@FXML
	void handleReturn(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Unsaved work will be lost");
		alert.setContentText("Do you still want to EXIT?");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK) {
			exit();
		}

	}

	/**
	 * allows user to search for a new term after prompting with a confirmation msg
	 */
	@FXML
	void handleNewSearch(ActionEvent event) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Are you sure?");
		alert.setHeaderText("Unsaved work will be lost");
		alert.setContentText("Do you still want to start a new search?");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK) {
			exit(SceneType.Search);
		}

	}

	/**
	 * allows user to select whether to create a video using default images if user has not manually selected images 
	 * */
	private boolean defaultImages() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("No images selected");
		alert.setHeaderText("No images selected");
		alert.setContentText("would you like to create a creation with the default image selection");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}

	}


	/**
	 * allows user to select whether to create a video using the text in box, if user has not manually selected text
	 */
	private boolean noText() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("No audio created");
		alert.setHeaderText("No audio created");
		alert.setContentText("would you like to create a creation with the text currently in the box?");
		Optional<ButtonType> result = alert.showAndWait();
		if(result.get() == ButtonType.OK) {
			return true;
		} else {
			return false;
		}

	}


	/**
	 * creates the popup that is used to select images
	 */
	private void initializeSetImages(TemplateData data) {
		FXMLLoader loader = new FXMLLoader();

		loader.setLocation(getClass().getResource("SetImages.fxml"));
		Parent layout; 
		try {

			layout = loader.load();
			_controller=loader.getController();

			_controller.construct(this);
			Scene scene = new Scene(layout);
			_stage = new Stage();
			_stage.setScene(scene);
			_stage.initModality(Modality.APPLICATION_MODAL);
			_stage.initStyle(StageStyle.UNDECORATED);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Opens image selection menu
	 */
	@FXML
	void handleImages(ActionEvent event) {
		if (_runningThread) {
			error("A Process is currently running");
			return;
		}
		_defaultImages = false;
		popupSetImages();
	}


	public void popdownSetImages() {
		_stage.close();
	}

	private void popupSetImages() {
		_stage.show();
		if(_template!=null) {
			_controller.setSelectedImages(_template.getSelectedImages());
		}

	}

	/**
	 * this saves all text in box, regardless of what is selected
	 */
	@FXML
	private void saveAllAudio(){
		_displayTextArea.selectAll();
		handleSaveAudio();
		_displayTextArea.deselect();
	}	

	@FXML
	private void handleSaveAudio() {
		_selectedText=_displayTextArea.getSelectedText();

		if(_runningThread ==true || _selectedText.isEmpty()) {
			return;
		}

		_runningThread = true;
		AudioCreator createAudio = new AudioCreator(this);
		_team.submit(createAudio);
		createAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				try {
					if(createAudio.get()) {
						new AudioBar(_selectedText,audioCount+"",_audioList);
						_audioBox.setItems(_audioList);
						audioCount++;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
				_runningThread = false;
			}
		});
	}


	/**
	 * plays a preview of the selected audio text
	 * @param event
	 */
	@FXML
	void handleTestAudio(ActionEvent event) {

		if(_selectedText.isEmpty() || _runningThread) {
			return;
		}

		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		if(voice == null ||voice.contentEquals("Default")) {
			RunBash audioCreation = new RunBash("echo \"" + _selectedText + "\" | festival --tts");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
				}
			});

		}else {
			RunBash audioCreation = new RunBash("echo {\""+voice+"\",'(SayText \""+_selectedText+"\")'} | bash -c festival");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
					if(audioCreation.returnError() != null && audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						error("some words selected cannot be converted by selcted voice package");
						return;
					}
				}
			});
		}
	}


	/*
	 * 
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
		if(checkAudioBar()&&!_runningThread) {
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


	/**
	 * 
	 * @return currently selected images (default is all images selected)
	 */
	public List<String> getSelectedImages() {
		List<String> images = new ArrayList<String>();
		List<ImageElement> elements = _controller.getSelectedImages();

		for(ImageElement name: elements) {
			images.add(name.toString());
		}
		return images;
	}




	/**
	 * helper method that creates a popup when an error occurs
	 * @param msg
	 */
	public void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR "+msg);
		alert.setHeaderText("ERROR");
		alert.setContentText(msg);
		alert.showAndWait();
	}

	/**
	 * helper method that creates a popup when an error occurs
	 * @param msg
	 */
	public void notification(String msg) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(msg);
		alert.setHeaderText("Done");
		alert.setContentText(msg);
		alert.showAndWait();
	}


	/**
	 * passes Search info to scene
	 * @param text
	 * @param term
	 * @param fromTemplate (does a template already exist)
	 */
	public void setup(String text, String term,TemplateData template) {
		_displayTextArea.setText(text);
		_term = term;
		_festivalVoice.getSelectionModel().clearAndSelect(0);

		_runningThread = false;
		_template=template;
		initializeSetImages(template);
	}

	/**
	 * passes Search info to scene
	 * @param text
	 * @param term
	 */
	public void setup(String text, String term) {
		_videoName.setText(term);
		setup(text,term,null);
	}

	/**
	 * passes Template data info to scene
	 * @param data
	 */
	public void setup(TemplateData data) {
		String term = data.getTerm();
		String text = data.getText();

		//this is currently kinda glitchy (ie it doesnt work)

		String path = "./resources/templates/" + data.getName();
		_videoName.setText(data.getName());
		_team.submit(new RunBash("cp -rf " + path +"/images ./resources/temp"));
		Future<?> doneAudio = _team.submit(new RunBash("cp -rf " + path +"/audio ./resources/temp"));
		try {
			//forces code to wait for task completion
			doneAudio.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for(String file: data.getOrder()) {
			new AudioBar(data.getText(audioCount),file,_audioList);
			audioCount++;
		}
		_audioBox.setItems(_audioList);
		/*
		File folder = new File("./resources/temp/audio");
		for(int i=0; i<folder.listFiles().length; i++) {
			System.out.println(audioCount);
			new AudioBar(data.getText(audioCount),""+i,_audioList);
			_audioBox.setItems(_audioList);
			audioCount++;
		}
		 */
		_imageSelection.setSelected(data.usingImages());
		setup(text,term,data);
		if(data.getBGM() != null) {
			_musicChoiceBox.getSelectionModel().select(data.getBGM());
		}
	}


	private void handleSaveTemplate(){
		String path = "./resources/templates/" + _videoName.getText();
		File template = new File(path);
		if(template.exists()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Overwrite existing template?");
			alert.setHeaderText("Template with this name already exists");
			alert.setContentText("Overwrite with changes?");
			Optional<ButtonType> result = alert.showAndWait();
			if(result.get() != ButtonType.OK) {
				return;
			}else {
				template.delete();
			}

		}
		template.mkdir();
		_team.submit(new RunBash("cp -rf ./resources/temp/images "+ path));
		_team.submit(new RunBash("cp -rf ./resources/temp/audio "+ path));

		FileOutputStream fos;
		try {
			File file = new File(Main.getPathToResources() + "/templates/"+_videoName.getText() + "/info.class");
			//file.createNewFile();
			fos = new FileOutputStream(Main.getPathToResources() + "/templates/"+_videoName.getText() + "/info.class");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			TemplateData data = new TemplateData(this);
			oos.writeObject(data);
			oos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void exit(SceneType location) {
		//handleSaveTemplate();
		Main.changeScene(location, this);
	}

	//exits to default location
	private void exit() {
		exit(SceneType.MainMenu);
	}

	
	/*
	 * The following are all getters, this helps to translate GUI input information into back end classes used for
	 * video/audio Creation and other purposes
	 *
	 */

	public String getText() {
		return _displayTextArea.getText();
	}

	public String getName() {
		return _videoName.getText();
	}

	public String getTerm() {
		return _term;
	}

	public String getBGM() {
		return _musicChoiceBox.getSelectionModel().getSelectedItem();
	}

	public String getSelectedText() {
		return _displayTextArea.getSelectedText();
	}

	public List<String> getAudioText() {
		List<String> audioText = new ArrayList<String>();
		for(Node node : _audioList) {
			audioText.add(((AudioBar) node).getText());
		}

		return audioText;
	}

	public String getVoicePackage() {
		return _festivalVoice.getSelectionModel().getSelectedItem();
	}


	public boolean usingImages() {
		return _imageSelection.isSelected();
	}


	public List<String> fileOrder() {
		List<String> files = new ArrayList<String>();
		for(Node node : _audioList) {
			//if(node.getClass().getName() == "application.scenebuilder.AudioBar") {
			files.add(((AudioBar) node).getName());
			//}

		}
		return files;
	}
}
