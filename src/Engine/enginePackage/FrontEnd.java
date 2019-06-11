package Engine.enginePackage;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import FaceDisplay.ActorFace;
import FaceDisplay.FaceDisplay;

import com.storytron.enginecommon.LabeledSentence;
import com.storytron.enginecommon.MenuElement;
import com.storytron.enginecommon.Utils;
import com.storytron.swat.util.MenuTooltipManager;
import com.storytron.uber.Actor;
import com.storytron.uber.Deikto;
import com.storytron.uber.Sentence;
import com.storytron.uber.Verb;
import com.storytron.uber.Verb.WSData;
import com.storytron.uber.operator.Operator;

public class FrontEnd {
	private static final int TopSentenceX = 128;
	private static final int TopSentenceY = 40;
	private static final int BottomSentenceX = 500;
	private static final int BottomSentenceY = 480;
	private static final int GlyphSize = 95;
	private static final int InnerGlyphSize = 80;
	private static final int WindowSizeX = 1366;
	private static final int WindowSizeY = 728;
	private static final int DialogSizeX = 965;
	private static final int UpperBubbleX = 97;
	private static final int UpperBubbleY = 27;
	private static final int LowerBubbleX = 444;
	private static final int LowerBubbleY = 452;
	private static final int FrameTime = 40; // duration of an excursion step in milliseconds
	
	private JFrame mainFrame;
	private DialogPanel dialogPanel;
	private SentenceImage topSentence, bottomSentence;
	private Color transparent;
	private Deikto dk;
	private BufferedImage upLink, downLink, rightLink, leftLink, doWhat, playerBack;
	private BufferedImage upRightLink, downRightLink, upLeftLink, downLeftLink;
	private BufferedImage darkener, upperBubble, lowerBubble;
	private BufferedImage[] upperBackground;
	private BufferedImage[] lowerBackground;
	private BufferedImage backstep, checkButton;
	private BufferedImage[] numbers, certainties;
	private Image windowBuffer;
	private String[] quantifierLabels, certaintyLabels, numberLabels;
	private Verb bottomVerb;
	private Engine engine;
	public Graphics2D g2;
	public boolean isGlyphSelected, isSentenceComplete, isPlayerDone, isMouseDown;
	public boolean isUpperRightSelected, isLowerRightSelected;
	public int iChosenMenuItem;
	public int clickedColumn, clickedRow;
	private ArrayList<ScreenSympol> sympols;
	int lastMouseX, lastMouseY;
	long lastMouseTime; 
	Timer toolTipTimer, toolTipStopper, animationTimer;
	private Timer attackTimer, sustainTimer, decayTimer, intervalTimer;
	private int animationStep;
	boolean isToolTipActive;
	String toolTipText;
	Rectangle myClipRect = new Rectangle(0, 0, DialogSizeX, WindowSizeY);
	FaceDisplay fd;
	Image offscreenFace;
	Graphics2D offscreenFaceG2, windowBufferG2;
	String interlocutorName;
	ActorFace fInterlocutor;
	Actor aInterlocutor;
	int iInterlocutor;
	int iEmotion, iExcursion;
	float animationFraction;
	Random myRandom;
	float excursionMagnitude, emotionMagnitude;
	HistoryPanel historyPanel;
	int iStage;
	String paintCaller = "nobody";

//**********************************************************************
	public FrontEnd(Deikto tdk) {
		this.dk = tdk;
		engine = new Engine(dk, this);
		myRandom = new Random(23674);
		mainFrame = new JFrame("Siboot");
		
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		mainFrame.setPreferredSize(new Dimension(WindowSizeX, WindowSizeY));
		mainFrame.setSize(WindowSizeX, WindowSizeY);
		mainFrame.setVisible(false);
//		mainFrame.setBackground(Color.magenta);
		
		Container contentPane = mainFrame.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		dialogPanel = new DialogPanel();
		dialogPanel.setPreferredSize(new Dimension(876, 728));
		dialogPanel.setMaximumSize(new Dimension(876, 728));
//		dialogPanel.setBackground(Color.green);
		mainFrame.add(dialogPanel);

		historyPanel = new HistoryPanel(engine, dk);

//		Icon image = new ImageIcon( System.getProperty("user.dir")+"/res/images/DSCN0055.png" );
      JScrollPane scroll = new JScrollPane();
      scroll.setPreferredSize(new Dimension(380, 740));
      scroll.getViewport().add(historyPanel);
      mainFrame.add(scroll);

		

//      mainFrame.add(historyPanel);
				
//		mainFrame.pack();
	
		numbers = new BufferedImage[7];
		certainties = new BufferedImage[7];
		quantifierLabels = new String[7];
		certaintyLabels = new String[6];
		numberLabels = new String[7];
		upperBackground = new BufferedImage[6];
		lowerBackground = new BufferedImage[6];
		sympols = new ArrayList<ScreenSympol>();

		
		transparent=new Color(0,0,0,0);
		upLink = readSympol("UpLink");
		downLink = readSympol("DownLink");
		leftLink = readSympol("LeftLink");
		rightLink = readSympol("RightLink");
		upRightLink = readSympol("UpRightLink");
		downRightLink = readSympol("DownRightLink");
		upLeftLink = readSympol("UpLeftLink");
		downLeftLink = readSympol("DownLeftLink");
		doWhat = readSympol("Do What");
		playerBack = readImage("ActorBacks/Camiggdo.png");
		backstep = readSympol("Backstep");
		checkButton = readSympol("CheckButton");
		darkener = readSympol("darkener");
		upperBubble = readImage("UpperBubble.png");
		lowerBubble = readImage("LowerBubble.png");
		
		upperBackground[0] = readImage("UpperBackgrounds/Orange.png");
		upperBackground[1] = readImage("UpperBackgrounds/Black.png");
		upperBackground[2] = readImage("UpperBackgrounds/Green.png");
		upperBackground[3] = readImage("UpperBackgrounds/Exterior.png");
		upperBackground[4] = readImage("UpperBackgrounds/Interior.png");
		upperBackground[5] = readImage("UpperBackgrounds/School.png");
		
		lowerBackground[0] = readImage("LowerBackgrounds/Orange.png");
		lowerBackground[1] = readImage("LowerBackgrounds/Black.png");
		lowerBackground[2] = readImage("LowerBackgrounds/Green.png");
		lowerBackground[3] = readImage("LowerBackgrounds/Exterior.png");
		lowerBackground[4] = readImage("LowerBackgrounds/Interior.png");
		lowerBackground[5] = readImage("UpperBackgrounds/School.png");
		
		quantifierLabels[0] = "very negative";
		quantifierLabels[1] = "negative";
		quantifierLabels[2] = "slightly negative";
		quantifierLabels[3] = "zero";
		quantifierLabels[4] = "slightly positive";
		quantifierLabels[5] = "positive";
		quantifierLabels[6] = "very positive";
	
		certaintyLabels[0] = "very uncertain";
		certaintyLabels[1] = "uncertain";
		certaintyLabels[2] = "slightly uncertain";
		certaintyLabels[3] = "slightly certain";
		certaintyLabels[4] = "certain";
		certaintyLabels[5] = "very certain";
	
		numberLabels[0] = "-3";
		numberLabels[1] = "-2";
		numberLabels[2] = "-1";
		numberLabels[3] = "0";
		numberLabels[4] = "1";
		numberLabels[5] = "2";
		numberLabels[6] = "3";
	
		for (int i=0; i<7; ++i) { numbers[i] = readSympol(quantifierLabels[i]); }
		
		for (int i=0; i<6; ++i) { certainties[i] = readSympol(certaintyLabels[i]); }
		
		bottomSentence = null;
		isGlyphSelected = false;
		isPlayerDone = false;
		isSentenceComplete = false;
		isMouseDown = false;
		isUpperRightSelected = true;
		isLowerRightSelected = true;
		lastMouseX = 0;
		lastMouseY = 0;
		isToolTipActive = false;
		mainFrame.setVisible(true);
	}
// **********************************************************************
	private class ScreenSympol {
		int x,y;
		String toolTip;
		ScreenSympol(int tx, int ty, String tToolTip) {
			x = tx;
			y = ty;
			toolTip = tToolTip;
		}
		public int getX() { return x; }
		public int getY() { return y; }
		public String getToolTip() { return toolTip; }
	}
// **********************************************************************
		private class DialogPanel extends JPanel {
			private static final long serialVersionUID = 1L;
			public DialogPanel self;
			
			public DialogPanel() {
				self = this;
				offscreenFace = new BufferedImage(300, 310, BufferedImage.TYPE_4BYTE_ABGR);
				offscreenFaceG2 = (Graphics2D)offscreenFace.getGraphics();
				offscreenFaceG2.setColor(transparent);
				offscreenFaceG2.fillRect(0,0,300,310);
				fd = new FaceDisplay(offscreenFaceG2);
				iExcursion = 13;
		
				// low-level mouse event listener
				addMouseListener(new MouseListener() {
		//			  public void mouseDragged(MouseEvent e) { }
						//------------------------------------------------------------
					  public void mouseReleased(MouseEvent e) {
						  // first question: did he click on a glyph, any glyph?
						  if (isMouseDown) {
							  isToolTipActive = false;
							  isMouseDown = false;
							  int iHotWord = bottomSentence.getHotWord();
							  
							  // recalculate the clicked glyph
							  clickedColumn = -1;
							  clickedRow = -1;
							  int ex = e.getX();
							  int ey = e.getY();
							  if ((ex > BottomSentenceX) & (ex < BottomSentenceX + 5 * GlyphSize)
									  & (ey > BottomSentenceY) & (ey < BottomSentenceY + 3 * GlyphSize)) {
								  clickedColumn = (ex - BottomSentenceX) / GlyphSize;
								  clickedRow = (ey - BottomSentenceY) / GlyphSize;
								  int sx = ex - (clickedColumn * GlyphSize + BottomSentenceX);
								  int sy = ey - (clickedRow * GlyphSize + BottomSentenceY);
								  if ((sx > InnerGlyphSize) || (sy > InnerGlyphSize)) {
									  clickedColumn = -1;
									  clickedRow = -1;
								  }
							  }
							  // is it the verb, and is the verb hot?
							  if ((clickedColumn == 1) & (clickedRow == 0) & (iHotWord == Sentence.Verb)) {
								  // yes, raise the menu for the verb
								  bottomSentence.showMenu(self, BottomSentenceX+GlyphSize, BottomSentenceY, iHotWord);						  
							  }
							  else { // was it on the hotWord?
								  if (!isSentenceComplete && (iHotWord > Sentence.Verb) && (clickedColumn == bottomVerb.getSentenceColumn(iHotWord))
										  && (clickedRow == bottomVerb.getSentenceRow(iHotWord))) {
									  bottomSentence.showMenu(self, BottomSentenceX +clickedColumn * GlyphSize, 
											  									BottomSentenceY + clickedRow * GlyphSize, iHotWord);							  
								  }
								  else { // no, was it on the backspace glyph?
									  if ((clickedColumn == 4) & (clickedRow == 0)) {
										  // yes, player clicked on the backspace glyph
										  bottomSentence.decrementHotWord();
									  }
									  else { // no, was it on the execute command key?
										  if ((clickedColumn == 4) & (clickedRow == 1) & isSentenceComplete) {
											  // yes, player clicked on the execute glyph
											  iChosenMenuItem = 0;
											  isPlayerDone = true;									  
										  }
									  }							  
								  }
							  }
							  paintCaller = "mouseReleased";
							  repaint();
						  }
					  }
						//------------------------------------------------------------
					  public void mouseClicked(MouseEvent e) { }
						//------------------------------------------------------------
					  public void mousePressed(MouseEvent e) {
						  // first check for mouseclicks in the input sentence box
						  isToolTipActive = false;
						  toolTipStopper.stop();
						  toolTipTimer.stop();
						  clickedColumn = -1;
						  clickedRow = -1;
						  int ex = e.getX();
						  int ey = e.getY();
						  if ((ex > BottomSentenceX) & (ex < BottomSentenceX + 5 * GlyphSize)
								  & (ey > BottomSentenceY) & (ey < BottomSentenceY + 3 * GlyphSize)) {
							  clickedColumn = (ex - BottomSentenceX) / GlyphSize;
							  clickedRow = (ey - BottomSentenceY) / GlyphSize;
							  int sx = ex - (clickedColumn * GlyphSize + BottomSentenceX);
							  int sy = ey - (clickedRow * GlyphSize + BottomSentenceY);
							  if ((sx > InnerGlyphSize) || (sy > InnerGlyphSize)) {
								  clickedColumn = -1;
								  clickedRow = -1;
							  }
							  isMouseDown =  ((clickedColumn >= 0) & (clickedRow >= 0));
						  }
						  						  
						  if (isMouseDown) { paintCaller = "isMouseDown"; repaint(); } 
						  
					  }
						//------------------------------------------------------------
					  // In a better world, I would make a button unpressed if this happened to it,
					  //  but I'm too lazy.
					  public void mouseExited(MouseEvent e) { }
						//------------------------------------------------------------
					  public void mouseEntered(MouseEvent e) { }
					});
		
				toolTipTimer=new Timer(100, new ActionListener() {
					public void actionPerformed(ActionEvent e) { 
						Rectangle testRect = new Rectangle();
						// search to see if the mouse is over a sympol
						int i = sympols.size()-1;
						boolean gotcha = false;
						while ((i>=0) & !gotcha) {
							testRect.setBounds(sympols.get(i).getX(), sympols.get(i).getY(), 80, 80);
							if (testRect.contains(lastMouseX, lastMouseY)) {
								gotcha = true;
								toolTipStopper.start();
								isToolTipActive = true;
								toolTipText = sympols.get(i).getToolTip();
								myClipRect.setBounds(lastMouseX, lastMouseY, 150, 32);
								paintCaller = "";
								repaint();
								myClipRect.setBounds(0, 0, DialogSizeX, WindowSizeY);
							}
							else --i;					
						}
					};			
				});
				toolTipTimer.setRepeats(false);
		
				toolTipStopper=new Timer(3000, new ActionListener() {
					public void actionPerformed(ActionEvent e) { 
						isToolTipActive = false;
						myClipRect.setBounds(lastMouseX, lastMouseY, 150, 32);
						paintCaller = "";
						repaint();
						myClipRect.setBounds(0, 0, DialogSizeX, WindowSizeY);
					};			
				});
				toolTipStopper.setRepeats(false);
				
				attackTimer=new Timer(FrameTime, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int duration = fd.getExpression(iExcursion).getAttack();
						animationFraction = ((float)animationStep)/duration;
						++animationStep;
						if (animationStep>duration) {
							attackTimer.stop();
							sustainTimer.start();
							animationStep = 0;
						}
						paintCaller = "";
						repaint();
					};
				});
				attackTimer.setRepeats(true);
		
				sustainTimer=new Timer(FrameTime, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int duration = fd.getExpression(iExcursion).getSustain();
						++animationStep;
						if (animationStep>duration) {
							sustainTimer.stop();
							decayTimer.start();
							animationStep = fd.getExpression(iExcursion).getDecay();
						}
					};
				});
				sustainTimer.setRepeats(true);
				
				decayTimer=new Timer(FrameTime, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						int duration = fd.getExpression(iExcursion).getDecay();
						animationFraction = ((float)animationStep)/duration;
						--animationStep;
						if (animationStep<0) {
							decayTimer.stop();
							intervalTimer.start();
						}
						paintCaller = "";
						repaint();
					};
				});
				decayTimer.setRepeats(true);
				
				intervalTimer=new Timer(2000, new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						animationStep = 0;
						float bestMoodValue = 0.0f;
						Actor.MoodTrait bestMood = Actor.MoodTrait.Sad_Happy;
						for(Actor.MoodTrait t:Actor.MoodTraits) {
							if (Math.abs(aInterlocutor.get(t)) > bestMoodValue) {
								bestMood = t;
								bestMoodValue = aInterlocutor.get(t);
							}
						}
						String excursionSuffix = String.valueOf(1 + myRandom.nextInt(4));
						String excursionLabel = "";
						if (bestMoodValue > 0) {
							switch (bestMood) {
								case Sad_Happy: { excursionLabel = "happy #"; break;}
								case Fearful_Angry: { excursionLabel = "angry #"; break;}
								case Suspicious_Gullible: { excursionLabel = "sincere #"; break;}
							}
						}
						else {
							switch (bestMood) {
								case Sad_Happy: { excursionLabel = "sad #";  break;}
								case Fearful_Angry: { excursionLabel = "afraid #"; break;}
								case Suspicious_Gullible: { excursionLabel = "lying #"; break;}
							}
						}
						iExcursion = fd.getExpressionIndex(excursionLabel.concat(excursionSuffix));
						excursionMagnitude = Math.abs(bestMoodValue);
//						System.out.println("excursion: "+fd.getExpression(iExcursion).getName()+"  "+String.valueOf(excursionMagnitude));
						attackTimer.start();
					};
				});
				intervalTimer.setRepeats(false);		
				
				addMouseMotionListener(new MouseMotionListener() {
					public void mouseMoved(MouseEvent e) {
						if (isToolTipActive) {
							myClipRect.setBounds(lastMouseX, lastMouseY, 150, 32);
							paintCaller = "mouseMoved";
							repaint();
							myClipRect.setBounds(0, 0, DialogSizeX, WindowSizeY);
						}
						lastMouseX = e.getX();
						lastMouseY = e.getY();
						toolTipTimer.restart();
						isToolTipActive = false;
						toolTipStopper.stop();
					}
					public void mouseDragged(MouseEvent e) { }
					});
				paintCaller = "myPanel";
//				repaint();
			}
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	    public Dimension getPreferredSize() {
	        return new Dimension(DialogSizeX,WindowSizeY);
	    }
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		private void drawSympol(BufferedImage theImage, int x, int y, String tToolTip) {
			windowBufferG2.drawImage(theImage, x, y, transparent, this);
			sympols.add(new ScreenSympol(x, y, tToolTip));
		}
// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		public void paintComponent(Graphics g) {
//			super.paintComponent(g);
			sympols.clear();
			g2 = (Graphics2D)g;
			
			windowBuffer = createImage(WindowSizeX, WindowSizeY);
			windowBufferG2 = (Graphics2D)windowBuffer.getGraphics();

			windowBufferG2.setClip(myClipRect);
			windowBufferG2.setColor(Color.black);
			windowBufferG2.fillRect(0, 0, 960, WindowSizeY);
			windowBufferG2.drawImage(upperBackground[iStage], 200, 14, transparent, this);
			windowBufferG2.drawImage(lowerBackground[iStage], 0, 429, transparent, this);
			windowBufferG2.drawImage(playerBack, 104, 264, transparent, this);
			
			if (topSentence != null) {
				Sentence ts = topSentence.getTheSentence();
				windowBufferG2.drawImage(upperBubble, UpperBubbleX, UpperBubbleY, transparent, this);
				int tw = ts.getIWord(Sentence.Verb);
				Verb topVerb = dk.getVerb(tw);
				for (int i = 0; i<topSentence.getHotWord(); ++i) {
					boolean klugeFlag1 = topVerb.getLabel().equals("attack") & (i==3);
					if (((i>0) | (ts.getIWord(i) != Engine.FATE)) & topVerb.isVisible(i) & !klugeFlag1) {
						int x = TopSentenceX + GlyphSize * topVerb.getSentenceColumn(i);
						int y = TopSentenceY + GlyphSize * topVerb.getSentenceRow(i);
						drawSympol(topSentence.getGlyph(i), x, y, topSentence.getToolTip(i));
						
						WSData wsd = topVerb.getWSData(i);
						drawLinks(wsd, x, y);
					}
				}
				// show interlocutor's face and torso
				interlocutorName = dk.getActor(ts.getIWord(Sentence.Subject)).getLabel();
				fInterlocutor = fd.getFace(interlocutorName);
				if ((interlocutorName!=null) & (fInterlocutor != null)) {
					if (!interlocutorName.equals("Fate") & !interlocutorName.equals("Camiggdo")) {
						windowBufferG2.drawImage(fInterlocutor.getShoulders(), 450, 159, transparent, this);
						fd.drawFace(interlocutorName, iEmotion, emotionMagnitude, iExcursion, excursionMagnitude, animationFraction);
						offscreenFaceG2.setClip(new Rectangle(0,0,300,310));
						Graphics2D g3 = (Graphics2D)fInterlocutor.getMask().getGraphics();
						g3.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
						g3.drawImage(offscreenFace,0,0,transparent,this);
						windowBufferG2.drawImage(fInterlocutor.getMask(), 572, 14, transparent, this);
					}
				}
				else {
					System.out.println("no interlocutor name!");
				}
			}
			if (bottomSentence != null) {
				windowBufferG2.drawImage(lowerBubble, LowerBubbleX, LowerBubbleY, transparent, this);				

				Sentence bs = null;
				try {
					bs = bottomSentence.getTheSentence();
				} catch (Exception e) {
					Utils.displayDebuggingError("FrontEnd.paint(): bs = bottomSentence.getTheSentence()");
				}
				int tw = bs.getIWord(Sentence.Verb);
				drawSympol(bottomSentence.getGlyph(0), BottomSentenceX, BottomSentenceY, "You");
				windowBufferG2.drawImage(rightLink, BottomSentenceX+79, BottomSentenceY+33, transparent, this);
				if (tw >= 0) {
					bottomVerb = dk.getVerb(tw);
					int hotWordIndex = 0;
					if (bottomSentence != null) {
						try {
							hotWordIndex = bottomSentence.getHotWord();
						} catch (Error e) {
							Utils.displayDebuggingError("FrontEnd.paint(): hotWordIndex = bottomSentence.getHotWord();");
						}
						// draw backspace image
						drawSympol(backstep, BottomSentenceX + 4*GlyphSize, BottomSentenceY, "go back one word");	
						if ((isMouseDown) && (clickedColumn == 4) && (clickedRow == 0))
							windowBufferG2.drawImage(darkener,BottomSentenceX + 4*GlyphSize, BottomSentenceY, transparent, this);				
						if (bottomSentence != null) {
							for (int i = 0; i<bottomSentence.getHotWord(); ++i) {
								boolean klugeFlag1 = bottomVerb.getLabel().equals("defend") & (i==4);
								if (bottomVerb.isVisible(i) & !klugeFlag1) {
									int x = BottomSentenceX + GlyphSize * bottomVerb.getSentenceColumn(i);
									int y = BottomSentenceY + GlyphSize * bottomVerb.getSentenceRow(i);
									drawSympol(bottomSentence.getGlyph(i), x, y, bottomSentence.getToolTip(i));
									if ((isMouseDown) && (i == hotWordIndex) && (clickedColumn == bottomVerb.getSentenceColumn(i)) 
											&& (clickedRow == bottomVerb.getSentenceRow(i)))
										windowBufferG2.drawImage(darkener,x, y, transparent, this);				
									
									WSData wsd = bottomVerb.getWSData(i);
									drawLinks(wsd, x, y);
								}
							}
							// now draw the unknown glyph OR the sentence ender
							if (isSentenceComplete) {
								drawSympol(checkButton, BottomSentenceX + 4*GlyphSize, BottomSentenceY+GlyphSize, "do this!");					
								if ((isMouseDown) && (clickedColumn == 4) && (clickedRow == 1))
									windowBufferG2.drawImage(darkener,BottomSentenceX + 4*GlyphSize, BottomSentenceY+GlyphSize, transparent, this);				
							}
							else {
								if (bottomVerb.isVisible(hotWordIndex)) {
									int x = BottomSentenceX + GlyphSize * bottomVerb.getSentenceColumn(hotWordIndex);
									int y = BottomSentenceY + GlyphSize * bottomVerb.getSentenceRow(hotWordIndex);
									Operator.Type tType = bottomVerb.getWordSocketType(hotWordIndex);
									String fileName = "";
									switch (tType) {
										case Actor: { fileName = "Who"; break; }
										case Verb: { fileName = "Do What"; break; }
										case Prop: { fileName = "What"; break; }
										case Stage: { fileName = "Where"; break; }
										case ActorTrait: { fileName = "Which"; break; }
										case PropTrait: { fileName = "Which"; break; }
										case StageTrait: { fileName = "Which"; break; }
										case MoodTrait: { fileName = "Which"; break; }
										case Quantifier: { fileName = "How Much"; break; }
										case Certainty: { fileName = "How Much"; break; }
										default: { fileName = "How Much"; break; }
									}
									drawSympol(readSympol(fileName), x, y, fileName);
									if ((isMouseDown) && (clickedColumn == bottomVerb.getSentenceColumn(hotWordIndex)) 
											&& (clickedRow == bottomVerb.getSentenceRow(hotWordIndex)))
										windowBufferG2.drawImage(darkener,x, y, transparent, this);	
									
									WSData wsd = bottomVerb.getWSData(hotWordIndex);
									drawLinks(wsd, x, y);
								}
							}
						}
					}
				}
				else { // verb has not yet been specified
					int x = BottomSentenceX + GlyphSize;
					int y = BottomSentenceY ;
					drawSympol(doWhat, x, y, "select a word" );				
					if ((isMouseDown) && (clickedColumn == 1) && (clickedRow == 0))
						windowBufferG2.drawImage(darkener,x, y, transparent, this);				
				}
			}		
/*			
			// paint the character display on the right side panel
			for (int i=2; i<6; ++i) {
				String name = dk.getActor(i).getLabel();
				drawSympol(readSympol(name), CharacterDisplayX+(i-1)*GlyphSize, CharacterDisplayY, name);
				if (i != selectedCharacter) { // lighten unselected characters
					windowBufferG2.drawImage(lightener,CharacterDisplayX+(i-1)*GlyphSize, CharacterDisplayY, transparent, this);									
				}
			}
			if (selectedCharacter>0) {
				if (isMouseDown)
					windowBufferG2.drawImage(darkener,CharacterDisplayX+(selectedCharacter-1)*GlyphSize, CharacterDisplayY, transparent, this);	
				else {
					Actor selectedActor = dk.getActor(selectedCharacter);

					// first draw auragon counts with certainties
					for (int j=0; j<3; ++j) {
						String sympolName = "";
						switch (j) {
							case 0: { sympolName = "Red"; break; }
							case 1: { sympolName = "Green"; break; }
							case 2: { sympolName = "Blue"; break; }
						}
						drawSympol(readSympol(sympolName+"Auragon"), CharacterDisplayX+100+GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, sympolName+" auragon count");
						windowBufferG2.drawImage(leftLink, CharacterDisplayX+85+3*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize+33, transparent, this);

						int iTrait = dk.findActorTraitWord(sympolName+"Auragon"); 
						float auragonCount = dk.getActor(1).getP(dk.getActorTraits().get(iTrait), selectedActor);
						int iCount = (int)bounded2Real(auragonCount)+3;
						drawSympol(numbers[iCount], CharacterDisplayX+100+3*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, numberLabels[iCount]);
						windowBufferG2.drawImage(leftLink, CharacterDisplayX+85+5*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize+33, transparent, this);
						
						float certainty = dk.getActor(1).getU(dk.getActorTraits().get(iTrait), selectedActor);
						int iCertainty = (int)(bounded2Real(certainty)+2.5f);
						if (iCertainty < 0) 
							iCertainty = 0;
						if (iCertainty > 5) 
							iCertainty = 5;
						drawSympol(certainties[iCertainty], CharacterDisplayX+100+5*GlyphSize/2, CharacterDisplayY+100+j*GlyphSize, certaintyLabels[iCertainty]);
//						Utils.displayDebuggingError(auragonCount+"  "+(int)bounded2Real(auragonCount)+"      "+certainty+"  "+iCertainty);
					}

					// next draw P3 values with certainties
					for (int j=0; j<3; ++j) {
						String name = dk.getActor(1).getLabel();
						drawSympol(readSympol(name), CharacterDisplayX+10+GlyphSize, 430+j*GlyphSize, "you");					
						
						String sympolName = "";
						String tip = "";
						switch (j) {
							case 0: { sympolName = "powerful"; tip = "fear of you"; break; }
							case 1: { sympolName = "honest"; tip = "trust in you"; break; }
							case 2: { sympolName = "good"; tip = "affection for you"; break; }
						}
						drawSympol(readSympol(sympolName), CharacterDisplayX+10+2*GlyphSize, CharacterDisplayY+390+j*GlyphSize, tip);
						windowBufferG2.drawImage(leftLink, CharacterDisplayX-5+2*GlyphSize, CharacterDisplayY+390+j*GlyphSize+33, transparent, this);

						int iTrait = dk.findActorTraitWord(sympolName)-2; 
						float p3Value = dk.getActor(1).getP3(iTrait, selectedCharacter, 1);
						int iValue = (int)bounded2Real(p3Value)+3;
						if (iValue < 0) 
							iValue = 0;
						if (iValue > 6) 
							iValue = 6;
//							Utils.displayDebuggingError(sympolName+" "+p3Value+"  iValue: "+iValue);
						drawSympol(numbers[iValue], CharacterDisplayX+10+3*GlyphSize, CharacterDisplayY+390+j*GlyphSize, quantifierLabels[iValue]);
						windowBufferG2.drawImage(leftLink, CharacterDisplayX-5+3*GlyphSize, CharacterDisplayY+390+j*GlyphSize+33, transparent, this);
						
						float certainty = dk.getActor(1).getU3(iTrait, selectedCharacter, 1);
						int iCertainty = (int)bounded2Real(certainty)+3;
						if (iCertainty < 0) 
							iCertainty = 0;
						if (iCertainty > 6) 
							iCertainty = 6;
						drawSympol(certainties[iCertainty], CharacterDisplayX+10+4*GlyphSize, CharacterDisplayY+390+j*GlyphSize, certaintyLabels[iCertainty]);
						windowBufferG2.drawImage(leftLink, CharacterDisplayX-5+4*GlyphSize, CharacterDisplayY+390+j*GlyphSize+33, transparent, this);
//						Utils.displayDebuggingError(auragonCount+"  "+(int)bounded2Real(auragonCount)+"      "+certainty+"  "+iCertainty);
					}
				}
			}
*/				
			// any tooltips overlay everything else
			if (isToolTipActive) {
				windowBufferG2.setStroke(new BasicStroke(1));
				FontMetrics fm = windowBufferG2.getFontMetrics();
				int width = fm.stringWidth(toolTipText) + 10;
				windowBufferG2.setColor(new Color(255,255,205));
				windowBufferG2.fillRect(lastMouseX+10, lastMouseY+10, width, 20);
				windowBufferG2.setColor(Color.black);
				windowBufferG2.drawRect(lastMouseX+10, lastMouseY+10, width, 20);
				windowBufferG2.drawString(toolTipText, lastMouseX + 15, lastMouseY + 24);
			}
			g2.drawImage(windowBuffer, 0, 0, transparent, this);
			myClipRect.setBounds(0, 0, DialogSizeX, WindowSizeY);
		}
// ----------------------------------------------------------------------
		public void drawLinks(WSData wsd, int x, int y) {
			if (wsd.outArrow[WSData.UP]) 
				windowBufferG2.drawImage(upLink, x+32, y-15, transparent, this);
			if (wsd.outArrow[WSData.DOWN]) 
				windowBufferG2.drawImage(downLink, x+32, y+79, transparent, this);
			if (wsd.outArrow[WSData.LEFT]) 
				windowBufferG2.drawImage(leftLink, x-15, y+33, transparent, this);
			if (wsd.outArrow[WSData.RIGHT]) 
				windowBufferG2.drawImage(rightLink, x+80, y+33, transparent, this);
			if (wsd.outArrow[WSData.UPRIGHT]) 
				windowBufferG2.drawImage(upRightLink, x+100, y-20, transparent, this);
			if (wsd.outArrow[WSData.DOWNRIGHT]) 
				windowBufferG2.drawImage(downRightLink, x+100, y+78, transparent, this);
			if (wsd.outArrow[WSData.UPLEFT]) 
				windowBufferG2.drawImage(upLeftLink, x-30, y-30, transparent, this);
			if (wsd.outArrow[WSData.DOWNLEFT]) 
				windowBufferG2.drawImage(downLeftLink, x-13, y+78, transparent, this);
		}

	}
// **********************************************************************
	public void writeHistoryBook() {
		int size = engine.getHistoryBookSize();
		int currentSubject = engine.getHistoryBookPage(size-1).getIWord(0);
		int previousSubject = engine.getHistoryBookPage(size-2).getIWord(0);
		boolean isNewLine = ((previousSubject != 1) | ((previousSubject == 1) & (currentSubject == 1)));
//		historyBookDisplay.draw(size-1, isNewLine);		
	}
// **********************************************************************
	public void getTopSentence(LabeledSentence tLabeledSentence, int tTime, int playerId) {	
		topSentence = new SentenceImage(true, tLabeledSentence);
		topSentence.revise(tLabeledSentence);
		interlocutorName = tLabeledSentence.labels[0];
		iInterlocutor = dk.findActor(interlocutorName);
		aInterlocutor = dk.getActor(iInterlocutor);
		Sentence coreSentence = tLabeledSentence.rawSentence;
		if (!interlocutorName.equals("Fate") & !interlocutorName.equals("Camiggdo")) {
			iStage = coreSentence.getLocation();
			intervalTimer.start();
			fInterlocutor = fd.getFace(interlocutorName);
			bottomSentence = null;
			emotionMagnitude = 1.0f;
			String theExpression = dk.getVerb(coreSentence.getIWord(Sentence.Verb)).getExpression();
			if (theExpression.equals("greet nicely")) {
				String theAdverb = tLabeledSentence.labels[3];
				int theQuantifier = coreSentence.getIWord(4) - 3;
				emotionMagnitude = Math.abs((float)theQuantifier/3.0f);
				
				if (theQuantifier >= 0) {
					if (theAdverb.equals("honest"))
						theExpression = "greet sincerely";
					if (theAdverb.equals("powerful"))
						theExpression = "greet dominantly";						
				}
				else {
					if (theAdverb.equals("good"))
						theExpression = "greet nastily";
					if (theAdverb.equals("honest"))
						theExpression = "greet insincerely";
					if (theAdverb.equals("powerful"))
						theExpression = "greet fearfully";						
				}
			}
			iEmotion = fd.getExpressionIndex(theExpression);
		}
//		else
//			intervalTimer.stop();
	}
// **********************************************************************
	public int getPlayerDone(LabeledSentence tLabeledSentence,int playerId) {
		isSentenceComplete = true;
		bottomSentence = new SentenceImage(false, tLabeledSentence);
		bottomSentence.revise(tLabeledSentence);
		while (!isPlayerDone) {
			try { Thread.sleep(10);	} catch (InterruptedException e) { }
		}
		paintCaller = "getPlayerDone";
		dialogPanel.repaint();
		historyPanel.repaint();
		isPlayerDone = false;
		return iChosenMenuItem;
	}
// **********************************************************************
	public int getPlayerSelection(LabeledSentence tLabeledSentence, ArrayList<MenuElement> menuElements, 
			int wordSocket, int playerId) {
		bottomSentence = new SentenceImage(false, tLabeledSentence);
		bottomSentence.revise(tLabeledSentence, menuElements, wordSocket);
		while (!isGlyphSelected) {
			try { Thread.sleep(10); } catch (InterruptedException e) { }
		}
		paintCaller = "getPlayerSelection";
		dialogPanel.repaint();
		isGlyphSelected = false;
		return (iChosenMenuItem);
	}
// **********************************************************************
	public void writeStorybook(String tSentence) {
		
	}
// **********************************************************************
	public void theEnd() {
		
	}
// **********************************************************************		
	String dumpSentence(Sentence tSentence) {
		String appendedString="";
		String output = "";
		output = output + engine.getCMoments() + " "+dk.getActor(tSentence.getIWord(0)).getLabel();
		int iVerb = tSentence.getIWord(Sentence.Verb);
		if (iVerb >= 0) {
			for (int i=1; (i < Sentence.MaxWordSockets); ++i) {
				int iWord = tSentence.getIWord(i);
				if (iWord>=0) {
					appendedString = " "+dk.getLabelByDataType(dk.getVerb(iVerb).getWordSocketType(i), iWord);
					output=output+appendedString;
				}
			}
		}
		output=output+" @"+dk.getStage(tSentence.getLocation()).getLabel();
		output=output+"  CausalEvent: "+tSentence.getCausalEvent();
		return output;
	}
// **********************************************************************
	int quantifierIndex(float x) {
		int index = (int)(4.0f*x)+3;
		if ((index<0) | (index>6))
			Utils.displayDebuggingError("FrontEnd.quantifierIndex(): quantifier index out of range");
		return index;
	}
// **********************************************************************	 
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
// **********************************************************************
	public final class SentenceImage {
		private JPopupMenu menu;
		boolean isTopSentence;
		private int iHotWord;
		private Sentence theSentence;
		private BufferedImage[] glyphs;
		private String[] toolTips;		
		// ---------------------------------------------------------------------
		SentenceImage(boolean tIsTopSentence, LabeledSentence tEvent) {
			isTopSentence = tIsTopSentence;
			theSentence = tEvent.rawSentence;
			iHotWord = 1;	
			glyphs = new BufferedImage[Sentence.MaxWordSockets];
			toolTips = new String[Sentence.MaxWordSockets];
		}
		// ---------------------------------------------------------------------
		public void revise(final LabeledSentence tEvent) {
			revise(tEvent,null, iHotWord);
		}
		// ---------------------------------------------------------------------
		public void revise(final LabeledSentence tEvent, ArrayList<MenuElement> tMenuElement, 
							final int tWordSocket) {		
			iHotWord = tWordSocket;
			for(int i=iHotWord+1; i < Sentence.MaxWordSockets; i++) {
				glyphs[i] = null;
			}
			
			if (tMenuElement!=null && !tMenuElement.isEmpty()) {
				if (menu==null) {
					menu = new JPopupMenu("?");
					menu.addPopupMenuListener(new PopupMenuListener() {
						public void popupMenuCanceled(PopupMenuEvent e) {
						}
						public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
							// Theoretically this should be done in the canceled event,
							// but the canceled event does not seem to be executed in an applet. 
							SwingUtilities.invokeLater(new Runnable(){
								public void run() {
									SentenceImage.this.requestFocusInWindow();
								}
							});
						}
						public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
							SwingUtilities.invokeLater(new Runnable(){
								public void run() {
									if (menu.getComponentCount()>0) {
										final javax.swing.MenuElement me[] = new javax.swing.MenuElement[2];
										me[0] = menu;
										me[1] = (javax.swing.MenuElement)menu.getComponent(0);
										MenuSelectionManager.defaultManager().setSelectedPath(me);
									}
								}
							});
						}
						
					});
				} else menu.removeAll();
				
				int menuSize = (int)Math.sqrt(tMenuElement.size()) + 1;
				menu.setLayout(new GridLayout(menuSize, menuSize));
				for (int i=0;i<tMenuElement.size();i++) {
					MenuElement me = tMenuElement.get(i);
					final int ind = i;
					final String menuText = me.getLabel();
					JMenuItem localItem = null;
					try {
						localItem = new JMenuItem(new ImageIcon(readSympol(menuText)));
					} catch (Exception e) { 
						Utils.displayDebuggingError("FrontEnd.revise(): Couldn't find image for "+menuText); 
					}
					localItem.setToolTipText(Utils.toHtmlTooltipFormat(Utils.nullifyIfEmpty(menuText)));
					MenuTooltipManager.sharedInstance().registerComponent(localItem);
					menu.add(localItem);
					localItem.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							int iWord;
							if (iHotWord == 1) {
								iWord = dk.findVerb(menuText);
							}
							else {
								Verb verb = dk.getVerb(theSentence.getIWord(Sentence.Verb));
								iWord = dk.findWordByWordType(verb.getWSData(iHotWord).type, menuText);
							}
							tEvent.rawSentence.setIWord(tWordSocket, iWord);
							theSentence.setIWord(tWordSocket, iWord);
							tEvent.labels[iHotWord] = menuText;
							isGlyphSelected = true;
							iChosenMenuItem = ind;
							revise(tEvent);
						}
					});	
				}				
			} else {
				menu = null;
			}
			tEvent.visible[0] = true; // a kluge to correct an error in the original code.
			int i = 0;
			while ((i<Sentence.MaxWordSockets) & (theSentence.getIWord(i)>=0) & (tEvent.labels[i].length()>0)) {
				glyphs[i] = readSympol(tEvent.labels[i]);
				toolTips[i] = tEvent.labels[i];
				++i;
			}
			iHotWord = i;
			if (!isTopSentence) {
				if (iHotWord > Sentence.Verb) {
					Verb bVerb = dk.getVerb(theSentence.getIWord(Sentence.Verb));
					isSentenceComplete = !bVerb.isWordSocketActive(iHotWord) || !bVerb.isVisible(iHotWord);
				}
			}
//			if (isTopSentence) {
//				myClipRect.setBounds(UpperBubbleX, 0, 800, 330);
//			}
//			else  { // we set a larger clip to handle menus
//				myClipRect.setBounds(LowerBubbleX, LowerBubbleY, 550, 300);
//			}
			paintCaller = "revise";
			dialogPanel.repaint();
		}
		// ---------------------------------------------------------------------
		public void decrementHotWord() {
			--iHotWord;
			if (iHotWord == Sentence.Verb)
				theSentence.setIWord(Sentence.Verb, -1);
			if (isSentenceComplete) {
				// must correct if we backspaced from end of sentence
				isSentenceComplete = false;
				isPlayerDone = true;
			}
			iChosenMenuItem = -1;
			isGlyphSelected = true;
		}
		// ---------------------------------------------------------------------
		public BufferedImage getGlyph(int iWordSocket) { return glyphs[iWordSocket]; }
		// ---------------------------------------------------------------------
		public String getToolTip(int iWordSocket) { return toolTips[iWordSocket]; }
		// ---------------------------------------------------------------------
		public int getHotWord() { return iHotWord; }
		// ---------------------------------------------------------------------
		public Sentence getTheSentence() { 
			if (theSentence == null) 
				Utils.displayDebuggingError("FrontEnd.getTheSentence(): theSentence is null");
			return theSentence; }
		// ---------------------------------------------------------------------
		/** Tells if this sentence has any wordsocket selection to undo. */
		/*
		private boolean existsWordSocketsToUndo() {
			for(int i=0;i<hotWordSocket;i++)
				if (hot[i])
					return true;
			return false;
		}
		*/
		// ---------------------------------------------------------------------
		/** Adds a button "." with the given action at the end of the sentence. */
		public JButton putPeriodButton(ActionListener periodPressed){
			JButton b = new JButton(".");
			b.addActionListener(periodPressed);
			b.setToolTipText("Press to confirm your sentence!");
			return b;
		}
		// ---------------------------------------------------------------------
		public void setEnabled(boolean enabled) {
		}
		// ---------------------------------------------------------------------
		public void showMenu(Component invoker, int x, int y, int iWord) {
			if (menu == null)
				Utils.displayDebuggingError("FrontEnd:showMenu(): menu == null");
			if (invoker == null)
				Utils.displayDebuggingError("FrontEnd:showMenu(): invoker == null");
			menu.show(invoker, x, y);
		}
		// ---------------------------------------------------------------------
		public boolean requestFocusInWindow() {
//			if (menu!=null && menu.isVisible())
				return true;		
		}
		// ---------------------------------------------------------------------
	}
// ************************************************************
	public void launch() {
		try { engine.run(); }  catch (InterruptedException e) { e.printStackTrace(); }
	}
// ************************************************************
	public void showFroggerLogger() {
		engine.showFroggerLogger();
	}
// ************************************************************
	private BufferedImage readSympol(String sympolName) {
		return readImage("Sympols/"+sympolName+".png");
	}
// ************************************************************
	// just a file-reading routine
	private BufferedImage readImage(String fileName) {
      BufferedImage bi=null;
      String tName = System.getProperty("user.dir")+"/res/images/"+fileName;
		try {
			bi=ImageIO.read(new File(tName));		
      } catch (Exception e) { 
      	Utils.displayDebuggingError("FrontEnd.readImage(): Cannot find image: "+fileName); }
		return bi;
	}

}