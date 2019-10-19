package application.scenebuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.RunBash;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;

/**
 * manages scene where user selects images to include in creation
 * @author student
 *
 */
public class SetImagesController implements Initializable{

	@FXML
	private GridPane _mainPane;

	@FXML
	private Button _resetButton;


	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _images = new ArrayList<String>();
	private ObservableList<ImageElement> _imageList;
	private CreateMenuController _parent;


	/**
	 * Initializes the grid pane and sets the nine images obtained from flickr
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		RunBash bash = new RunBash("ls ./resources/temp/images | cut -f1 -d'.'");
		_team.submit(bash);
		
		bash.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			
			@Override
			public void handle(WorkerStateEvent event) {

				try {
					_images = bash.get();		
				} catch (Exception e) {
					e.printStackTrace();
				}


				//gets the list of images obtained from flickr
				_imageList = FXCollections.observableArrayList();
				int i =0;
				for(String image:_images) {
					ImageElement displayImage = new ImageElement(image);
					_imageList.add(displayImage);
					_mainPane.add(displayImage, i%3, i/3);
					i++;	
				}
			}
		});
	}


	public void construct(CreateMenuController parent) {
		_parent=parent;
	}

	@FXML
	void selectAll(ActionEvent event) {
		selectAllNone(true);
	}

	@FXML
	void selectNone() {
		selectAllNone(false);
	}

	/**
	 * helper for selection buttons
	 * @param all
	 */
	public void selectAllNone(boolean all) {
		if(all) {
			for(ImageElement i:_imageList) {
				if(!i.isSelected()) {
					i.handleImageClicked();
				}
			}
		}else {
			for(ImageElement i:_imageList) {
				if(i.isSelected()) {
					i.handleImageClicked();
				}
			}
		}
	}

	/**
	 * hides window
	 * @param event
	 */
	@FXML
	public void handleDone(ActionEvent event) {
		_parent.popdownSetImages();
	}

	public void setSelectedImages(List<String> SelectedImages) {
		selectNone();
		for(String i : SelectedImages) {
			_imageList.get(Integer.parseInt(i)).setSelected(true);

		}
	}

/**
 * returns list of selected images
 * @return
 */
public List<ImageElement> getSelectedImages(){
	List<ImageElement> selected = new ArrayList<ImageElement>();

	for(ImageElement i:_imageList) {
		if(i.isSelected()) {
			selected.add(i);
		}
	}
	return selected;
}



}
