package application.scenebuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.AudioBar;
import application.Main;
import application.RunBash;
import application.Main.SceneType;
import application.creators.AudioCreator;
import application.creators.VideoCreator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;

/**
 * connection point between all video and audio creation tools
 * @author student
 *
 */
public class CreateHubController implements Initializable{


	private ModifyAudioController _audioControl = null;
	private ModifyImagesController _imageControl=null;
	private boolean _loaded=true, _defaultImages=true, _doneSaving = true;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private TemplateData _data;
	private String _term;
	private String _name;
	private boolean  _newAudioList=false;
	
	
	
	@FXML
	private Button _imageButton;

	@FXML
	private Button _createButton;

	@FXML
	private Button _returnButton;

	@FXML
	private TextField _videoName;

	@FXML
	private CheckBox _imageSelection;

	@FXML
	private Button _newSearchButton;

	@FXML
	private ProgressIndicator _loading;
	
	@FXML
	private ChoiceBox<String> _musicChoiceBox;
	

	/**
	 * this initialises choice box to allow for the selection of different festival voices
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {


		ObservableList<String> music = FXCollections.observableArrayList();
		music.addAll("No Music","victor_-_Calling_on_Dolphins.mp3");
		_musicChoiceBox.setItems(music);	
		_musicChoiceBox.getSelectionModel().select(0);
	}


	/**
	 * finalises video creation by merging audio and video files
	 * @param event
	 */
	@FXML
	void handleCreate(ActionEvent event) {
		
		/*
		 * error handling before video creation process, ensures user inputs are correct
		 */
		if(_videoName.getText().isBlank()) {
			Main.error("No Name set");
			return;
		}
		
		List<String> audioList;
		if(_audioControl!=null){
			audioList = _audioControl.getAudioText();
		}else {
			audioList = _data.getAudioText();
		}

		if(audioList==null||audioList.isEmpty()) {
			Main.error("Please add some audio");
			return;
		}

		if(_defaultImages && _imageSelection.isSelected()) {
			boolean confirm  = defaultImages();
			if(!confirm) {
				return;
			}
		}
		
		//if creation is from an existing template ask if user wants to make new creation or overwrite previous
		if (_data.isTemplate()) {

			if (!_data.getName().contentEquals(_videoName.getText())) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Are you sure?");
				alert.setHeaderText("Name change detected");
				alert.setContentText("Do you want to create a new creation?");
				Optional<ButtonType> result = alert.showAndWait();
				if(result.get() != ButtonType.OK) {
					return;
				}
			}
		}

		_name = _videoName.getText();

		if((!_name.matches("[a-zA-Z0-9_-]*"))) {
			Main.error("name can only contain letter, numbers, _ and - ");
			return;
		}else{
			//checks if file already exists
			RunBash f = new RunBash("[ -e ./resources/VideoCreations/"+_name+".mp4 ]");
			_team.submit(f);
			f.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

				@Override
				public void handle(WorkerStateEvent arg0) {
					if(f.getExitStatus()== 0 ) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Creation already Exists?");
						alert.setHeaderText("A Creation with the same name already Exists");
						alert.setContentText("Do you want to overwrite?");
						Optional<ButtonType> result = alert.showAndWait();
						if(result.get() != ButtonType.OK) {
							return;
						}
						RunBash remove = new RunBash("rm ./resources/VideoCreations/"+_name+".mp4");
						_team.submit(remove);
						handleSaveTemplate();
						createVideo();

					}else {
						handleSaveTemplate();
						createVideo();
					}
					_loading.setVisible(true);
					_createButton.setVisible(false);
				}
			});
			return;
		}
	}

	/**
	 * THis method contains most/all of the bash and ffmpeg commands used in video creation
	 */
	public void createVideo() {
		VideoCreator creator = new VideoCreator(this);
		_team.submit(creator);
		creator.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				exit(SceneType.MainMenu);
			}
		});
	}

	private void handleSaveTemplate(){

		String path = "./resources/templates/" + _videoName.getText();
		File template = new File(path);
		/*if(template.exists()) {
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

		}*/
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

		Main.initiateFileSystem();
		Main.changeScene(location, this);
	}


	/**
	 * sets up existing audio if any
	 */
	@FXML
	void handleToCurrentAudio() {
		if(_audioControl == null) {
			_audioControl = (ModifyAudioController)Main.changeScene(SceneType.Audio, this);
			_audioControl.setup(_videoName.getScene(),_data);
		}else {
			_audioControl.setMe();
		}
	}


	/**
	 * handles image selciton transistion
	 * @param event
	 */
	@FXML
	void handleImages(ActionEvent event) {
		_defaultImages=false;
		if(_imageControl ==null) {
			_imageControl = (ModifyImagesController)Main.changeScene(SceneType.newImages, this);
			_imageControl.setup(_videoName.getScene(),_data);
		}else {
			_imageControl.setMe();
		}

	}

	/**
	 * returns to search menu so user can perform another search
	 * @param event
	 */
	@FXML
	void handleNewSearch(ActionEvent event) {
		Main.initiateFileSystem();
		exit(SceneType.Search);
	}

	/**
	 * exits to main menu
	 * @param event
	 */
	@FXML
	void handleReturn(ActionEvent event) {
		Main.initiateFileSystem();
		Main.changeScene(SceneType.MainMenu, this);
	}


	/**
	 * used for loading pre existing templates
	 * @param data
	 */
	public void setup(TemplateData data) {
		_data = data;
		_videoName.setText(data.getName());
		_term = data.getTerm();
		if(data.isTemplate()) {
			_loaded=false;
			Future<?> doneAudio = data.load();
			try {
				//forces code to wait for task completion
				doneAudio.get();
				_loaded=true;
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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



	/*
	 * the following are all getters used to obtain information for video creation
	 */

	public String getName() {
		return _videoName.getText();
	}

	public String getTerm() {
		return _term;
	}

	public String getBoxText() {
		if(_audioControl==null) {
			return _data.getText();
		}
		return _audioControl.getText();
	}

	public String getBGM() {
		return _musicChoiceBox.getSelectionModel().getSelectedItem();
	}

	public List<String> fileOrder() {
		ObservableList<HBox> audioList;
		if(_audioControl==null) {
			return _data.getOrder();
		}else {
			audioList = _audioControl.getAudioList();
		}
		List<String> files = new ArrayList<String>();
		for(Node node : audioList) {
			files.add(((AudioBar) node).getName());	
		}
		return files;

	}

	public boolean usingImages() {
		return _imageSelection.isSelected();
	}

	public List<String> getAudioText() {
		if(_audioControl==null) {
			return _data.getAudioText();
		}
		List<String> audioText = new ArrayList<String>();
		for(Node node : _audioControl.getAudioList()) {
			audioText.add(((AudioBar) node).getText());
		}

		return audioText;
	}

	public List<String> getSelectedImages() {
		if(_imageControl==null) {
			return _data.getSelectedImages();
		}
		return _imageControl.getSelectedImages();
	}



}

