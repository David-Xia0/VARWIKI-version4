package application;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javafx.scene.media.Media;

public class QuizTemplate implements Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int _total;
	private List<String> _names;
	private String _name;
	
	
	public QuizTemplate(List<String> names, String name) {
		_total = names.size();
		_names = names;
		_name = name;
	}

	public int getTotal() {
		return _total;
	}
	
	public Media getVideo(int i) {
		URL mediaUrl;
		try {
			mediaUrl = new File(Main.getPathToResources() + "/VideoCreations/"+_names.get(i)+".mp4").toURI().toURL();
			Media media = new Media(mediaUrl.toExternalForm());
			return media;
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public String toString() {
		return _name;
	}

}
