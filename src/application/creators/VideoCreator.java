package application.creators;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import application.RunBash;
import application.scenebuilder.CreateHubController;
import application.scenebuilder.TemplateData;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

/**
 * this class creates The final video output using all audio and image inputs
 * @author student
 *
 */
public class VideoCreator extends Task<Void> {

	private ExecutorService _team = Executors.newSingleThreadExecutor();

	/*
	 * all information required to make a video
	 */
	private final String name;
	private final String term;
	private final String _music;
	private boolean usingImages;
	private boolean createFinished;
	private final List<String> _files;
	private final List<String> _selectedImages;
	private final String resourceLocation;
	private final String outputLocation;
	private RunBash createVideo;

	
	/*
	 * the following constuctors are all used to extract information required to make a video
	 */
	public VideoCreator(TemplateData videoData) {
		name = videoData.getName();
		term = videoData.getTerm();
		_music = videoData.getBGM();
		usingImages = videoData.usingImages();
		_files = videoData.getOrder();
		_selectedImages = new ArrayList<String>();
		List<String> selectedImages = videoData.getSelectedImages();
		for (String image : selectedImages) {
			_selectedImages.add(image);
		}
		resourceLocation = "/temp";
		outputLocation = "./resources/VideoCreations/" + name + ".mp4";
	}

	public VideoCreator(CreateHubController videoData) {
		name = videoData.getName();
		term = videoData.getTerm();
		_music = videoData.getBGM();
		usingImages = videoData.usingImages();
		_files = videoData.fileOrder();
		_selectedImages = new ArrayList<String>();
		List<String> selectedImages = videoData.getSelectedImages();
		for (String image : selectedImages) {
			_selectedImages.add(image);
		}
		resourceLocation = "/temp";
		outputLocation = "./resources/VideoCreations/" + name + ".mp4";
	}
	
	

	/**
	 * creates the slide show for the video using ffmpeg
	 * The duration of each image is the same. This is calculated by divding up total audio time
	 * @param images
	 * @param totalDuration
	 */
	private void videoMaker(List<String> images, double totalDuration) {
		double duration = totalDuration / images.size();
		String stringDuration = Double.toString(duration);
		String text = "";
		String lastImage = "";
		for (String name : images) {
			System.out.println(name);
			text = text + "file 'images/" + name + "'\nduration " + stringDuration + "\n";
			lastImage = name;
		}
		text = text + "file '" + lastImage + "'";

		RunBash createFile = new RunBash(
				"touch ./resources/temp/cmd.txt ; echo -e \"" + text + "\" > ./resources/temp/cmd.txt");
		_team.submit(createFile);
		
		RunBash makeVideo = new RunBash(
				"ffmpeg -f concat -safe 0 -i ./resources/temp/cmd.txt -r 25 -pix_fmt yuv420p -vf 'scale=trunc(iw/2)*2:trunc(ih/2)*2' "
						+ " ./resources/temp/" + name + ".mp4 ");
		_team.submit(makeVideo);
	}


	/**
	 * this is the main Video Creation method.
	 * This method concatentates all audio files and then merges audio with image slideshow
	 */
	@Override
	protected Void call() throws Exception {

		String audioFileNames = "";
		for (String audio : _files) {
			audioFileNames = audioFileNames + "./resources" + resourceLocation + "/audio/" + audio + ".wav ";
		}

		RunBash mergeAudio = new RunBash("sox " + audioFileNames + " ./resources/temp/output.wav");
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
							RunBash createVideoAudio = new RunBash(
									"ffmpeg -i ./resources/temp/output.wav -vn -ar 44100 -ac 2 -b:a 192k ./resources/temp/output.mp3 &> /dev/null ");
							_team.submit(createVideoAudio);

							if (!usingImages) {
								createVideo = new RunBash(" ffmpeg -f lavfi -i color=c=blue:s=320x240:d=" + audioLength
										+ " -vf \"drawtext=fontfile=/path/to/font.ttf:fontsize=30: "
										+ "fontcolor=white:x=(w-text_w)/2:y=(h-text_h)/2:text=" + term
										+ "\" ./resources/temp/" + name + "noImage.mp4 &> /dev/null ;"
										+ " ffmpeg -i ./resources/temp/" + name
										+ "noImage.mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ outputLocation + " &> /dev/null");
							} else {
								videoMaker(_selectedImages, audioLength);
								createVideo = new RunBash("ffmpeg -i ./resources/temp/" + name
										+ ".mp4 -i ./resources/temp/output.mp3 -c:v copy -c:a aac -strict experimental "
										+ outputLocation + "  &> /dev/null");

							}
							
							createVideo.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
								@Override
								public void handle(WorkerStateEvent event) {
									(new BGM(_music)).mergeBGM(name, audioLength);
									createFinished = true;
								}
							});
							_team.submit(createVideo);
						} catch (NumberFormatException | InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				});
				
				_team.submit(audioLengthSoxi);
			}
		});
		
		_team.submit(mergeAudio);
		
		while (!createFinished) {
			if (_team.isTerminated()) {
				createFinished = true;
			}
		}
		return null;
	}
}
