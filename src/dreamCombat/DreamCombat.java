package dreamCombat;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;


public class DreamCombat extends JFrame {
	private static final long serialVersionUID = 1l;
	static final 	Color transparent = new Color(0,0,0,0);	// transparency color
	static final int WindowSize = 500;
	static final int ImageSize = 708;
	static final int ImageOffset = (ImageSize - WindowSize)/2;
	static final int startEyes = 70;
	static final int startOpponentAuragon = 100;
	static final int startPlayerAuragon = 140;	
	static final int startCombat = 180;	
	static final int endCombat = 240;	
	static final int AuragonSize = 250;
	static final int auragonLeft = 125;
	static BufferedImage[] eyes = new BufferedImage[6];
	static BufferedImage[] redAuragon = new BufferedImage[60];
	static BufferedImage[] greenAuragon = new BufferedImage[60];
	static BufferedImage[] blueAuragon = new BufferedImage[60];
	static Timer frameTimer;
	static int shortCounter, longCounter;
	Graphics2D netG2, combatG2;
	BufferedImage netImage, combatImage;
	int cStencils = 21;
	Stencil[] stencil = new Stencil[cStencils];
	int loopLength = 64;
	Graphics2D  g2;
	int timerLength = 30;
	Random r;
	PrimeColor redPC, greenPC, bluePC;
	int sumOverTimes;
	int iSequence, iOpponent, iOpponentAuragon, iPlayerAuragon;
	boolean finished;
	// ************************************************************
	public DreamCombat() { // conventional initialization stuff
		super("DreamCombat 0.80");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(200, 50, WindowSize, WindowSize+20);
		setSize(WindowSize, WindowSize+20);
		setLayout(null);
		setBackground(Color.white);
		shortCounter = 0;
		longCounter = 0;
		r = new Random();
		iSequence = r.nextInt(6);
		
		netImage = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_4BYTE_ABGR);
		netG2 = (Graphics2D)netImage.getGraphics();
		netG2.setColor(Color.black);
		
		combatImage = new BufferedImage(AuragonSize, 10, BufferedImage.TYPE_4BYTE_ABGR);
		combatG2 = (Graphics2D)combatImage.getGraphics();
		combatG2.setComposite(AlphaComposite.getInstance(AlphaComposite.XOR));						
		
		redPC = new PrimeColor(Color.red);
		greenPC = new PrimeColor(Color.green);
		bluePC = new PrimeColor(Color.blue);
		
		int i = 0;
		stencil[i++] = new Stencil("8 stripe thinner");
		stencil[i++] = new Stencil("8 stripe");
		stencil[i++] = new Stencil("center");
		stencil[i++] = new Stencil("circlerings 4");
		stencil[i++] = new Stencil("Concentric growing rings");
		stencil[i++] = new Stencil("flower 8 skinnier");
		stencil[i++] = new Stencil("flower");
		stencil[i++] = new Stencil("hole");
		stencil[i++] = new Stencil("loose spiral 4");
		stencil[i++] = new Stencil("loose spiral 4 flip");
		stencil[i++] = new Stencil("loose spiral 8");
		stencil[i++] = new Stencil("loose spiral 8 flip");
		stencil[i++] = new Stencil("ring");
		stencil[i++] = new Stencil("rose 4 dense thinner");
		stencil[i++] = new Stencil("rose 4 dense");
		stencil[i++] = new Stencil("spiral");
		stencil[i++] = new Stencil("spiral flip");
		stencil[i++] = new Stencil("tight spiral 8");
		stencil[i++] = new Stencil("tight spiral 8 flip");
		stencil[i++] = new Stencil("tighter spiral 4");
		stencil[i++] = new Stencil("tighter spiral 4 flip");
		
				
		eyes[0] = readImage("Eyes/Camiggdo");		
		eyes[1] = readImage("Eyes/Camiggdo");		
		eyes[2] = readImage("Eyes/Koopie");		
		eyes[3] = readImage("Eyes/Skordokott");		
		eyes[4] = readImage("Eyes/Zubi");
		eyes[5] = readImage("Eyes/Subotai");
		
		for (i=0; i< 60; ++i) {
			redAuragon[i] = readImage("RedAuragon/Red_000"+numeration(i));
			greenAuragon[i] = readImage("GreenAuragon/Green_000"+numeration(i));
			blueAuragon[i] = readImage("BlueAuragon/Blue_000"+numeration(i));
		}

		frameTimer=new Timer(timerLength, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				++shortCounter;
				if (shortCounter==loopLength) shortCounter=0;
				++longCounter;
				repaint();
			};
		});
		frameTimer.setRepeats(true);		
	}	
// ************************************************************
	public void launch(int tOpponent, int tPlayerAuragon, int tOpponetAuragon, boolean isPlayerAttacker) {
		finished = false;
		iOpponentAuragon = tOpponetAuragon;
		iPlayerAuragon = tPlayerAuragon;
		iOpponent = tOpponent;
		frameTimer.start();
		setVisible(true);
		
	}
// ************************************************************
	public boolean isFinished() {
		return finished;
	}
// ************************************************************
	private String numeration(int i) {
		String result = ((Integer)i).toString();
		if (i<10)
			result = "0"+result;
		return result;
	}
// ************************************************************
	public static AffineTransform createFlipTransform(boolean verticalFlip, int imageWid, int imageHt) {
		AffineTransform at = new AffineTransform();
		if (verticalFlip) {
			at = new AffineTransform(new double[] {1.0,0.0,0.0,-1.0});
			at.translate(0.0, -imageHt);
		}
		else {
			at = new AffineTransform(new double[] {-1.0,0.0,0.0,1.0});
			at.translate(-imageWid, 0.0);
		}
		return at;
	}
// ************************************************************
	private class PrimeColor {
		Color theColor;
		Graphics2D g2D;
		BufferedImage image;
		int iStencil, jStencil, iRate, jRate;
		boolean iClockwise, jClockwise;
		public PrimeColor(Color tColor) {
			theColor = tColor;
			image = new BufferedImage(ImageSize, ImageSize, BufferedImage.TYPE_4BYTE_ABGR);
			g2D = (Graphics2D)image.getGraphics();
			resetStencils();
		}
		void resetStencils() {
			iStencil = r.nextInt(cStencils);
			do { jStencil = r.nextInt(cStencils); } while (jStencil == iStencil);
			iRate = (int)Math.pow(2, (double)(r.nextInt(2)));
			jRate = (int)Math.pow(2, (double)(r.nextInt(2)));
			iClockwise = r.nextBoolean();
		}
		void setUpPaint() {
			g2D.setColor(theColor);
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
			g2D.fillRect(0, 0, ImageSize, ImageSize);
			g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
		}
		void drawStencils() {
			AffineTransform tx = new AffineTransform();
			double rotAngle = iRate * 2.0*Math.PI*(double)shortCounter/(double)loopLength;
			if (!iClockwise) rotAngle = -rotAngle;
			tx.setToRotation(rotAngle, (double)WindowSize/2, (double)WindowSize/2);
			g2D.setTransform(tx);
			g2D.drawImage(stencil[iStencil].image, -ImageOffset, -ImageOffset, null);
			rotAngle = jRate * 2.0*Math.PI*(double)shortCounter/(double)loopLength;
			if (!jClockwise) rotAngle = -rotAngle;
			tx.setToRotation(rotAngle, (double)WindowSize/2, (double)WindowSize/2);
			g2D.setTransform(tx);
			g2D.drawImage(stencil[jStencil].image, -ImageOffset, -ImageOffset, null);
			g2D.setTransform(new AffineTransform());
		}

	}
// ************************************************************
	private class Stencil {
		BufferedImage image;
		String label;
		public Stencil(String tLabel) {
			label = tLabel;
			image = readImage(label);
		}
	}
// ************************************************************
	public void paint(Graphics g) {
		long startTime = System.currentTimeMillis();
		g2 = (Graphics2D)g;
		
			
		redPC.setUpPaint();
		greenPC.setUpPaint();
		bluePC.setUpPaint();
		
		netG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
		netG2.fillRect(0, 0, ImageSize, ImageSize);
		netG2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

		redPC.drawStencils();
		greenPC.drawStencils();
		bluePC.drawStencils();

		/*
		switch (iSequence) {
			case 0: {
				redPC.drawStencils();
				greenPC.drawStencils();
				bluePC.drawStencils();
				break;
			}
			case 1: {
				redPC.drawStencils();
				bluePC.drawStencils();
				greenPC.drawStencils();
				break;
			}
			case 2: {
				greenPC.drawStencils();
				redPC.drawStencils();
				bluePC.drawStencils();
				break;
			}
			case 3: {
				greenPC.drawStencils();
				bluePC.drawStencils();
				redPC.drawStencils();
				break;
			}
			case 4: {
				bluePC.drawStencils();
				redPC.drawStencils();
				greenPC.drawStencils();
				break;
			}
			case 5: {
				bluePC.drawStencils();
				greenPC.drawStencils();
				redPC.drawStencils();
				break;
			}
		}
		++iSequence;
		if (iSequence>5) iSequence = 0;
		*/
				
		
		switch (iSequence) {
			case 0: {
				netG2.drawImage(redPC.image, 0, 0, null);
				netG2.drawImage(greenPC.image, 0, 0, null);
				netG2.drawImage(bluePC.image, 0, 0, null);
				break;
			}
			case 1: {
				netG2.drawImage(redPC.image, 0, 0, null);
				netG2.drawImage(bluePC.image, 0, 0, null);
				netG2.drawImage(greenPC.image, 0, 0, null);
				break;
			}
			case 2: {
				netG2.drawImage(greenPC.image, 0, 0, null);
				netG2.drawImage(redPC.image, 0, 0, null);
				netG2.drawImage(bluePC.image, 0, 0, null);
				break;
			}
			case 3: {
				netG2.drawImage(greenPC.image, 0, 0, null);
				netG2.drawImage(bluePC.image, 0, 0, null);
				netG2.drawImage(redPC.image, 0, 0, null);
				break;
			}
			case 4: {
				netG2.drawImage(bluePC.image, 0, 0, null);
				netG2.drawImage(redPC.image, 0, 0, null);
				netG2.drawImage(greenPC.image, 0, 0, null);
				break;
			}
			case 5: {
				netG2.drawImage(bluePC.image, 0, 0, null);
				netG2.drawImage(greenPC.image, 0, 0, null);
				netG2.drawImage(redPC.image, 0, 0, null);
				break;
			}
		}
		++iSequence;
		if (iSequence>5) iSequence = 0;
		

		
		// draw semi-transparent images together
		netG2.drawImage(redPC.image, 0, 0, null);
		netG2.drawImage(greenPC.image, 0, 0, null);
		netG2.drawImage(bluePC.image, 0, 0, null);
		if (longCounter>startEyes) {	
			if (longCounter<startOpponentAuragon) {
				float magnifier = (float)(5.0f/(float)(startOpponentAuragon + 5 - longCounter));
				float z = (float)(longCounter-startEyes);
				int x = (int)(WindowSize - 50 - z*z*z/96.4f);
				int y = (int)(WindowSize - 20 - z*z/2.2f);
				int x1 = (int)(magnifier*(float)eyes[iOpponent].getWidth());
				int y1 = (int)(magnifier*(float)eyes[iOpponent].getHeight());
				netG2.drawImage(eyes[iOpponent], x, y, x1, y1, this);
			}
			else { // longCounter > startEyes
				netG2.drawImage(eyes[iOpponent], WindowSize/2-eyes[iOpponent].getWidth()/2, 40, this);
				if (longCounter < startPlayerAuragon) {
					int shortCounter = longCounter - startOpponentAuragon;
					if (shortCounter>59) shortCounter -= 59;
					// attacker auragon fades in
					BufferedImage displayedAuragon=null;
					switch (iOpponentAuragon) {
						case 0: { displayedAuragon = greenAuragon[shortCounter]; break; }
						case 1: { displayedAuragon = blueAuragon[shortCounter]; break; }
						case 2: { displayedAuragon = redAuragon[shortCounter]; break; }
					}
					AlphaComposite savedAC = (AlphaComposite)netG2.getComposite();
					netG2.setComposite(savedAC.derive(((float)(longCounter-startOpponentAuragon)/60.0f)));
					
					netG2.drawImage(displayedAuragon, auragonLeft, 10, AuragonSize, AuragonSize, this);
					netG2.setComposite(savedAC);

				}
				else { // we have completed fading in the opponent auragon
					// first display the opponent auragon, still animating
					BufferedImage upperAuragon = null;
					int shortCounter = longCounter - startOpponentAuragon;
					while (shortCounter>59) 
						shortCounter -= 59;
					switch (iOpponentAuragon) {
						case 0: { upperAuragon = greenAuragon[shortCounter]; break; }
						case 1: { upperAuragon = blueAuragon[shortCounter]; break; }
						case 2: { upperAuragon = redAuragon[shortCounter]; break; }
					}
					// begin fading in the player's auragon
					shortCounter = longCounter - startPlayerAuragon;
					while (shortCounter>59) 
						shortCounter -= 59;
					BufferedImage lowerAuragon = null;

					switch (iPlayerAuragon) {
						case 0: { lowerAuragon = greenAuragon[shortCounter]; break; }
						case 1: { lowerAuragon = blueAuragon[shortCounter]; break; }
						case 2: { lowerAuragon = redAuragon[shortCounter]; break; }
					}
					AlphaComposite savedAC = (AlphaComposite)netG2.getComposite();
					if (longCounter<startCombat) {
						// fade in player auragon
						netG2.setComposite(savedAC.derive(((float)(longCounter-startPlayerAuragon)/60.0f)));						
						netG2.drawImage(lowerAuragon, auragonLeft, 250, AuragonSize, AuragonSize, this);
						netG2.setComposite(savedAC);
						netG2.drawImage(upperAuragon, auragonLeft, 0, AuragonSize, AuragonSize, this);
					}
					else { // time to begin combat; both auragons must move towards the center
						if (longCounter<endCombat) {
							// determine which auragon wins
							boolean tie = (iPlayerAuragon == iOpponentAuragon);
							boolean playerWins = ((iPlayerAuragon == 2) & (iOpponentAuragon == 0))
									| ((iPlayerAuragon == 1) & (iOpponentAuragon == 2))
									| ((iPlayerAuragon == 0) & (iOpponentAuragon == 1));
							boolean opponentWins = ((iPlayerAuragon == 2) & (iOpponentAuragon == 1))
									| ((iPlayerAuragon == 1) & (iOpponentAuragon == 0))
									| ((iPlayerAuragon == 0) & (iOpponentAuragon == 2));
							int topY = 4*(longCounter - startCombat);
							int bottomY = WindowSize - topY;
							if (playerWins & (topY>AuragonSize)) topY = AuragonSize;
							if (opponentWins & (topY>AuragonSize/2)) topY = AuragonSize/2;
							
							if (playerWins) {
								int by = bottomY - AuragonSize;
								if (by<AuragonSize/2)
									by = AuragonSize/2;
								netG2.drawImage(upperAuragon, auragonLeft, topY, auragonLeft+AuragonSize, AuragonSize, 0, 0, AuragonSize, AuragonSize-topY, transparent, this);
								netG2.drawImage(lowerAuragon, auragonLeft, by, AuragonSize, AuragonSize, this);
							}
							if (opponentWins) {
								netG2.drawImage(lowerAuragon, auragonLeft, AuragonSize, auragonLeft+AuragonSize, bottomY, 0, WindowSize-bottomY, AuragonSize, AuragonSize, transparent, this);
								netG2.drawImage(upperAuragon, auragonLeft, topY, AuragonSize, AuragonSize, this);
								System.out.println("opponent wins: "+topY+"   "+bottomY);
							}
							if (tie) {
								netG2.drawImage(upperAuragon, auragonLeft, topY, auragonLeft+AuragonSize, AuragonSize, 0, 0, AuragonSize, AuragonSize-topY, transparent, this);
								netG2.drawImage(lowerAuragon, auragonLeft, AuragonSize, auragonLeft+AuragonSize, WindowSize-topY, 0, topY, AuragonSize, AuragonSize, transparent, this);
//								combatG2.drawImage(upperAuragon, 0, 0, AuragonSize, 5, 0, AuragonSize-topY-5, AuragonSize, AuragonSize-topY, transparent, this);
//								netG2.drawImage(combatImage, auragonLeft, AuragonSize-5, auragonLeft+AuragonSize,AuragonSize, 0, 0, AuragonSize, 5, transparent, this);
							}
						}
						else { // time to end this display
							frameTimer.stop();
							setVisible(false);
							finished = true;
						}
					}				
				}
			}
		}
		
		// put result onscreen
		g2.drawImage(netImage, 0, 20, null);
		
		long drawTime = System.currentTimeMillis() - startTime;
		sumOverTimes += drawTime;
		if (shortCounter == loopLength-1) {
			sumOverTimes = 0;
		}
	}
// ************************************************************
	// just a file-reading routine
	static BufferedImage readImage(String fileName) {
      BufferedImage bi=null;
		try {
			String fullName=System.getProperty("user.dir")+"/res/DreamCombatImages/"+fileName+".png";
			File tFile = new File(fullName);
			bi=ImageIO.read(tFile);		
      } catch (IOException e) 
			{ 
      	System.out.println("can't find image "+fileName); }
		return bi;
	}

}