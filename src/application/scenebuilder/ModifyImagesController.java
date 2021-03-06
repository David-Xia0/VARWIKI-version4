package application.scenebuilder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.Main;
import application.RunBash;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

/**
 * images can be seelcted and deselected here.
 * These images are used for the slideshow in the final video
 * @author student
 *
 */
public class ModifyImagesController implements Initializable{

	@FXML
	private GridPane _mainPane;

	@FXML
	private Button _resetButton;
	private boolean _setup=false;

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private List<String> _images = new ArrayList<String>();
	private ObservableList<ImageElement> _imageList;
	private Scene _parent;
	private TemplateData _data;



	/**
	 * Initializes the grid pane and sets the nine images obtained from flickr
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		RunBash bash = new RunBash("ls ./resources/temp/images | cut -f1 -d'.'");
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
				while(!_setup) {
					int ii=0;
				}
				setSelectedImages(_data.getSelectedImages());

			}

			});
		_team.submit(bash);

	}


	/**
	 * initializes the seelction screen
	 * @param parent
	 * @param data
	 */
	public void setup(Scene parent, TemplateData data) {
		_parent=parent;
		_data = data;
		_setup=true;
	}

	@FXML
	void selectAll() {
		selectAllNone(true);
	}

	@FXML
	void selectNone() {
		selectAllNone(false);
	}

	/**
	 * Helper for selection buttons, checks if a image is selected or not and reverses the selection
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
	 * Hides the window and returns to previous scene
	 * @param event
	 */
	@FXML
	public void handleDone(ActionEvent event) {
		Main.getMainStage().setScene(_parent);
	}

	public void setSelectedImages(List<String> SelectedImages) {
		if (SelectedImages.size() == 9) {
			return;
		}
		System.out.println(SelectedImages);

		selectNone();
		System.out.println("itsnull: "+SelectedImages ==null);
		for(String i : SelectedImages) {
			_imageList.get(Integer.parseInt(i.substring(0, i.lastIndexOf(".jpg")))-1).setSelected(true);
		}

	}

	/**
	 * returns list of selected images
	 * @return
	 */
	public List<String> getSelectedImages(){
		List<String> selected = new ArrayList<String>();

		for(ImageElement i:_imageList) {
			if(i.isSelected()) {
				selected.add(i.toString());
			}
		}
		return selected;
	}


	/**
	 * sets this as the current scene
	 */
	public void setMe() {
		Main.getMainStage().setScene(_mainPane.getScene());
		
	}
}

