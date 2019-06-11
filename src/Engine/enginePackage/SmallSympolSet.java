package Engine.enginePackage;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class SmallSympolSet {
	HashMap<String, BufferedImage> sympolMap = new HashMap<String, BufferedImage>();
// ************************************************************
	public SmallSympolSet() {
      String tName = System.getProperty("user.dir")+"/res/images/SmallSympols";
      listFilesForFolder(new File(tName));
	}
// ************************************************************
	public BufferedImage getGlyph(String sympolName) {
		return sympolMap.get(sympolName);
	}
// ************************************************************
	private void listFilesForFolder(final File folder) {
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	      	  String tString = fileEntry.getName();
	      	  if (!tString.equals(".DS_Store")) {
	      		  String sympolName = tString.substring(0, tString.length()-4);
	      		  sympolMap.put(sympolName, readImage("SmallSympols/"+tString));
	      	  }
	        }
	    }
	}
// ************************************************************
	// just a file-reading routine
	private BufferedImage readImage(String fileName) {
      BufferedImage bi = null;
      String tName = System.getProperty("user.dir")+"/res/images/"+fileName;
		try {
			bi=ImageIO.read(new File(tName));		
      } catch (Exception e) { 
      	System.out.println("Cannot find image: "+fileName); }
		return bi;
	}
// ************************************************************
// ************************************************************
// ************************************************************
}
