package application.scenebuilder;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Slider;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import jdk.jfr.EventType;
import application.*;

/**
 * manages main menu
 * @author student
 *
 */
public class MainMenuController implements Initializable{


	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _creations = new ArrayList<String>();
	private enum State{
		EMPTY,
		PLAYING,
		PAUSED,
		FINISHED
	};
	private MainMenuController _this;

	private ObservableList<HBox> _videoList = FXCollections.observableArrayList();

	private State _state = State.EMPTY;
	@FXML
	private Button createButton;

	@FXML 
	private Slider _slider;

	@FXML
	private VBox videoBox;

	@FXML
	private Text _videoTime;

	@FXML
	private MediaView _player;

	@FXML
	private Button backwardButton;

	@FXML
	private Button _multiButton;

	@FXML
	private Button forwardButton;
	
	@FXML
	private Button _quizButton;

	@FXML
	private Button muteButton;

	@FXML
	private HBox _hbox;

	@FXML
	private ListView<HBox> videoListView;
	private boolean _muted = false;

	private MediaBox player_;
	private boolean _actionsSet;


	/**
	 * sets new media to video player
	 */
	private void setNewMedia() {
		HBox currentSelection = (HBox) videoListView.getSelectionModel().getSelectedItem();
		//setup(currentSelection);	
		if( currentSelection!=null){
			Text asText = (Text)currentSelection.getChildren().get(0);

			URL mediaUrl;
			try {
				mediaUrl = new File(Main.getPathToResources() + "/VideoCreations/"+asText.getText()+".mp4").toURI().toURL();
				Media newMedia = new Media(mediaUrl.toExternalForm());
				player_.setMedia(newMedia);
			} catch (Exception e) {
				//e.printStackTrace();
			}
			if(_actionsSet) {
			player_.SetOnForwardAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					playNext();
				}
			});
			player_.SetOnBackwardAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					if(player_.getTimeMillis()>=player_.getTotalDuration()/5) {
						player_.setTime(new Duration(0));
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
	 * eventhandler for button
	 */
	@FXML
	void handleSelectionChange() {
		setNewMedia();
	}


	/**
	 * changes scene to search
	 */
	@FXML
	void handleCreate() {

		player_.pause();
		Main.changeScene("Search.fxml", this);
	}

	/**
	 * universal delete button
	 * @param event
	 */
	@FXML
	void handleDeleteVideo(ActionEvent event) {
		HBox selectedItem = videoListView.getSelectionModel().getSelectedItem();
		if(selectedItem instanceof VideoBar) {
			((VideoBar) selectedItem).delete();
		}
	}
	
	@FXML
	void handleQuiz(ActionEvent event) {
		
	}



	/**
	 * adds list view and media player to scene
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		_state = State.EMPTY;
		_this = this;

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

				if(_creations.get(0).isEmpty()) {

					Text noCreations = new Text("No Current Creations");

					_videoList.add(new HBox(noCreations));
					videoListView.setItems(_videoList);

				}else {

					for(String video:_creations) {
						new VideoBar(video,_videoList);
					}
					videoListView.setItems(_videoList);
				}
				//This adds the videoPlayer to the scene
				player_ = new MediaBox();
				//This controls how forward/backward buttons work
				


				_hbox.getChildren().add(player_);	
				if (videoListView.getSelectionModel().isEmpty()){
					videoListView.getSelectionModel().clearAndSelect(0);
					setNewMedia();
				}

				}
			});
		}

		public void playPrev() {
			videoListView.getSelectionModel().selectPrevious();
			setNewMedia();
		}
		public void playNext() {

			videoListView.getSelectionModel().selectNext();
			setNewMedia();
		}


	}
