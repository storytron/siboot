package Engine.enginePackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import com.storytron.enginecommon.Utils;
import com.storytron.uber.Actor;
import com.storytron.uber.Deikto;
import com.storytron.uber.Sentence;
import com.storytron.uber.Verb;
import com.storytron.uber.Verb.WSData;
import com.storytron.uber.operator.Operator;

public class HistoryPanel extends JLabel {
	// This class draws the scroll pane on the right side of the window.
	private static final long serialVersionUID = 1L;
	static final 	Color transparent = new Color(0,0,0,0);	// transparency color
	static final int PaneWidth = 380;  // width of the scroll pane
	static final int PaneHeight = 740;  // height of the scroll pane
	private static final int CharacterDisplayY = 30;	// initial offset for painting
	private static final int GlyphSize = 75;  // standard size of the glyphs being painted, including space
	
	
	private Image mainBuffer; // the offscreen image into which I paint
	Graphics2D offScreenG2D, mainBufferG2D;
	Engine tEngine; // needed for looking up past events
	// these next images are for "graphical housekeeping".
	private BufferedImage upLink, downLink, rightLink, leftLink, lightener, darkener;
	private BufferedImage upRightLink, downRightLink, upLeftLink, downLeftLink;
	private SmallSympolSet smallSympols; // the complete set of sympols
	private int selectedCharacter; // index of the character whom the player has clicked on
	private boolean isMouseDown; // a simple flag
	private Deikto dk;  // the main dictionary of SympolTalk, used to look up sympols.
	private BufferedImage[] numbers, certainties; // the sympol images for numbers and certainties
	private Color[] backgroundColors;

// ************************************************************
	public HistoryPanel(Engine theEngine, Deikto tdk) {	
		setVisible(false); // make sure it doesn't attempt to paint until AFTER it has been initialized
		tEngine = theEngine;
		dk = tdk;
		smallSympols = new SmallSympolSet();
		setPreferredSize(new Dimension(PaneWidth, 1000));
		setLayout(new BorderLayout());
		
		setBackground(Color.green);
		
		
		upLink = readImage("UpLink");
		downLink = readImage("DownLink");
		leftLink = readImage("LeftLink");
		rightLink = readImage("RightLink");
		upRightLink = readImage("UpRightLink");
		downRightLink = readImage("DownRightLink");
		upLeftLink = readImage("UpLeftLink");
		downLeftLink = readImage("DownLeftLink");
		lightener = readImage("lightener");
		darkener = readImage("darkener");
		numbers = new BufferedImage[7];
		numbers[0] = readImage("very negative");
		numbers[1] = readImage("negative");
		numbers[2] = readImage("slightly negative");
		numbers[3] = readImage("zero");
		numbers[4] = readImage("slightly positive");
		numbers[5] = readImage("positive");
		numbers[6] = readImage("very positive");
		
		certainties = new BufferedImage[6];
		certainties[0] = readImage("very uncertain");
		certainties[1] = readImage("uncertain");
		certainties[2] = readImage("slightly uncertain");
		certainties[3] = readImage("slightly certain");
		certainties[4] = readImage("certain");
		certainties[5] = readImage("very certain");

		selectedCharacter = 1;
		isMouseDown = false;
		
		backgroundColors = new Color[12];
		backgroundColors[0] = new Color(255, 255, 192);
		backgroundColors[1] = new Color(255, 255, 128);
		backgroundColors[2] = new Color(255, 192, 255);
		backgroundColors[3] = new Color(255, 128, 255);
		backgroundColors[4] = new Color(192, 255, 255);
		backgroundColors[5] = new Color(128, 255, 255);
		backgroundColors[6] = new Color(255, 192, 128);
		backgroundColors[7] = new Color(192, 255, 128);
		backgroundColors[8] = new Color(192, 128, 255);
		backgroundColors[9] = new Color(192, 128, 255);
		backgroundColors[10] = new Color(192, 128, 128);
		backgroundColors[11] = new Color(128, 192, 128);
				      
      setVisible(true); // OK, now we can permit painting
		
		// low-level mouse event listener
		addMouseListener(new MouseListener() {
//			  public void mouseDragged(MouseEvent e) { }
				//------------------------------------------------------------
			  public void mouseReleased(MouseEvent e) {
				  // first question: did he click on a glyph, any glyph?
				  if (isMouseDown) {
					  isMouseDown = false;
					  repaint();
				  }
			  }
				//------------------------------------------------------------
			  public void mouseClicked(MouseEvent e) { }
				//------------------------------------------------------------
			  public void mousePressed(MouseEvent e) {
				  int ex = e.getX();
				  int ey = e.getY();
					  
				  // now check for mouseclicks in the Character Display box
				  selectedCharacter = 1;
					for (int i=1; i<6; ++i) {
						if ((ex > 20+(i-1)*GlyphSize) & (ex < 20+i*GlyphSize-15) & (ey > 10) & (ey < CharacterDisplayY+GlyphSize)) {
							selectedCharacter = i;	
						}
					}
				  
				  isMouseDown = (selectedCharacter > 0);
				  if (isMouseDown) { repaint(); } 
				  
			  }
				//------------------------------------------------------------
			  // In a better world, I would make a button unpressed if this happened to it,
			  //  but I'm too lazy.
			  public void mouseExited(MouseEvent e) { }
				//------------------------------------------------------------
			  public void mouseEntered(MouseEvent e) { }
			});

	}		
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	   public void paint(Graphics g){
	   	Graphics2D g2 = (Graphics2D) g;
			mainBuffer = createImage(PaneWidth, 10000);
			mainBufferG2D = (Graphics2D)mainBuffer.getGraphics();

			// paint the sympols for the five characters onto the top of the panel
			for (int i=1; i<6; ++i) {
				String name = dk.getActor(i).getLabel();
				mainBufferG2D.drawImage(smallSympols.getGlyph(name), 10+(i-1)*GlyphSize, CharacterDisplayY, transparent, this);
				if (i != selectedCharacter) { // lighten unselected characters
					mainBufferG2D.drawImage(lightener,10+(i-1)*GlyphSize, CharacterDisplayY, transparent, this);									
				}
			}
			if (selectedCharacter>0) {
				if (isMouseDown) // this darkens the character if the mouse is down on him
					mainBufferG2D.drawImage(darkener,10+(selectedCharacter-1)*GlyphSize, CharacterDisplayY, transparent, this);	
				else { // this paints all the information about the selected character.
					Actor selectedActor = dk.getActor(selectedCharacter);

					// first draw auragon counts with certainties
					for (int j=0; j<3; ++j) {
						String sympolName = "";
						switch (j) {
							case 0: { sympolName = "Red"; break; }
							case 1: { sympolName = "Green"; break; }
							case 2: { sympolName = "Blue"; break; }
						}
		 				mainBufferG2D.drawImage(smallSympols.getGlyph(sympolName+"Auragon"), 60+GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, transparent, this);
						mainBufferG2D.drawImage(leftLink, 45+3*GlyphSize/2, CharacterDisplayY+124+j*GlyphSize, transparent, this);

						int iTrait = dk.findActorTraitWord(sympolName+"Auragon"); 
						float auragonCount = dk.getActor(1).getP(dk.getActorTraits().get(iTrait), selectedActor);
						int iCount = (int)bounded2Real(auragonCount)+3;
		 				mainBufferG2D.drawImage(numbers[iCount], 60+3*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, transparent, this);
						mainBufferG2D.drawImage(leftLink, 45+5*GlyphSize/2, CharacterDisplayY+124+j*GlyphSize, transparent, this);
						
						float certainty = dk.getActor(1).getU(dk.getActorTraits().get(iTrait), selectedActor);
						int iCertainty = (int)(bounded2Real(certainty)+2.5f);
						if (iCertainty < 0) 
							iCertainty = 0;
						if (iCertainty > 5) 
							iCertainty = 5;
		 				mainBufferG2D.drawImage(certainties[iCertainty], 60+5*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, transparent, this);
//	 						Utils.displayDebuggingError(auragonCount+"  "+(int)bounded2Real(auragonCount)+"      "+certainty+"  "+iCertainty);
					}

					// next draw P3 values with certainties
					for (int j=0; j<3; ++j) {
						String name = dk.getActor(1).getLabel();
		 				mainBufferG2D.drawImage(smallSympols.getGlyph(name), -15+GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
						
						String sympolName = "";
						switch (j) {
							case 0: { sympolName = "powerful"; break; }
							case 1: { sympolName = "honest"; break; }
							case 2: { sympolName = "good"; break; }
						}
		 				mainBufferG2D.drawImage(smallSympols.getGlyph(sympolName), -15+2*GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
						mainBufferG2D.drawImage(leftLink, -30+2*GlyphSize, CharacterDisplayY+390+j*GlyphSize+23, transparent, this);

						int iTrait = dk.findActorTraitWord(sympolName)-2; 
						float p3Value = dk.getActor(1).getP3(iTrait, selectedCharacter, 1);
						int iValue = (int)bounded2Real(p3Value)+3;
						if (iValue < 0) 
							iValue = 0;
						if (iValue > 6) 
							iValue = 6;
		 				mainBufferG2D.drawImage(numbers[iValue], -15+3*GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
						mainBufferG2D.drawImage(leftLink, -30+3*GlyphSize, CharacterDisplayY+390+j*GlyphSize+23, transparent, this);
						
						float certainty = dk.getActor(1).getU3(iTrait, selectedCharacter, 1);
						int iCertainty = (int)bounded2Real(certainty)+3;
						if (iCertainty < 0) 
							iCertainty = 0;
						if (iCertainty > 6) 
							iCertainty = 6;
		 				mainBufferG2D.drawImage(certainties[iCertainty], -15+4*GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
						mainBufferG2D.drawImage(leftLink, -30+4*GlyphSize, CharacterDisplayY+390+j*GlyphSize+23, transparent, this);
//	 						Utils.displayDebuggingError(auragonCount+"  "+(int)bounded2Real(auragonCount)+"      "+certainty+"  "+iCertainty);
					}
				}
					
				mainBufferG2D.fillRect(10, CharacterDisplayY + 620, PaneWidth - 20, 3);
				// now add the history
				int currentBottom = CharacterDisplayY + 550;
				int currentDay = 0;
				int cSentences = 0;
				setBackground(backgroundColors[0]);
				setPreferredSize(new Dimension(PaneWidth, 1000+100*tEngine.getHistoryBookSize()));
				for (int i=0; (i<tEngine.getHistoryBookSize()); ++i) {
					boolean isTwoRowsHigh = false;
					Sentence zSentence = tEngine.getHistoryBookPage(i);
					Verb zVerb = tEngine.getDeikto().getVerb(zSentence.getIWord(Sentence.Verb));
					if (zVerb.getLabel().equals("night falls")) {
						mainBufferG2D.setColor(new Color(backgroundColors[currentDay].getRed()/2, backgroundColors[currentDay].getGreen()/2,backgroundColors[currentDay].getBlue()/2));
					}
					if (zVerb.getLabel().equals("start the day")) {
						++currentDay;
						mainBufferG2D.setColor(backgroundColors[currentDay]);
					}
					if ((zSentence.getWhoKnows(1)) & ((zSentence.getIWord(0)==selectedCharacter) 
							| (zSentence.getIWord(2)==selectedCharacter)  
							| ((zSentence.getIWord(3)==selectedCharacter) & (zSentence.getWordSocket(3).getType()==Operator.Type.Actor)))) {
						int backgroundHeight = GlyphSize+10;
						int n = 0;
						while (zSentence.getIWord(n) > 0) {
							if (zVerb.getSentenceRow(n)>0) backgroundHeight += GlyphSize;
							++n;
						}
							
						mainBufferG2D.fillRect(0, currentBottom + GlyphSize + cSentences * GlyphSize, PaneWidth, backgroundHeight);
						
						++cSentences;
						int offset = (zSentence.getIWord(0) != 1) ? 50 : 0;
						int j = 0;
						while (zSentence.getIWord(j)>=0) {
							if (zVerb.isVisible(j) & ((j>0) | (zSentence.getIWord(j) > 0))) {
								if (zVerb.getSentenceRow(j)>0)
									isTwoRowsHigh = true;
								int x = 20 + GlyphSize * zVerb.getSentenceColumn(j) + offset;
								int y = currentBottom + GlyphSize * zVerb.getSentenceRow(j) + cSentences * GlyphSize;
								int k = zSentence.getIWord(j);
								BufferedImage testImage = smallSympols.getGlyph(tEngine.getDeikto().getLabelByDataType(zSentence.getWordSocket(j).getType(), k));
								if (testImage != null) {
									mainBufferG2D.drawImage(testImage, x, y, transparent, null);
								}
								
								WSData wsd = zVerb.getWSData(j);
								if (wsd.outArrow[WSData.UP]) 
									mainBufferG2D.drawImage(upLink, x+22, y-15, transparent, null);
								if (wsd.outArrow[WSData.DOWN]) 
									mainBufferG2D.drawImage(downLink, x+22, y+59, transparent, null);
								if (wsd.outArrow[WSData.LEFT]) 
									mainBufferG2D.drawImage(leftLink, x-15, y+23, transparent, null);
								if (wsd.outArrow[WSData.RIGHT]) 
									mainBufferG2D.drawImage(rightLink, x+60, y+23, transparent, null);
								if (wsd.outArrow[WSData.UPRIGHT]) 
									mainBufferG2D.drawImage(upRightLink, x+60, y-20, transparent, this);
								if (wsd.outArrow[WSData.DOWNRIGHT]) 
									mainBufferG2D.drawImage(downRightLink, x+60, y+58, transparent, this);
								if (wsd.outArrow[WSData.UPLEFT]) 
									mainBufferG2D.drawImage(upLeftLink, x-15, y-20, transparent, this);
								if (wsd.outArrow[WSData.DOWNLEFT]) 
									mainBufferG2D.drawImage(downLeftLink, x-15, y+58, transparent, this);
							}
							++j;
						}
						currentBottom += isTwoRowsHigh ? GlyphSize +10 : 10;
					}
				}
			}
	    g2.drawImage(mainBuffer, 0, 0, transparent, this);
}
	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
 // ************************************************************   	
	/*   public void paintComponent(Graphics g){

   	// This paint method works. It's what happens AFTER it paints that trouble starts.
		mainBuffer = createImage(PaneWidth, PaneHeight);
		mainBufferG2D = (Graphics2D)mainBuffer.getGraphics();
		mainBufferG2D.setColor(Color.blue); // testing the drawing onto the screen
		mainBufferG2D.fillRect(0, 0, 300, 600);

		// paint the sympols for the five characters onto the top of the panel
		for (int i=2; i<6; ++i) {
			String name = dk.getActor(i).getLabel();
			mainBufferG2D.drawImage(smallSympols.getGlyph(name), (i-1)*GlyphSize, CharacterDisplayY, transparent, this);
			if (i != selectedCharacter) { // lighten unselected characters
				mainBufferG2D.drawImage(lightener,(i-1)*GlyphSize, CharacterDisplayY, transparent, this);									
			}
		}
		if (selectedCharacter>0) {
			if (isMouseDown) // this darkens the character if the mouse is down on him
				mainBufferG2D.drawImage(darkener,(selectedCharacter-1)*GlyphSize, CharacterDisplayY, transparent, this);	
			else { // this paints all the information about the selected character.
				Actor selectedActor = dk.getActor(selectedCharacter);

				// first draw auragon counts with certainties
				for (int j=0; j<3; ++j) {
					String sympolName = "";
					switch (j) {
						case 0: { sympolName = "Red"; break; }
						case 1: { sympolName = "Green"; break; }
						case 2: { sympolName = "Blue"; break; }
					}
	 				mainBufferG2D.drawImage(smallSympols.getGlyph(sympolName+"Auragon"), 100+GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, transparent, this);
					mainBufferG2D.drawImage(leftLink, 85+3*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize+33, transparent, this);

					int iTrait = dk.findActorTraitWord(sympolName+"Auragon"); 
					float auragonCount = dk.getActor(1).getP(dk.getActorTraits().get(iTrait), selectedActor);
					int iCount = (int)bounded2Real(auragonCount)+3;
	 				mainBufferG2D.drawImage(numbers[iCount], 10+GlyphSize, CharacterDisplayY+100+j*GlyphSize, transparent, this);
					mainBufferG2D.drawImage(leftLink, 85+5*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize+33, transparent, this);
					
					float certainty = dk.getActor(1).getU(dk.getActorTraits().get(iTrait), selectedActor);
					int iCertainty = (int)(bounded2Real(certainty)+2.5f);
					if (iCertainty < 0) 
						iCertainty = 0;
					if (iCertainty > 5) 
						iCertainty = 5;
	 				mainBufferG2D.drawImage(certainties[iCertainty], 100+5*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, transparent, this);
// 						Utils.displayDebuggingError(auragonCount+"  "+(int)bounded2Real(auragonCount)+"      "+certainty+"  "+iCertainty);
				}

				// next draw P3 values with certainties
				for (int j=0; j<3; ++j) {
					String name = dk.getActor(1).getLabel();
	 				mainBufferG2D.drawImage(smallSympols.getGlyph(name), 10+GlyphSize, 430+j*GlyphSize, transparent, this);
					
					String sympolName = "";
					switch (j) {
						case 0: { sympolName = "powerful"; break; }
						case 1: { sympolName = "honest"; break; }
						case 2: { sympolName = "good"; break; }
					}
	 				mainBufferG2D.drawImage(smallSympols.getGlyph(sympolName), 10+2*GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
					mainBufferG2D.drawImage(leftLink, -5+2*GlyphSize, CharacterDisplayY+390+j*GlyphSize+33, transparent, this);

					int iTrait = dk.findActorTraitWord(sympolName)-2; 
					float p3Value = dk.getActor(1).getP3(iTrait, selectedCharacter, 1);
					int iValue = (int)bounded2Real(p3Value)+3;
					if (iValue < 0) 
						iValue = 0;
					if (iValue > 6) 
						iValue = 6;
	 				mainBufferG2D.drawImage(numbers[iValue], 10+3*GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
					mainBufferG2D.drawImage(leftLink, -5+3*GlyphSize, CharacterDisplayY+390+j*GlyphSize+33, transparent, this);
					
					float certainty = dk.getActor(1).getU3(iTrait, selectedCharacter, 1);
					int iCertainty = (int)bounded2Real(certainty)+3;
					if (iCertainty < 0) 
						iCertainty = 0;
					if (iCertainty > 6) 
						iCertainty = 6;
	 				mainBufferG2D.drawImage(certainties[iCertainty], 10+4*GlyphSize, CharacterDisplayY+390+j*GlyphSize, transparent, this);
					mainBufferG2D.drawImage(leftLink, -5+4*GlyphSize, CharacterDisplayY+390+j*GlyphSize+33, transparent, this);
// 						Utils.displayDebuggingError(auragonCount+"  "+(int)bounded2Real(auragonCount)+"      "+certainty+"  "+iCertainty);
				}
			}
		}
    view.getGraphics().drawImage(mainBuffer, 0, 0, transparent, this);
    // All the imagery painted in this method is drawn onto the panel by the above line.
    // But it is immediately overwritten by the yellow background of the ViewPort.
	}
	*/
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		float bounded2Real(float boundedNumber) {
			if (boundedNumber > 0.0f) {
				if (boundedNumber>Utils.MAXI_VALUE)
					boundedNumber = Utils.MAXI_VALUE;
				return (1.0f / (1.0f - boundedNumber)) -1.0f;
			}
			else
			{
				if (boundedNumber<Utils.MINI_VALUE)
					boundedNumber = Utils.MINI_VALUE;
				return 1.0f - (1.0f / (1.0f + boundedNumber));
			}
		}
// ************************************************************
	// just a file-reading routine
	private BufferedImage readImage(String fileName) {
      BufferedImage bi=null;
      String tName = System.getProperty("user.dir")+"/res/images/SmallSympols/"+fileName+".png";
		try {
			bi=ImageIO.read(new File(tName));		
      } catch (Exception e) { 
      	System.out.println("Cannot find image: "+fileName); }
		return bi;
	}
// ************************************************************

}

