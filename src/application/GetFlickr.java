package application;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.photos.Size;

import javafx.concurrent.Task;

/**
 * task which saves x images related to a term to /resources/temp/images
 * @author student
 *
 */
public class GetFlickr extends Task<Void>{

		private String _searchTerm;
		private int _num;

		/**
		 * constructor
		 * @param searchTerm
		 * @param num
		 */
		public GetFlickr(String searchTerm, int num){
			_searchTerm = searchTerm;
			_num=num;
		}
	
		
		/**
		 * see class description
		 */
		@Override
		protected Void call() throws Exception {
				//Main.clearImages();
		try {
			String apiKey = getKey("apiKey");
			String sharedSecret = getKey("sharedSecret");

			Flickr flickr = new Flickr(apiKey, sharedSecret, new REST());
			
			
			int page = 0;
			
	        PhotosInterface photos = flickr.getPhotosInterface();
	        SearchParameters params = new SearchParameters();
	        params.setSort(SearchParameters.RELEVANCE);
	        params.setMedia("photos"); 
	        params.setText(_searchTerm);
	        
	        PhotoList<Photo> results = photos.search(params, _num, page);
	        int i = 0;
	        for (Photo photo: results) {
	        	
	        	i++;
	        	try {
	        		BufferedImage image = photos.getImage(photo,Size.LARGE);
		        	String filename = i + ".jpg";
		        
		        	File outputfile = new File(Main.getPathToResources() + "/temp/images",filename);
		        	ImageIO.write(image, "jpg", outputfile);
		        	
	        	} catch (FlickrException fe) {
	        		System.err.println("Ignoring image " +i +": "+ fe.getMessage());
				}
	       
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * returns keys from config file
	 * @param key
	 * @return
	 * @throws Exception
	 */
	private String getKey(String key) throws Exception {

		String config = Main.getPathToResources() + "/flickr-api-keys.txt"; 
		File file = new File(config); 
		BufferedReader br = new BufferedReader(new FileReader(file)); 
		
		String line;
		while ( (line = br.readLine()) != null ) {
			if (line.trim().startsWith(key)) {
				br.close();
				return line.substring(line.indexOf("=")+1).trim();
			}
		}
		br.close();
		throw new RuntimeException("Couldn't find " + key +" in config file "+file.getName());
	}
}
