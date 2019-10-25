package application.creators;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.RunBash;
import application.scenebuilder.CreateMenuController;
import application.scenebuilder.TemplateData;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public class VideoCreator extends Task<Void>{

	private ExecutorService _team = Executors.newSingleThreadExecutor(); 

	private final String name;
	private final String term;
	private final String _music;
	private boolean usingImages;
	private boolean markImages;
	private boolean createFinished;
	private final List<String> audioText;
	private final List<String> _files;
	private final List<String> _selectedImages;
	private final String resourceLocation;
	private final String outputLocation;
	private RunBash createVideo;
	
	public VideoCreator(CreateMenuController videoData) {
		name = videoData.getName();
		term = videoData.getTerm();
		_music = videoData.getBGM();
		usingImages = videoData.usingImages();
		audioText = videoData.getAudioText();
		_files = videoData.fileOrder();
		_selectedImages = new ArrayList<String>();
		List<String> selectedImages = videoData.getSelectedImages();
		for(String image: selectedImages) {
			_selectedImages.add(image.substring(0, image.lastIndexOf(".")));
		}
		resourceLocation = "/temp";
		outputLocation = "./resources/VideoCreations/"+name+".mp4";
	}

	/**
	 * this constructor is mainly designed for the matching game to use
	 */
	public VideoCreator(TemplateData videoData) {
		name = videoData.getName();
		term = " ";
		_music = null;
		usingImages = videoData.usingImages();
		audioText = videoData.getAudioText();
		_files = videoData.getOrder();
		_selectedImages = videoData.getSelectedImages();
		resourceLocation = "/templates/"+name;
		outputLocation = "./resources/temp/matchingVideo.mp4";

	}


	/**
	 * creates slideshow from stored and selected images
	 */
	private void videoMaker() {
		RunBash makeVideo = new RunBash("ffmpeg -f concat -safe 0 -i ./resources/temp/cmd.txt -r 25 -pix_fmt yuv420p -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' "
				+ " ./resources/temp/"+ name+".mp4 ");
		_team.submit(makeVideo);
	}


	private void textFileBuilder(List<String> images, double totalDuration) {
		double duration = totalDuration/images.size();
		String stringDuration = Double.toString(duration);
		String text = ""; 	
		String lastImage="";
		for(String name:images) {
			System.out.println(name);
			text= text +"file '"+ name +".jpg'\nduration " + stringDuration + "\n";
			lastImage=name;
		}
		text=text+"file '"+lastImage+"'";

		RunBash createFile = new RunBash("touch ./resources/temp/cmd.txt ; echo -e \""+text+ "\" > ./resources/temp/cmd.txt");
		_team.submit(createFile);
	}

	private void markImages(List<String> images) {
		for (String path: images) {
			System.out.println(path);
			RunBash mark= new RunBash("ffmpeg -i ./resources"+resourceLocation+"/images/" + path + ".jpg -vf \"drawtext=text='"+ term + "':fontcolor=white:fontsize=75:x=(w-text_w)/2: y=(h-text_h-line_h)/2:\" ./resources/temp/" + path+".jpg");
			_team.submit(mark);
		}
	}

	@Override
	protected Void call() throws Exception {
		
		String audioFileNames = "";

		for(String audio:_files) {
			audioFileNames = audioFileNames+"./resources"+resourceLocation +"/audio/"+audio+".wav ";
		}	

		RunBash mergeAudio = new RunBash("sox "+ audioFileNames +" ./resources/temp/output.wav");
		System.out.println(audioFileNames);
		
		mergeAudio.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			
			@Override
			public void handle(WorkerStateEvent event) {
				RunBash audioLengthSoxi = new RunBash("soxi -D ./resources/temp/output.wav");
				audioLengthSoxi.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
					@Override
					public void handle(WorkerStateEvent event) {
						double audioLength;

						try {
							audioLength = Double.parseDouble(audioLengthSoxi.get().get(0));
							RunBash createVideoAudio = new RunBash("ffmpeg -i ./resources/temp/output.wav -vn -ar 44100 -ac 2 -b:a 192k ./resources/temp/output.mp3 &> /dev/null ");
							_team.submit(createVideoAudio);
						
							if(!usingImages) {
								createVideo = new RunBash(" ffmpeg -f lavfi -i color=c=blue:s=320x240:d="+audioLength 
										+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
										+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text="+term+"\" ./resources/temp/"+name+"noImage.mp4 &> /dev/null ;"
										+ "; ffmpeg -i ./resources/temp/"+name +"noImage.mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental " 
										+ outputLocation + " &> /dev/null");
							} else {
								markImages(_selectedImages);
								textFileBuilder(_selectedImages,audioLength);
								videoMaker();
								createVideo = new RunBash("ffmpeg -i ./resources/temp/"+name +".mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ outputLocation+"  &> /dev/null");

							}
							_team.submit(createVideo);
							createVideo.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
								@Override
								public void handle(WorkerStateEvent event) {
									(new BGM(_music)).mergeBGM(name,audioLength);
									createFinished=true;
								}
							});
						} catch (NumberFormatException | InterruptedException | ExecutionException e) {
						}

					}
				});
				_team.submit(audioLengthSoxi);
			}
		});
		_team.submit(mergeAudio);	
		while(!createFinished) {
			if(_team.isTerminated()) {
				createFinished=true;
			}
		}
		return null;

	}
}




