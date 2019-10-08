package application.scenebuilder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.GetFlickr;
import application.Main;
import application.RunBash;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

/**
 * handles search scene
 * @author student
 *
 */
public class SearchController {

	@FXML
	private Button _searchButton,_backButton;
	private ExecutorService _team = Executors.newSingleThreadExecutor();
	private boolean _runningThread;
	@FXML
	private TextField _term;
	@FXML
	private ProgressIndicator _searching;
	
	
	/**
	 * returns to menu
	 */
	@FXML
	void back(){
		Main.changeScene("MainMenu.fxml", this);
	}

	/**
	 * If there is an entered term, the term is searched on wikipedia
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

		//wiki search bash command is created and run on another thread
		RunBash command = new RunBash("wikit "+ term);
		_team.submit(command);
		_runningThread = true;
		command.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				String text;


				try {
					text = command.get().get(0);
					//checks if search was successful or not
					if(text.contentEquals(term + " not found :^(" )) {
						Main.error("search term not found");
						Main.changeScene("Search.fxml", this);
						return;
					}


					GetFlickr imageDown = new GetFlickr(term, 9);
					_team.submit(imageDown);
					imageDown.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
						@Override
						public void handle(WorkerStateEvent event) {

							CreateMenuController controller = (CreateMenuController)Main.changeScene("CreateMenu.fxml", this,false);
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
}

