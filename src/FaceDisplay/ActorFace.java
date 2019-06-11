package FaceDisplay;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;

public class ActorFace extends JDialog implements ImageObserver{
	private static final long serialVersionUID = 1l;
	public static final double featureScale = 0.225; // ratio of final size to editing size
	public static final double baseFaceScale = 0.375; // ratio of final size to editing size
	
	// These are graphic features associated with each actor. 
	// Meaning is obvious from labels.
	private int[][]  thickness = new int[Feature.MaxFeatureSize][Expression.FeatureCount];
	private int[] bigVerticalOffsets = new int[Feature.MaxFeatureSize];
	private int[] smallVerticalOffsets = new int[Feature.MaxFeatureSize];
	private int  bigBaseY; 
	private int  smallBaseY; 
	private int eyeSeparation;
	private int jowlSeparation;
	private float jawWidth;
	private float eyeSize;
	private String myName;

	// These are PNGs drawn by the artist.
	// bigFace is the primary background face; it should be 1120h x 1272v
	// bigIris should be about 80h x 80v
	private static BufferedImage standardIris = readImage("standardIris");
	private static BufferedImage standardNose = readImage("standardNose");
	
	// these images are scaled down from the three above by 'scalingFactor'.
	private BufferedImage smallFace, smallNose, smallIris, mask, shoulders;
	
// ************************************************************
	public ActorFace(String name) {
		myName = name;		
		smallFace = readImage(myName+"/baseFace");
		smallNose = readImage(myName+"/Nose");
		smallIris = readImage(myName+"/Iris");
		mask = readImage(myName+"/mask");
		shoulders = readImage(myName+"/shoulders");
		
		for (int i = 0; (i<Feature.MaxFeatureSize); ++i) {
			bigVerticalOffsets[i] = 0;
			for (int j=0; j<Expression.FeatureCount; ++j) {
				thickness[i][j] = 0;
			}
		}
		jawWidth = 1.0f;
	}
// ************************************************************
	public static BufferedImage getScaledImage(BufferedImage image, int width, int height) throws IOException {
	    int imageWidth  = image.getWidth();
	    int imageHeight = image.getHeight();

	    double scaleX = (double)width/imageWidth;
	    double scaleY = (double)height/imageHeight;
	    AffineTransform scaleTransform = AffineTransform.getScaleInstance(scaleX, scaleY);
	    AffineTransformOp bilinearScaleOp = new AffineTransformOp(scaleTransform, AffineTransformOp.TYPE_BILINEAR);
	    
	    return bilinearScaleOp.filter( image, new BufferedImage(width, height, image.getType()));
	}
// ************************************************************
// ****************** Getters and Setters *********************
// ************************************************************
// ************************************************************
	public String getName() { return myName; }
// ************************************************************
	public int getSmallBaseY() {return smallBaseY; }
// ************************************************************
	public void setSmallBaseY(int newValue) { smallBaseY = newValue; }
// ************************************************************
	public int getBigBaseY() { return bigBaseY; }
// ************************************************************
	public void setBigBaseY(int newValue) { bigBaseY = newValue; }
// ************************************************************
	public int getBigVerticalOffsets(int i) { return bigVerticalOffsets[i]; }
// ************************************************************
	public void setBigVerticalOffsets(int i, int newValue) { bigVerticalOffsets[i] = newValue; }
// ************************************************************
	public int getSmallVerticalOffsets(int i) { return smallVerticalOffsets[i]; }
// ************************************************************
	public void setSmallVerticalOffsets(int i, int newValue) { smallVerticalOffsets[i] = newValue; }
// ************************************************************
	public void setThickness(int i, int j, int newValue) { thickness[i][j] = newValue; }
// ************************************************************
	public int getThickness(int i, int j) { return thickness[i][j]; }
// ************************************************************
	public BufferedImage getSmallFace() {return smallFace; }
// ************************************************************
	public BufferedImage getSmallIris() {return smallIris; }
// ************************************************************
	public BufferedImage getSmallNose() {return smallNose; }
// ************************************************************
	public float getJawWidth() { return jawWidth; }
// ************************************************************
	public void setJawWidth(float newMouthWidth) { jawWidth = newMouthWidth; }
// ************************************************************
	public float getEyeSize() { return eyeSize; }
// ************************************************************
	public void setEyeSize(float newEyeSize) { eyeSize = newEyeSize; }
// ************************************************************
	public int getEyeSeparation() { return eyeSeparation; }
// ************************************************************
	public void setEyeSeparation(int newEyeSeparation) { eyeSeparation = newEyeSeparation;	}
// ************************************************************
	public int getJowlSeparation() { return jowlSeparation; }
// ************************************************************
	public void setJowlSeparation(int newJowlSeparation) { jowlSeparation = newJowlSeparation; }
// ************************************************************
	public static BufferedImage getStandardIris() { return standardIris; }
// ************************************************************
	public static BufferedImage getStandardNose() { return standardNose; }
// ************************************************************
	public BufferedImage getMask() { return mask; }
// ************************************************************
	public BufferedImage getShoulders() { return shoulders; }
// ************************************************************
// **************** End Getters and Setters *******************
// ************************************************************
	// just a file-reading routine
	public static BufferedImage readImage(String fileName) {
      BufferedImage bi=null;
		try {
			String fullName=System.getProperty("user.dir")+"/res/FaceRes/"+fileName+".png";
			File tFile = new File(fullName);
			bi=ImageIO.read(tFile);		
      } catch (IOException e) {
      	System.out.println("can't find Image "+fileName); 
      	}
		return bi;
	}
// ************************************************************
	public void makeItSmall() {
		for (int i = 0; (i<Feature.MaxFeatureSize); ++i) {
			for (int j=0; j<Expression.FeatureCount; ++j) {
				thickness[i][j] *= featureScale;
			}
			smallVerticalOffsets[i] = (int)(bigVerticalOffsets[i] * featureScale);
		}
	}
// ************************************************************

}
