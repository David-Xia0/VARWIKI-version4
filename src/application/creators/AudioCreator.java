package application.creators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AudioBar;
import application.RunBash;
import application.scenebuilder.TemplateData;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * this class is used to create audio files selcted by the user
 * @author student
 *
 */
public class AudioCreator extends Task<Boolean>{

	private static final int MAXLENGTH =20;
	private boolean _cancelOperation = false;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private String _text;
	private int _audioChunksCount;
	private String _voicePackage;
	private boolean _createFinished=false;
	private int _audioCatCount;

	
	public AudioCreator(String text, String voicePackage, int current) {
		_text = text;
		_voicePackage = voicePackage;
		_audioChunksCount = current;
	}

	/**
	 * call method should return true if the audio files are succesffully created
	 */
	@Override
	protected Boolean call() throws Exception {
		return createAudio();
	}

	/**
	 * This breaks down highlighted text into smaller chunks and calls save audio on them (this is the function that makes audio)
	 * Chunks need to be broken down to preserve audio quallity whne doing tts 
	 * @return
	 */
	public boolean createAudio() {
		_team.submit(new RunBash("mkdir ./resources/temp/tmpaudio"));
		
		String[] wordCount = _text.split("\\s+");


		List<RunBash>commandList = new ArrayList<RunBash>();
		List<String>audioList = new ArrayList<String>();

		/*
		 * determines suitable length for audio chunks
		 */
		for(int i = 0; i<wordCount.length/MAXLENGTH + 1; i++) {
			String audio = "";
			for (int j=0; j<MAXLENGTH; j++) {
				int index = MAXLENGTH*i + j;
				if(index<wordCount.length) {
					audio = audio + wordCount[index] + " ";
				}
			}
			_audioCatCount=i;
			audioList.add(audio);
			commandList.add(saveAudio(audio));
		} 
		
		
		
		RunBash lastCommand = commandList.get(commandList.size()-1);
		lastCommand.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				if(_cancelOperation) {
					return;
				}
				
			
				//if there is a problema nd the audio cannot be converted this error will be displayed
				if(lastCommand.returnError() != null &&lastCommand.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
					error("some words selected cannot be converted by selected voice package");
					_cancelOperation =true;
					_createFinished=true;
					return;
				}


				String audioFileNames="";
				for(int i =0; i<audioList.size(); i++) {
					audioFileNames = audioFileNames+ "./resources/temp/tmpaudio/" + i +".wav ";	
				}	

				//all audio chunks are merged to create one audio file
				RunBash mergeAudio = new RunBash("sox "+ audioFileNames + "./resources/temp/audio/" + _audioChunksCount + ".wav");
				_team.submit(mergeAudio);	
				mergeAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

					@Override
					public void handle(WorkerStateEvent arg0) {	
						_createFinished=true;
					}
					
				});
				
				_team.submit(new RunBash("rm -rf ./resources/temp/tmpaudio"));
			}
		});
		
		//all audio chunks are queued for creation
		for(int i =0; i<commandList.size(); i++) {
			_team.submit(commandList.get(i));
		}
		
		//method only completes when all creations are finished
		while(!_createFinished) {
			if(_team.isTerminated()) {
				_createFinished=true;
			}
		}
		
		return (!_cancelOperation);
	}



	/**
	 * this method calls the bash commands on the selected Text
	 * @param selectedText
	 * @return
	 */
	private RunBash saveAudio(String selectedText){
		RunBash audioCreation;
		if( _voicePackage ==null || _voicePackage.contentEquals("Default") ) {
			audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./resources/temp/tmpaudio/"+ _audioCatCount + ".wav");
		}else {
			audioCreation = new RunBash("echo \"" + selectedText + "\" | text2wave -o ./resources/temp/tmpaudio/"+ _audioCatCount + ".wav " + "-eval \""+_voicePackage+"\"");
			audioCreation.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					if(audioCreation.returnError() != null && audioCreation.returnError().substring(0, 10).contentEquals("SIOD ERROR")) {
						error("some words selected cannot be converted by selcted voice package");
						_createFinished=true;
						_cancelOperation=true;
					}
				
				}
			});
		}
		return audioCreation;
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
}
