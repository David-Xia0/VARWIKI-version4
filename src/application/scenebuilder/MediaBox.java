package application.scenebuilder;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 * Packaged mediaPlayer class. The media player has embbeded pause play functionality.
 * 
 *
 */
public class MediaBox extends AnchorPane{

	@FXML
	private Button createButton;

	@FXML 
	private Slider _slider;

	@FXML
	private VBox videoBox;

	@FXML
	private Text _videoTime;

	@FXML
	private MediaView _view;

	private MediaPlayer _player;

	@FXML
	private Button _backwardButton;

	@FXML
	private Polygon _playIndicator;

	@FXML
	private Button _forwardButton;

	@FXML
	private Button muteButton;

	@FXML
	private ListView<HBox> videoListView;
	
	
	private boolean _muted = false;
	private boolean _playing;
	private boolean _Setup;
	private List<EventHandler<ActionEvent>> _actionsToSet;
	private List<Button> _on;


	/**
	 * Constructor generates default settings for the Media player
	 */
	public MediaBox()  {
		_Setup =false;
		_actionsToSet  = new ArrayList<EventHandler<ActionEvent>>();
		_on = new ArrayList<Button>();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MediaBox.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);
		load(fxmlLoader);
		try { 
			fxmlLoader.load();
		} catch (IOException exception) { 
			throw new RuntimeException(exception); 
		} 
	}
	
	@FXML 
	public void initialize() {
	}
	
	private void load(FXMLLoader fxmlLoader) {
	}


	/**
	 * Sets a video into the Media Player and loads default settings and conditions
	 * @param newMedia
	 */
	public void setMedia(Media newMedia) {

		//Create the player and set to play.
		if(null!=_player) {
			_player.stop();
			_player.dispose();
		}
		
		_player = new MediaPlayer(newMedia);
		_view.setMediaPlayer(_player);
		
		_player.setOnReady(new Runnable() {
			
			@Override
			public void run() {
				if(!_Setup) {
					setupSlider();
					_Setup=true;
				}
				syncSlider();
			}
			
		});
		
		//sets default settings for media player
		_player.setAutoPlay(false);
		_player.setMute(_muted);
		_player.seek(new Duration(0));

		_playing=false;
		_playIndicator.setVisible(true);

		//when media player finishs 
		_player.setOnEndOfMedia(new Runnable() {
			public void run() {
				_playing=false;
				_player.stop();
				_playIndicator.setVisible(true);

			}
		});
		
		//This is the code for the custom slider that takes the time property of the media player.
		_player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
			@Override
			public void changed(ObservableValue<? extends Duration> observable, Duration oldValue,
					Duration newValue) {				
				_slider.setValue(newValue.toMillis());
			}
		});		
	}


	/**
	 * Allows other classes to set the functionailty for the foward button
	 */
	public void SetOnForwardAction(EventHandler<ActionEvent> arg0) {
		if(!_Setup) {
			_actionsToSet.add(arg0);
			_on.add(_forwardButton);
		}else {
			_forwardButton.setOnAction(arg0);
		}
	}

	/**
	 * Allows other classes to set the functionailty for the backward button
	 * @param arg0
	 */
	public void SetOnBackwardAction(EventHandler<ActionEvent> arg0) {
		if(!_Setup) {
			_actionsToSet.add(arg0);
			_on.add(_backwardButton);
		}else {
			_backwardButton.setOnAction(arg0);
		}
	}
	
	/**
	 * get the milli second time on the media player
	 * @return
	 */
	public double getTimeMillis() {
		return _player.getCurrentTime().toMillis();
	}

	/**
	 * get the total duration of the video
	 * @return
	 */
	public double getTotalDuration() {
		return _slider.getMax();
	}

	/**
	 * sets the current time position in the video
	 * @param newTime
	 */
	public void setTime(Duration newTime) {
		_player.seek(newTime);
	}





	/**
	 * Action button that is used to load to the start of current video/go to previous video
	 * @param event
	 */
	@FXML
	void handleBackward(ActionEvent event) {
		_player.seek(new Duration(0));
	}

	/**
	 * Action button that is used to load to the end of current video/go to next video
	 * @param event
	 */
	@FXML 
	void handleForward(ActionEvent event) {
		_player.seek(_player.getCurrentTime().add(new Duration(3000)));
	}

	/**
	 * If mute button is presses, this method toggles the sound on and off on the media Player
	 * @param event
	 */
	@FXML
	void handleMute(ActionEvent event) {
		if(_player !=null) {
			if(!_muted) {
				muteButton.setText("Done");
			}else {
				muteButton.setText("Mute");
			}
			_muted= !_muted;
			_player.setMute(_muted);
		}
	}




	/**
	 * If the video is pressed, this methods handles the pause and play functionality
	 */
	@FXML
	void handleVideoMultiButton() {		
		if(_player !=null) {
			if(_playing) {
				pause();
			}else {
				play();
			}
		}
	}

	/**
	 * Pauses the current media player
	 */
	public void pause() {
		if(_player !=null) {
			_playIndicator.setVisible(true);
			_player.pause();
			_playing =false;
		}

	}

	/**
	 * Resumes play for the current media player
	 */
	private void play() {
		if(_player !=null) {
			_player.play();
			_playIndicator.setVisible(false);
			_playing = true;
		}
	}


	/**
	 * checks if player was playing before drag event on slider, if so, plays
	 */
	private void checkPlay() {
		if (_player != null && _playing) {
			_player.play();
		}
	}


	/**
	 * syncs slider to new mediaplayer on change of media. Max is set to new duration
	 */
	private void syncSlider() {
		_slider.setMax(_player.getTotalDuration().toMillis());
		_slider.setValue(0);
		_videoTime.setText("00:00");
	}


	/**
	 * This method sets up the functionaliy and binds properties of the slider to the Media plauer
	 */
	private void setupSlider() {
		Pane thumb = (Pane) _slider.lookup(".thumb");

		thumb.setOnDragDetected(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				_player.pause();
			}
		});	
		
		thumb.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				_player.seek(Duration.millis(_slider.getValue()));
				checkPlay();
			}
		});
		
		_slider.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				_player.seek(Duration.millis(_slider.getValue()));
				checkPlay();
			}
		});
		
		_slider.valueProperty().addListener(new ChangeListener<Number>(){
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				String time = "";
				Double newValue = (Double)arg1;
				time += String.format("%02d", ((int)newValue.doubleValue()/60000)%60);
				time += ":";
				time += String.format("%02d", ((int)newValue.doubleValue()/1000)%60);
				_videoTime.setText(time);
			}
		});
	}
}
