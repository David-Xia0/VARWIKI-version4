package application.scenebuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.GetFlickr;
import application.Main;
import application.RunBash;
import application.Main.SceneType;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * This scene Requests the user for a input search term.
 * The search term is then processed all data from wikipedia and flickr is obtained according to the term
 *
 */
public class SearchController {

	private ExecutorService _team = Executors.newSingleThreadExecutor();
	
	@FXML
	private Button _searchButton,_backButton;
	
	@FXML
	private TextField _term;
	
	@FXML
	private ProgressIndicator _searching;
	
	
	/**
	 * If the user decides to he can return to the main menu screen
	 */
	@FXML
	void back(){
		exit(SceneType.MainMenu);
	}

	/**
	 * If the search button is presesd, and there is a term entered. We check if the term is a "good" term and then obtain other data.
	 * @param event
	 */
	@FXML
	void search(ActionEvent event) {
		String term = _term.getCharacters().toString();

		if(term.isEmpty()) {
			Main.error("please enter a term");
			return;
		}

		_searchButton.setVisible(false);
		_searching.setVisible(true);

		//wiki search bash command is used to obtain wikipedia information about the term
		RunBash command = new RunBash("wikit "+ term);
		_team.submit(command);
		
		command.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			
			@Override
			public void handle(WorkerStateEvent event) {
				String text;


				try {
					text = command.get().get(0);
					//checks if search was successful or not, and returns error
					if(text.contentEquals(term + " not found :^(" )) {
						Main.error("search term not found");
						exit(SceneType.Search);
						return;
					}

					//gets flickr images using search term
					GetFlickr imageDown = new GetFlickr(term, 9);
					_team.submit(imageDown);
					imageDown.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {
							CreateMenuController controller = (CreateMenuController)exit(SceneType.CreateMenu,false);
							controller.setup(text,term);
						}
					});
					
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * change screen, and clear temp folder by default
	 * @param location
	 * @return
	 */
	private Object exit(SceneType location) {
		return exit(location, true);
	}	
	
	/**
	 * change screen, but we cna determine if we want to clear the temp folder or not
	 * @param location
	 * @param destroy
	 * @return
	 */
	private Object exit(SceneType location,boolean destroy) {
		if(destroy) {
			Main.initiateFileSystem();
		}
		return Main.changeScene(location, this);
	}

}

