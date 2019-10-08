package application.scenebuilder;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AudioBar;
import application.RunBash;
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
public class CreateMenuController implements Initializable {

	private ObservableList<HBox> _audioList = FXCollections.observableArrayList();
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private boolean _runningThread;
	private int audioCount=0;
	private SetImagesController _controller;
	private String __videoName;

	@FXML
	private Button _playButton;

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
	private CheckBox _imageSelection;

	@FXML
	private Button _testButton;

	@FXML
	private Button _saveButton;

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

	private String _term;
	private Stage _stage;
	private boolean _saving=false;
	private boolean _defaultImages=true;

	
	/**
	 * this initialises choice box to allow for the selection of different festival voices
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		ObservableList<String> voices = FXCollections.observableArrayList();
		voices.addAll("Default","(voice_akl_nz_cw_cg_cg)","(voice_akl_nz_jdt_diphone)");
		_festivalVoice.setItems(voices);
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
		}else if(_audioList.isEmpty()){
			boolean confirmed = noText();
			if (!confirmed){
			return;
			}
			saveAllAudio();
		}
		_loading.setVisible(true);
		_createButton.setVisible(false);
		while(_saving) {
			int a =0;
		}
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
					if(f.getExitStatus()== 0) {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("ERROR ");
						alert.setHeaderText("File already exists");
						alert.setContentText("would you like to overwrite?");
						Optional<ButtonType> result = alert.showAndWait();

						if(result.get() != ButtonType.OK) {
							_loading.setVisible(false);
							_createButton.setVisible(true);
							return;
						}else {
							RunBash remove = new RunBash("rm ./resources/VideoCreations/"+__videoName+".mp4");
							_team.submit(remove);
							createVideo();
						}
					}else {
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
		List<String> images = getSelectedImages();
		String name = _videoName.getText();
		String audioFileNames="";
		
		for(Node audio:_audioList) {
			audioFileNames = audioFileNames+audio.toString()+".wav ";
		}		

		RunBash mergeAudio = new RunBash("sox "+ audioFileNames +" ./resources/temp/output.wav");
		_team.submit(mergeAudio);	
		mergeAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				RunBash audioLengthSoxi = new RunBash("soxi -D ./resources/temp/output.wav");
				_team.submit(audioLengthSoxi);
				_runningThread = true;
				audioLengthSoxi.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						_runningThread=false;
						double audioLength;

						try {
							audioLength = Double.parseDouble(audioLengthSoxi.get().get(0));
							RunBash createVideoAudio = new RunBash("ffmpeg -i ./resources/temp/output.wav -vn -ar 44100 -ac 2 -b:a 192k ./resources/temp/output.mp3 &> /dev/null "
									+ "; ffmpeg -f lavfi -i color=c=blue:s=320x240:d="+audioLength 
									+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
									+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+_term+"\" ./resources/temp/"+name+"noImage.mp4 &> /dev/null ;");

							_team.submit(createVideoAudio);
							RunBash createVideo2;
							if(!_imageSelection.isSelected()) {
								createVideo2 = new RunBash("ffmpeg -i ./resources/temp/"+name +"noImage.mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ "./resources/VideoCreations/"+name+".mp4  &> /dev/null");
							} else {
								markImages(images);
								textFileBuilder(images,audioLength);
								videoMaker();
								createVideo2 = new RunBash("ffmpeg -i ./resources/temp/"+name +".mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ "./resources/VideoCreations/"+name+".mp4  &> /dev/null");

							}
							_team.submit(createVideo2);
							_runningThread = true;
							createVideo2.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
								@Override
								public void handle(WorkerStateEvent event) {
									_runningThread=false;
									Main.changeScene("MainMenu.fxml", this);
								}
							});
						} catch (NumberFormatException | InterruptedException | ExecutionException e) {
							error("Video Creation Failed");
							_runningThread=false;
							Main.changeScene("MainMenu.fxml", this);
						}
					}
				});

			}
		});
	}


	/**
	 * this method marks the images with the topic text
	 * @param images
	 */
	private void markImages(List<String> images) {
		for (String path: images) {
		
			RunBash mark= new RunBash("ffmpeg -i ./resources/temp/images/" + path + " -vf \"drawtext=text='"+ _term + "':fontcolor=white:fontsize=75:x=(w-text_w)/2: y=(h-text_h-line_h)/2:\" ./resources/temp/" + path);
			_team.submit(mark);
		}
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
			Main.changeScene("MainMenu.fxml", this);
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
			Main.changeScene("Search.fxml", this);
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
	private void initializeSetImages() {
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
		_stage.hide();;
	}
	
	private void popupSetImages() {
		_stage.show();
	}

	/**
	 * Saves highlighted text as audio file
	 * 
	 */
	@FXML
	void handleSaveAudio() {
		
		_saving = true;
		String selectedText = _displayTextArea.getSelectedText();
		String[] wordCount = selectedText.split("\\s+");

		if(selectedText.isEmpty()) {
			_saving=false;
			return;
		}else if(wordCount.length>40) {
			for(int i =0; i<wordCount.length/40 + 1; i++) {
				String audio = "";
				for (int j=0; j<40; j++) {
					int index = 40*i + j;
					if(index<wordCount.length) {
						audio = audio + wordCount[index] + " ";
					}
				}
				saveAudio(audio);
				
			}
			_saving=false;
			return;
		}
		saveAudio(selectedText);
		_saving=false;
	}
	
	private void saveAudio(String selectedText){
		audioCount++;
		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		int audiocount = audioCount;
		if( voice ==null || voice.contentEquals("Default") ) {
			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./resources/temp/"+ audioCount + ".wav");

			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
					new AudioBar(selectedText,audiocount+"",_audioList);
					_audioBox.setItems(_audioList);
				}
			});
		}else {
			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./resources/temp/"+ audioCount + ".wav " + "-eval \""+_festivalVoice.getSelectionModel().getSelectedItem()+"\"");
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
					new AudioBar(selectedText,audiocount+"",_audioList);
					_audioBox.setItems(_audioList);

				}
			});
		}
	}

	/**
	 * plays a preview of the selected audio text
	 * @param event
	 */
	@FXML
	void handleTestAudio(ActionEvent event) {
		String selectedText = _displayTextArea.getSelectedText();
		if(selectedText.isEmpty() || _runningThread) {
			return;
		}
		String voice = _festivalVoice.getSelectionModel().getSelectedItem();
		if(voice == null ||voice.contentEquals("Default")) {

			RunBash audioCreation = new RunBash("echo \"" + selectedText + "\" | festival --tts");
			_team.submit(audioCreation);
			_runningThread = true;
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					_runningThread=false;
				}
			});

		}else {
			RunBash audioCreation = new RunBash("echo {\""+voice+"\",'(SayText \""+selectedText+"\")'} | bash -c festival");
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
	 * The following five methods are used to edit play existing creations
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


	/**
	 * 
	 * @return currently selected images (default is all images selected)
	 */
	private List<String> getSelectedImages() {
		List<String> images = new ArrayList<String>();
		List<ImageElement> elements = _controller.getSelectedImages();

		for(ImageElement name: elements) {
			images.add(name.toString());
		}
		return images;
	}

	/**
	 * builds text file used for ffmpeg command to create slideshow video
	 * @param images
	 * @param totalDuration
	 */
	private void textFileBuilder(List<String> images, double totalDuration) {
		double duration = totalDuration/images.size();
		String stringDuration = Double.toString(duration);
		String text = ""; 	
		String lastImage="";
		for(String name:images) {
			text= text +"file '" + name +"'\nduration " + stringDuration + "\n";
			lastImage=name;
		}
		text=text+"file '"+lastImage+"'";

		RunBash createFile = new RunBash("touch ./resources/temp/cmd.txt ; echo -e \""+text+ "\" > ./resources/temp/cmd.txt");
		_team.submit(createFile);



	}
	
	/**
	 * creates slideshow from stored and selected images
	 */
	private void videoMaker() {
		RunBash makeVideo = new RunBash("ffmpeg -f concat -safe 0 -i ./resources/temp/cmd.txt -r 25 -pix_fmt yuv420p -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2'  ./resources/temp/"+ __videoName +".mp4");
		_team.submit(makeVideo);
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
	 * passes Search info to scene
	 * @param text
	 * @param term
	 */
	public void setup(String text, String term) {
		_displayTextArea.setText(text);
		_term = term;
		_festivalVoice.getSelectionModel().clearAndSelect(0);
		_videoName.setText(_term);
		_runningThread = false;
		initializeSetImages();
	}
}