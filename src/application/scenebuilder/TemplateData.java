package application.scenebuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store Creation information. So this information can be accessed later for modification etc.
 *
 */
public class TemplateData implements Serializable {
		

	private static final long serialVersionUID = 1L;
	private final String text;
	private final String name;
	private final String term;
	private boolean usingImages;
	private final List<String> audioText;
	private final List<String> _files;
	private final List<String> _selectedImages;
	
	/**
	 * all information about creation is obatined from the create menu controller scene
	 * @param template
	 */
	public TemplateData(CreateMenuController template) {
		text = template.getText();
		name = template.getName();
		term = template.getTerm();
		usingImages = template.usingImages();
		audioText = template.getAudioText();
		_files = template.fileOrder();
		_selectedImages = new ArrayList<String>();
		List<String> selectedImages = template.getSelectedImages();
		for(String image: selectedImages) {
			_selectedImages.add(image.substring(0, image.lastIndexOf(".")));
		}
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
	
	public boolean usingImages() {
		return usingImages;
	}
	
}
