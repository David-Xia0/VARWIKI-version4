package application.creators;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.AudioBar;
import application.RunBash;
import application.scenebuilder.CreateMenuController;
import application.scenebuilder.TemplateData;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AudioCreator extends Task<Boolean>{

	private static final int MAXLENGTH =20;
	private boolean _cancelOperation = false;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private String _text;
	private int _audioChunksCount;
	private String _voicePackage;
	private boolean _createFinished=false;
	private int _audioCatCount;

	public AudioCreator(CreateMenuController data) {
		_text = data.getSelectedText();
		_audioChunksCount = data.getAudioText().size();
		_voicePackage = data.getVoicePackage();
	}
	
	public AudioCreator(String text, String voicePackage, int current) {
		_text = text;
		_voicePackage = voicePackage;
		_audioChunksCount = current;
	}

	@Override
	protected Boolean call() throws Exception {
		return createAudio();
	}

	
	public boolean createAudio() {

		_team.submit(new RunBash("mkdir ./resources/temp/tmpaudio"));
		
		String[] wordCount = _text.split("\\s+");


		List<RunBash>commandList = new ArrayList<RunBash>();
		List<String>audioList = new ArrayList<String>();

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
				System.out.println("audioGenerated");
				if(_cancelOperation) {
					return;
				}
				
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
				System.out.println(audioFileNames);
				System.out.println(_audioChunksCount);
				
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
		
		System.out.println("numcmds: "+  commandList.size());
		
		for(int i =0; i<commandList.size(); i++) {
			_team.submit(commandList.get(i));
			System.out.println("command: " + i);
		}
		
		while(!_createFinished) {
			if(_team.isTerminated()) {
				_createFinished=true;
			}
		}
		
		return (!_cancelOperation);
	}



	private RunBash saveAudio(String selectedText){
		RunBash audioCreation;
		System.out.println(selectedText);
		System.out.println(_voicePackage);
		

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
