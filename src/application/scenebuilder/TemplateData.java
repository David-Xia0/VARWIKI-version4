package application.scenebuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import application.AudioBar;
import application.RunBash;

/**
 * This class is used to store Creation information. So this information can be accessed later for modification etc.
 *
 */
public class TemplateData implements Serializable {
		

	private static final long serialVersionUID = 1L;
	private final String text;
	private final String name;
	private final String term;
	private final String BGM;
	private boolean usingImages;
	private final List<String> audioText;
	private final List<String> _files;
	private final List<String> _selectedImages;
	private boolean _isTemplate;
	
	/**
	 * all information about creation is obatined from the create menu controller scene
	 * @param template
	 */
	public TemplateData(CreateMenuController template) {
		text = template.getText();
		name = template.getName();
		term = template.getTerm();
		BGM = template.getBGM();
		_isTemplate = true;
		usingImages = template.usingImages();
		audioText = template.getAudioText();
		_files = template.fileOrder();
		_selectedImages = new ArrayList<String>();
		List<String> selectedImages = template.getSelectedImages();
		for(String image: selectedImages) {
			_selectedImages.add(image.substring(0, image.lastIndexOf(".")));
		}
	}
	
	/**
	 * all information about creation is obatined from the create hub controller scene
	 * @param template
	 */
	public TemplateData(CreateHubController template) {
		text = template.getBoxText();
		name = template.getName();
		term = template.getTerm();
		BGM = template.getBGM();
		_isTemplate = true;
		usingImages = template.usingImages();
		audioText = template.getAudioText();
		_files = template.fileOrder();
		_selectedImages = new ArrayList<String>();
		List<String> selectedImages = template.getSelectedImages();
		for(String image: selectedImages) {
			_selectedImages.add(image.substring(0, image.lastIndexOf(".")));
		}
	}
	
	public TemplateData(String term, String text) {
		BGM = "No Music";
		this.term=term;
		this.text = text;
		_isTemplate = false;
		_files = null;
		List<String> defaultSelection = new ArrayList<String>();
		for(int i =1; i<10; i++) {
		   defaultSelection.add(i+"");
		}
		_selectedImages =defaultSelection;
		name = term;
		audioText = null;
	}
	
	/*
	 * The following set of methods are all getters that return specific information
	 */
	
	public String getText() {
		return text;
	}
	
	public String getText(int audioNumber) {
		for(String text :audioText) {
			System.out.println(text);
		}
		return audioText.get(audioNumber);
	}
	
	public List<String> getAudioText(){
		return audioText;
	}
	
	public List<String> getSelectedImages(){
		return _selectedImages;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getOrder(){
		return _files;
	}
	
	public String getTerm() {
		return term;
	}
	
	public String getBGM() {
		return BGM;
	}
	
	public boolean usingImages() {
		return usingImages;
	}
	
	public boolean isTemplate() {
		return _isTemplate;
	}

	public Future<?> load() {
		String path = "./resources/templates/" + name;
		ExecutorService team = Executors.newSingleThreadExecutor(); 
		team.submit(new RunBash("cp -rf " + path +"/images ./resources/temp"));
		return team.submit(new RunBash("cp -rf " + path +"/audio ./resources/temp"));
	}
	
}
