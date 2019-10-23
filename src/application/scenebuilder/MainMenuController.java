package application.scenebuilder;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.text.Text;
import javafx.util.Duration;
import application.*;
import application.Main.SceneType;

/**
 * 
 * controller for the Main menu Scene.
 * This is the users first interactive screen where previous creations can be deleted and played back.
 * Can also access create new creations page and Go to matching quiz game
 *
 */
public class MainMenuController implements Initializable{


	private static boolean _lockStatus;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private MediaBox _playerBox;
	private boolean _actionsSet;
	private ObservableList<HBox> _videoList = FXCollections.observableArrayList();

	@FXML
    private Button _deleteButton;

    @FXML
    private Button _createButton;

    @FXML
    private HBox _hbox;

    @FXML
    private ListView<HBox> videoListView;

    @FXML
    private Button _quizButton;

    @FXML
    private Button _modifyButton;

    @FXML
    private ToggleButton _lockButton;



	/**
	 * Sets the selected video into our Media player
	 * Previews the first frame of the video and generates button functionality
	 */
	private void setNewMedia() {
		HBox currentSelection = (HBox) videoListView.getSelectionModel().getSelectedItem();

		//Checks if selection value is null or not, selection value can be null when there are no videos
		if( currentSelection!=null){
			Text asText = (Text)currentSelection.getChildren().get(0);

			URL mediaUrl;
			try {
				mediaUrl = new File(Main.getPathToResources() + "/VideoCreations/"+asText.getText()+".mp4").toURI().toURL();
				Media newMedia = new Media(mediaUrl.toExternalForm());
				_playerBox.setMedia(newMedia);
			} catch (Exception e) {
				//e.printStackTrace();
			}

			//sets button funcitonality in Video Player
			if(_actionsSet) {
				_playerBox.SetOnForwardAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						playNext();
					}
				});
				_playerBox.SetOnBackwardAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						if(_playerBox.getTimeMillis()>=_playerBox.getTotalDuration()/5) {
							_playerBox.setTime(new Duration(0));
						} else {
							playPrev();
						}
					}
				});
				_actionsSet=true;
			}
		}
	}

	
	/**
	 * Modify Existing Video Creation
	 * Goes to create Menu and loads existing template from video
	 */
	@FXML
	public void testSerial() {

		HBox currentSelection = (HBox) videoListView.getSelectionModel().getSelectedItem();
		Text asText = (Text)currentSelection.getChildren().get(0);
		String data = Main.getPathToResources() + "/templates/" +  asText.getText() + "/info.class";
		try {
			FileInputStream fileIn = new FileInputStream(data);
			ObjectInputStream object = new ObjectInputStream(fileIn);
			TemplateData template = (TemplateData) object.readObject();
			CreateMenuController controller = (CreateMenuController) exit(SceneType.CreateMenu);
			controller.setup(template);
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

	}

	/**
	 * Everytime the user selects a new creation, the Media player should preview the new selection
	 */
	@FXML
	void handleSelectionChange() {
		setNewMedia();
	}


	/**
	 * The scene changes to Creation scene
	 */
	@FXML
	void handleCreate() {
		_playerBox.pause();
		exit(SceneType.Search);
	}

	/**
	 * universal delete button on press action.
	 * Sends the delete message to the currently selected Video.
	 * Message will later ask user for further confirmation
	 */
	@FXML
	void handleDeleteVideo(ActionEvent event) {
		HBox selectedItem = videoListView.getSelectionModel().getSelectedItem();
		if(selectedItem instanceof VideoBar) {
			((VideoBar) selectedItem).delete();
		}
	}

	/**
	 * If the user has more than 3 creations in his library then they will be allow to access this scene.
	 * Scene is changed to the matching quiz game scene
	 */
	@FXML
	void handleQuiz(ActionEvent event) {
		if(_creations.size()>2) {
			exit(SceneType.QuizMenu);
		}else {
			error("PLease at least have 3 creations");
		}
	}
	
	/**
	 * this toggle button action switches between child mode and adult mode
	 * in child mode, Video delete,create and modify functionality is disabled
	 * @param event
	 */
	@FXML
	public void handleLockScreen(ActionEvent event) {
		_lockStatus=!_lockStatus;
		setLockButtons();
	}
	
	/**
	 * Sets the status of The buttons depending on the current lock status
	 * If lock is inplace, create delete modify buttons are greyed out
	 */
	public void setLockButtons() {
		_createButton.setDisable(_lockStatus);			
		_modifyButton.setDisable(_lockStatus);
		_deleteButton.setDisable(_lockStatus);
		if(_lockStatus) {
			_lockButton.setText("Unlock");
			_createButton.setOpacity(0.25);
			_modifyButton.setOpacity(0.25);
			_deleteButton.setOpacity(0.25);
		}else {
			_lockButton.setText("Lock");
			_createButton.setOpacity(1);
			_modifyButton.setOpacity(1);
			_deleteButton.setOpacity(1);
		}
	}



	/**
	 * This initializes the list view to show all the videos in library
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		/*
		 * this loads the current stored videos and loads the video player with the first stored video.
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

				//Message for user if there are no creations in library
				if(_creations.get(0).isEmpty()) {
					Text noCreations = new Text("No Current Creations");
					_videoList.add(new HBox(noCreations));
					videoListView.setItems(_videoList);

				//Sets list view to contain all library video names
				}else {
					for(String video:_creations) {
						new VideoBar(video,_videoList);
					}
					videoListView.setItems(_videoList);
				}

				//This adds the videoPlayer to the anchor pane
				_playerBox = new MediaBox();
				_hbox.getChildren().add(_playerBox);	
				if (videoListView.getSelectionModel().isEmpty()){
					videoListView.getSelectionModel().clearAndSelect(0);
					setNewMedia();
				}
			}

		});
		
		// checks if child lock is on and then disables create and modify buttons
		setLockButtons();
	}

	
	/**
	 * sets the video before the current selection into the Media Player
	 */
	public void playPrev() {
		videoListView.getSelectionModel().selectPrevious();
		setNewMedia();
	}
	
	/**
	 * sets the video after the current selection into the media Player
	 */
	public void playNext() {
		videoListView.getSelectionModel().selectNext();
		setNewMedia();
	}

	/**
	 * helper method that creates an error message popup that contains pararmeter input msg
	 */
	public void error(String msg) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("ERROR "+msg);
		alert.setHeaderText("ERROR");
		alert.setContentText(msg);
		alert.showAndWait();
	}

	/**
	 * helper method that changes scenes when user input is detected
	 */
	private Object exit(SceneType location) {
		return Main.changeScene(location, this);
	}

}

