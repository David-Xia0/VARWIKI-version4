package application.creators;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.RunBash;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;


/**
 * this calls merges video with background music
 * @author student
 *
 */
public class BGM {

	private String _bgm;
	private ExecutorService _team = Executors.newSingleThreadExecutor(); 
	private RunBash _concateBGM;
	
	public BGM(String mp3name){
		_bgm=mp3name;
	}
	
	/**
	 * this method uses the mp3name field and merges it with the video mp4 file
	 * @param file - takes in input of the video file name
	 * @param length - takes in the length of the video
	 */
	public void mergeBGM(String file, double length) {
		
	
			if(_bgm == null || _bgm.contentEquals("No Music")) {
				return;
			}
		
		//truncates BGM to length of video file
		RunBash truncateBGM = new RunBash("ffmpeg -i ./resources/"+_bgm+" -t "+length+" -acodec copy ./resources/temp/BGM.mp3");
		System.out.println(length+file);
		//concates BGM to the video file. 
		_concateBGM = new RunBash("ffmpeg  -i ./resources/VideoCreations/"+file+".mp4 -i ./resources/temp/BGM.mp3 -filter_complex \"amix=inputs=2\" "
				+ "-map 0:0 -c:a aac -strict -2 -c:v copy ./resources/VideoCreations/"+file+"-.mp4"
						+ "; cp ./resources/VideoCreations/"+file+"-.mp4 ./resources/VideoCreations/"+file+".mp4 ; rm ./resources/VideoCreations/"+file+"-.mp4 ; ");
		
		_team.submit(truncateBGM);
		_team.submit(_concateBGM);
		while(!_concateBGM.isDone()) {
		}
	}
	
	public boolean done() {
		return _concateBGM.isCancelled();
	}
}
