package FaceDisplay;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
/*
 * This is the basic data structure; 
 * it represents a graphic object superimposed on the background face.
 * It consists of a set of points in 2D space.
 * The simple linear features are drawn with drawLine();
 *   A few non-linear features each have their own unique drawing method.
 *   Some are just lines, some are closed polygons, and one (Iris) is a circle
 *   
 * Each feature has a left version and a right version.
 * 
 * Nomenclature: 
 *    "editing face" or "big face" denotes the large face used for editing
 *    "final face" or "small face" denotes the face that will displayed in applications.
 * 
 * On my personal style:
 * My primary motivation is to make it easy for the eye to quickly find
 *    things. That is expressed in several principles:
 *    1. Big, clear dividers between methods (the lines of asterisks)
 *    2. Compress no-brainer multi-line statements into a single line:
 *       a. if-statements with a single result and no else-clause
 *       b. getters and setters. Nobody reads them.
 *    3. Put the opening curly bracket for a block at the end of its parent line
 *    4. No long horizontal lines. One way or another, break them up into short lines.
 */
public class Feature {
// ************************************************************
		public static final int MaxFeatureSize = 12; // max number of points
		public static Polygon rightEyeOutline, leftEyeOutline; // used for clipping the pupils
		static final int BigCenterLine = 360; // change this to relocate the editing face
//		static final float pupilSize = 20;

		String label; 
		int size; // number of points used in this feature
		int bx, by; // handy shorthand for 'baseX' and 'baseY'
		static int originX, originY;
		public static Expression nullEmotion, nullExcursion;
		private Color transparent=new Color(0,0,0,0);
		
		// these next four constitute standard coordinates for the right and left versions
		int[] rightX = new int[MaxFeatureSize];
		int[] rightY = new int[MaxFeatureSize];
		int[] leftX = new int[MaxFeatureSize];
		int[] leftY = new int[MaxFeatureSize];

		// These give the coordinates where the point is actually drawn in the window.
		int[] rightBigX = new int[MaxFeatureSize];
		int[] rightBigY = new int[MaxFeatureSize];
		int[] leftBigX = new int[MaxFeatureSize];
		int[] leftBigY = new int[MaxFeatureSize];
		
		// These are the coordinates for the final display.
		public int[] rightSmallX = new int[MaxFeatureSize];
		int[] rightSmallY = new int[MaxFeatureSize];
		int[] leftSmallX = new int[MaxFeatureSize];
		int[] leftSmallY = new int[MaxFeatureSize];
		
		// feature line thickness, not actor line thickness. Percentage from 0 to 300
		int[] rightLineThickness = new int[MaxFeatureSize];
		int[] leftLineThickness = new int[MaxFeatureSize];
		
		// used only with excursions; specifies degree of participation the excursion
		double participation; 
		
		static Graphics2D bufferG2;
		static ActorFace who; // the actor being drawn
		
		// These represent the innermost point on the eyes, important for synching other features.
		static int rightBigLacrimalX, rightBigLacrimalY, leftBigLacrimalX, leftBigLacrimalY;
		static int rightSmallLacrimalX, leftSmallLacrimalX;
		static double[] lineThickness = {0.25, 0.50, 0.75, 1.0, 2.0, 3.0};
// ************************************************************
		public Feature() {
			participation = 1.0;
			for (int i=0; (i<MaxFeatureSize); ++i) {
				rightX[i]=0;
				rightY[i]=0;
				leftX[i]=0;
				leftY[i]=0;
				size = 0;
				rightLineThickness[i] = 3;
				leftLineThickness[i] = 3;
			}
		}		
// ************************************************************
		public Feature clone() {
			Feature newFeature = new Feature();
			newFeature.participation = participation;
			newFeature.label = label;
			newFeature.size = size;
			newFeature.bx = bx;
			newFeature.by = by;
			for (int i=0; (i<MaxFeatureSize); ++i) {
				newFeature.rightX[i] = rightX[i];
				newFeature.rightY[i] = rightY[i];
				newFeature.leftX[i] = leftX[i];
				newFeature.leftY[i] = leftY[i];
				
				newFeature.rightBigX[i] = rightBigX[i];
				newFeature.rightBigY[i] = rightBigY[i];
				newFeature.leftBigX[i] = leftBigX[i];
				newFeature.leftBigY[i] = leftBigY[i];
				
				newFeature.rightSmallX[i] = rightSmallX[i];
				newFeature.rightSmallY[i] = rightSmallY[i];
				newFeature.leftSmallX[i] = leftSmallX[i];
				newFeature.leftSmallY[i] = leftSmallY[i];
			}
			return newFeature;
		}
// ************************************************************
// ****************** Getters and Setters *********************
// ************************************************************
		public void setUpStatics(Expression tEmotion, Expression tExcursion) {
			nullEmotion = tEmotion;
			nullExcursion = tExcursion;
		}
// ************************************************************
		public String getLabel() { return label; }
// ************************************************************
		public int getSize() { return size; }
// ************************************************************
		public void setSize(int tSize) { size = tSize; }
// ************************************************************
		public void setLabel(String tLabel) { label = tLabel; }
// ************************************************************
		public static void setUpFeatures(Graphics2D ag2, ActorFace aWho) {
			bufferG2 = ag2;
			who = aWho;
		}
// ************************************************************
		public int getRightX(int j) { return rightX[j]; }
// ************************************************************
		public void setRightX(int j, int newValue) { 
			double a = ActorFace.featureScale; // shorten to make for easier reading
			double b = 0.5/ActorFace.featureScale;  // correction for round-off error
			rightX[j] = newValue;
			rightSmallX[j] = (rightX[j]>=0) ? (int)((rightX[j]+b)*a) : (int)((rightX[j]-b)*a);
		}
// ************************************************************
		public int getRightY(int j) { return rightY[j]; }
// ************************************************************
		public void setRightY(int j, int newValue) {
			rightY[j] = newValue; 
			double a = ActorFace.featureScale; // shorten to make for easier reading
			double b = 0.5/ActorFace.featureScale;  // correction for round-off error
			rightSmallY[j] = (int)((rightY[j]+b)*a);
		}
// ************************************************************
		public int getLeftX(int j) { return leftX[j]; }
// ************************************************************
		public void setLeftX(int j, int newValue) {
			leftX[j] = newValue; 
			double a = ActorFace.featureScale; // shorten to make for easier reading
			double b = 0.5/ActorFace.featureScale;  // correction for round-off error
			leftSmallX[j] = (int)((leftX[j]+b)*a);
		}
// ************************************************************
		public int getLeftY(int j) { return leftY[j]; }
// ************************************************************
		public void setLeftY(int j, int newValue) {
			leftY[j] = newValue; 
			double a = ActorFace.featureScale; // shorten to make for easier reading
			double b = 0.5/ActorFace.featureScale;  // correction for round-off error
			leftSmallY[j] = (int)((leftY[j]+b)*a);
		}		
// ************************************************************
	public int getRightBigX(int j) { return rightBigX[j]; }
// ************************************************************
	public void setRightBigX(int j, int newValue) { rightBigX[j] = newValue; }
// ************************************************************
	public int getRightBigY(int j) { return rightBigY[j]; }
// ************************************************************
	public void setRightBigY(int j, int newValue) { rightBigY[j] = newValue; }
// ************************************************************
	public int getLeftBigX(int j) { return leftBigX[j]; }
// ************************************************************
	public void setLeftBigX(int j, int newValue) { leftBigX[j] = newValue; }
// ************************************************************
	public int getLeftBigY(int j) { return leftBigY[j]; }
// ************************************************************
	public void setLeftBigY(int j, int newValue) { leftBigY[j] = newValue; }
// ************************************************************
	public int getRightLineThickness(int j) { return rightLineThickness[j]; }
// ************************************************************
	public void setRightLineThickness(int j, int newValue) { rightLineThickness[j] = newValue; }
// ************************************************************
	public int getLeftLineThickness(int j) { return leftLineThickness[j]; }
// ************************************************************
	public void setLeftLineThickness(int j, int newValue) { leftLineThickness[j] = newValue; }
// ************************************************************
	public static void setOrigin(int x, int y) { originX = x; originY = y; }
// ************************************************************
	public double getParticipation() { return participation; }
// ************************************************************
	public void setParticipation(double newValue) { participation = newValue; }
// ************************************************************
	public static double getLineThickness(int i) { return lineThickness[i]; }
// ************************************************************
// **************** End Getters and Setters *******************
// ************************************************************
		
		
// ************************************************************
	public void drawSmall(int index) {
		// bx and by mean "BaseX" and "BaseY". They are global in scope.
		bx = originX  + who.getSmallFace().getWidth()/2;
		by =  originY + who.getSmallBaseY() + who.getSmallVerticalOffsets(index);
		switch (index) {
			case Expression.Eye: { drawSmallEyes(); break; }
			case Expression.Iris: { drawSmallIris(); break; }
			case Expression.OrbLine: { drawSmallLine(Expression.OrbLine, who.getEyeSeparation(), who.getEyeSize()); break; }
			case Expression.BrowLine: { drawSmallLine(Expression.BrowLine, 0, 1.0); break; }
			case Expression.Eyebrow: { drawSmallLine(Expression.Eyebrow, who.getEyeSeparation(), who.getEyeSize()); break; }
			case Expression.Nose: { drawSmallNose(); break; }
			case Expression.Jowl: { drawSmallLine(Expression.Jowl, who.getJowlSeparation(), 1.0); break; }
			case Expression.UpperLip: { drawSmallUpperLip(); break; }
			case Expression.LowerLip: { drawSmallLowerLip(); break; }
		}
//		bufferG2.setClip(0,0,FaceEditor.WindowWidth-200,FaceEditor.WindowHeight-200);
	}
// ************************************************************
	public void drawBig(int index) {
		switch (index) {
			case Expression.Eye: { drawBigEyes(); break; }
			case Expression.Iris: { drawBigIris(); break; }
			case Expression.OrbLine: { drawBigLine(Expression.OrbLine); break; }
			case Expression.BrowLine: { drawBigLine(Expression.BrowLine); break; }
			case Expression.Eyebrow: { drawBigLine(Expression.Eyebrow); break; }
			case Expression.Nose: { drawBigNose(); break; }
			case Expression.Jowl: { drawBigLine(Expression.Jowl); break; }
			case Expression.UpperLip: { drawBigUpperLip(); break; }
			case Expression.LowerLip: { drawBigLowerLip(); break; }
		}
	}
// ************************************************************
		public void drawSmallEyes() {
			/*
			 * This is pretty straightforward. First we draw the inner outline,
			 *    then, for the small display only, we draw an outer outline.
			 *    We then fill in the gap between the inner and outer outlines.
			 */
			int x, y; // handy-dandies
			int[] x1 = new int[12];
			int[] y1 = new int[12];
			int dx, dy;
			
			// left side outline		
			leftEyeOutline = new Polygon();

			
			leftEyeOutline.reset();
			dx = bx - who.getEyeSeparation() - leftSmallX[0];
			dy = by + leftSmallY[0];
			leftSmallLacrimalX = dx-10; // the "10" is a fudge factor that I cannot justify
			leftEyeOutline.addPoint(dx,dy);
			x1[0]=dx; y1[0]=dy;
			for (int j=1; (j<size); ++j) {
				x = dx - (int)(who.getEyeSize()*(float)(leftSmallX[j]-leftSmallX[0]));
				y = dy + (int)(who.getEyeSize()*(float)(leftSmallY[j]-leftSmallY[0]));
				x1[j]=x; y1[j]=y;
				leftEyeOutline.addPoint(x,y);
			}
			leftEyeOutline.addPoint(dx,dy);
			x1[size]=dx; y1[size]=dy;

			bufferG2.setColor(Color.black);
			bufferG2.setStroke(new BasicStroke(7.0f));
			bufferG2.drawPolyline(x1, y1, size+1);
			bufferG2.setStroke(new BasicStroke(1.0f));
			bufferG2.setColor(Color.white);
			bufferG2.fillPolygon(leftEyeOutline);
			
			// right side outline
			rightEyeOutline = new Polygon();
			dx = bx + who.getEyeSeparation() + rightSmallX[0];
			dy = by + rightSmallY[0];
			rightSmallLacrimalX = dx-6; // the '6' is a fudge factor that I cannot justify
			rightEyeOutline.addPoint(dx,dy);
			x1[0]=dx; y1[0]=dy;
			for (int j=1; (j<size); ++j) {
				x = dx + (int)(who.getEyeSize()*(float)(rightSmallX[j]-rightSmallX[0]));
				y = dy + (int)(who.getEyeSize()*(float)(rightSmallY[j]-rightSmallY[0]));
				x1[j]=x; y1[j]=y;
				rightEyeOutline.addPoint(x,y);
			}
			rightEyeOutline.addPoint(dx,dy);
			x1[size]=dx; y1[size]=dy;
		
			bufferG2.setColor(Color.black);
			bufferG2.setStroke(new BasicStroke(7.0f));
			bufferG2.drawPolyline(x1, y1, size+1);
			bufferG2.setStroke(new BasicStroke(1.0f));
			bufferG2.setColor(Color.white);
			bufferG2.fillPolygon(rightEyeOutline);
						
//			bufferG2.setColor(Color.black);
			
		}
// ************************************************************
		public void drawBigEyes() {
			// right side outline
			rightBigX[0] = BigCenterLine + rightX[0] - 4;
			rightBigY[0] = rightY[0] - 4;
			rightBigLacrimalX = rightBigX[0];
			rightBigLacrimalY = rightBigY[0];
			for (int j=1; (j<size); ++j) {
				rightBigX[j] = rightBigLacrimalX + rightX[j]-rightX[0];
				rightBigY[j] = rightBigLacrimalY + rightY[j]-rightY[0];
			}			
			// left side outline		
			leftBigX[0] = BigCenterLine - leftX[0] - 4;
			leftBigY[0] = leftY[0] - 4;
			leftBigLacrimalX = leftBigX[0];
			leftBigLacrimalY = leftBigY[0];
			for (int j=1; (j<size); ++j) {
				leftBigX[j] = leftBigLacrimalX - leftX[j] + leftX[0];
				leftBigY[j] = leftBigLacrimalY + leftY[j]-leftY[0];
			}
		}
// ************************************************************
	public void drawSmallIris() {
		int ex, x, y;
		int dWidth = who.getSmallIris().getWidth()/2;
		int dHeight = who.getSmallIris().getHeight()/2;	
		
		// right side
		ex = bx + who.getEyeSeparation() + rightSmallX[0] - dWidth;
		x = rightSmallLacrimalX + (int)(who.getEyeSize()*(float)(ex-rightSmallLacrimalX));
		y = by + rightSmallY[0] - dHeight + 1; // I cannot justify the value '1'
		bufferG2.setClip(rightEyeOutline);
		bufferG2.drawImage(who.getSmallIris(), x, y, transparent, who);		

		// left side
		ex = bx - who.getEyeSeparation() - leftSmallX[0] - dWidth;
		x = leftSmallLacrimalX + (int)(who.getEyeSize()*(float)(ex-leftSmallLacrimalX));
		y = by + leftSmallY[0] - dHeight + 1; // I cannot justify the value '1'
		bufferG2.setClip(leftEyeOutline);
		bufferG2.drawImage(who.getSmallIris(), x, y, transparent, who);		

/*
		int centerX = x+size/2;
		int centerY = y+size/2;
		int a;
		for (int i=0; (i<20); ++i) {
			bufferG2.setColor(new Color(96, 0, 0));
			a = r.nextInt(360);
			bufferG2.fillArc(centerX, centerY, size, size, a, 10);
			a = r.nextInt(360);
			bufferG2.setColor(new Color(0, 140, 0));			
			bufferG2.fillArc(centerX, centerY, size, size, a, 10);
			a = r.nextInt(360);
			bufferG2.setColor(new Color(0, 0, 220));			
			bufferG2.fillArc(centerX, centerY, size, size, a, 10);
		}
		bufferG2.setColor(Color.black);	
		bufferG2.fillOval(centerX+size/4, centerY+size/4, size/2, size/2);

		ex = bx - who.getEyeSeparation() - leftSmallX[0] - size;
		x = leftSmallLacrimalX + (int)(who.getEyeSize()*(float)(ex-leftSmallLacrimalX));

		y = by + leftSmallY[0] - size + 1; // I cannot just the value '1'
		bufferG2.setClip(leftEyeOutline);
	//	radii
		centerX = x+size/2;
		centerY = y+size/2;
		for (int i=0; (i<20); ++i) {
			g2.setColor(new Color(96, 0, 0));			
			g2.fillArc(centerX, centerY, size, size, r.nextInt(360), 10);
			g2.setColor(new Color(0, 140, 0));			
			g2.fillArc(centerX, centerY, size, size, r.nextInt(360), 10);
			g2.setColor(new Color(0, 0, 220));			
			g2.fillArc(centerX, centerY, size, size, r.nextInt(360), 10);
		}
		g2.setClip(0,0,FaceEditor.WindowWidth,FaceEditor.WindowWidth);
		g2.setColor(Color.black);	
		g2.fillOval(centerX+size/4, centerY+size/4, size/2, size/2);
*/
/*  circle
		centerX = x+size/2;
		centerY = y+size/2;
		for (int i=0; (i<20); ++i) {
			g2.setColor(new Color(96, 0, 0));	
			int t = r.nextInt(size+1);
			g2.drawOval(centerX+size/2-t/2, centerY+size/2-t/2, t, t);
			g2.setColor(new Color(0, 140, 0));			
			t = r.nextInt(size+1);
			g2.drawOval(centerX+size/2-t/2, centerY+size/2-t/2, t, t);
			g2.setColor(new Color(0, 0, 220));			
			t = r.nextInt(size+1);
			g2.drawOval(centerX+size/2-t/2, centerY+size/2-t/2, t, t);
		}
		g2.setClip(0,0,FaceEditor.WindowWidth,FaceEditor.WindowWidth);
		g2.setColor(Color.black);	
		g2.fillOval(centerX+size/4, centerY+size/4, size/2, size/2);

		// lines
		centerX = x+size/2;
		centerY = y+size/2;
		for (int i=0; (i<20); ++i) {
			bufferG2.setColor(new Color(96, 0, 0));
			a = r.nextInt(360);
			bufferG2.fillArc(centerX, centerY, size, size, a, 10);
			a = r.nextInt(360);
			bufferG2.setColor(new Color(0, 140, 0));			
			bufferG2.fillArc(centerX, centerY, size, size, a, 10);
			a = r.nextInt(360);
			bufferG2.setColor(new Color(0, 0, 220));			
			bufferG2.fillArc(centerX, centerY, size, size, a, 10);
		}
		bufferG2.setColor(Color.black);	
		bufferG2.fillOval(centerX+size/4, centerY+size/4, size/2, size/2);
						
				
		bufferG2.drawImage(who.getSmallIris(), x, y, FaceEditor.transparent, who);		

		AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);
		bufferG2.setComposite(ac);						
//		bufferG2.drawImage(who.getSmallIris(), x, y, FaceEditor.transparent, who);		
		
		
		ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
		bufferG2.setComposite(ac);		
//		bufferG2.setClip(0,0,FaceEditor.WindowWidth,FaceEditor.WindowWidth);
/*					
		// left side
		ex = bx - who.getEyeSeparation() - leftSmallX[0] - dWidth;
		x = leftSmallLacrimalX + (int)(who.getEyeSize()*(float)(ex-leftSmallLacrimalX));
		y = by + leftSmallY[0] - dHeight + 1; // I cannot justify the value '1'
		bufferG2.setClip(leftEyeOutline);
		bufferG2.drawImage(who.getSmallIris(),x, y,FaceEditor.transparent,who);
		bufferG2.setClip(0,0,FaceEditor.WindowWidth,FaceEditor.WindowWidth);	
*/									
	}
// ************************************************************
	public void drawBigIris() {
		int x, y, dWidth, dHeight;
		dWidth = ActorFace.getStandardIris().getWidth()/2;
		dHeight = ActorFace.getStandardIris().getHeight()/2;

		// right side
		x = BigCenterLine + rightX[0] - dWidth; 
		y = rightY[0] - dHeight;
		rightBigX[0] = x + dWidth - 4;
		rightBigY[0] = y - 4;
		bufferG2.drawImage(ActorFace.getStandardIris(), x, y, transparent, who);
					
		// left side
		x = BigCenterLine - leftX[0] - dWidth;
		y = leftY[0] - dHeight;
		leftBigX[0] = x + dWidth - 4;
		leftBigY[0] = y - 4;		
		bufferG2.drawImage(ActorFace.getStandardIris(),x, y,transparent,who);
	}
// ************************************************************
	public void drawSmallUpperLip() {
		/*
		 * The upper lip, lower lip, and the dimple both present tricky problems because
		 *    they cross the centerline. Therefore, the left side and
		 *    the right side must be drawn together as a single unit.
		 *    Note that this method fills the upper lip; it does not draw the lines. Drawing the
		 *       lines for the editing-scale image is done by FacedEditor.drawControlRects().
		 */
		int x, y;
		Polygon upperLip = new Polygon();

		// right side
		for (int j=0; (j<size); ++j) {
			x = bx + (int)(who.getJawWidth() * rightSmallX[j]);
			y = by + rightSmallY[j];
			upperLip.addPoint(x,y);
		}
		// left side
		for (int j=size-1; (j>=0); --j) {
			x = bx - (int)(who.getJawWidth() * leftSmallX[j]);
			y = by + leftSmallY[j];
			upperLip.addPoint(x,y);
		}
			bufferG2.fillPolygon(upperLip);
	}
// ************************************************************
	public void drawBigUpperLip() {
		for (int j=0; (j<size); ++j) {
			rightBigX[j] = BigCenterLine + rightX[j]-4;
			rightBigY[j] = rightY[j]-4;
		}
		for (int j=size-1; (j>=0); --j) {
			leftBigX[j] = BigCenterLine - leftX[j]-4;
			leftBigY[j] = leftY[j]-4;
		}
	}
// ************************************************************
	public void drawBigLowerLip() {
		for (int j=0; (j<size); ++j) {
			rightBigX[j] = BigCenterLine + rightX[j]-4;
			rightBigY[j] = rightY[j]-4;
		}
		for (int j=size-1; (j>=0); --j) {
			leftBigX[j] = BigCenterLine - leftX[j]-4;
			leftBigY[j] = leftY[j]-4;
		}
	}
// ************************************************************
	public void drawSmallLowerLip() {
		int x, y;
		Polygon lowerLip = new Polygon();

		// right side
		for (int j=0; (j<size); ++j) {
			x = bx + (int)(who.getJawWidth() * rightSmallX[j]);
			y = by + rightSmallY[j];
			lowerLip.addPoint(x,y);
		}
		// left side
		for (int j=size-1; (j>=0); --j) {
			x = bx - (int)(who.getJawWidth() * leftSmallX[j]);
			y = by + leftSmallY[j];
			lowerLip.addPoint(x,y);
		}
		bufferG2.setColor(Color.black);
		bufferG2.fillPolygon(lowerLip);
		
	}
// ************************************************************
		/* These two methods were deprecrated, but I am retaining the code just
		 * in case Alvaro changes his mind.
		 * 
		public void drawSmallDimple() {
			int x, y;
			Polygon dimple = new Polygon();
			
			for (int j=0; (j<size); ++j) {
				x = bx + (int)(who.getJawWidth() * rightSmallX[j]);
				y = by + rightSmallY[j];
				dimple.addPoint(x, y);
			}

			// left side
			for (int j=size-1; (j>=0); --j) {
				x = bx - (int)(who.getJawWidth() * leftSmallX[j]);
				y = by + leftSmallY[j];
				dimple.addPoint(x,y);
			}
			// we trace BACKWARDS to fill it in
			for (int j=0; (j < size); ++j) {
				x = bx - leftSmallX[j];
				y = by + leftSmallY[j] + who.getThickness(j, Expression.Dimple);
				dimple.addPoint(x, y);
			}
			for (int j=size-1; (j>=0); --j) {
				x = bx + rightSmallX[j];
				y = by + rightSmallY[j] + who.getThickness(j, Expression.Dimple);
				dimple.addPoint(x, y);
			}
			g2.fillPolygon(dimple);
		}
// ************************************************************
		public void drawBigDimple() {
			for (int j=0; (j<size); ++j) {
				rightBigX[j] = bx + (int)(who.getJawWidth() * rightX[j]) - 4;
				rightBigY[j] = by + rightY[j] - 4;
			}			
			for (int j=size-1; (j>=0); --j) {
				leftBigX[j] = bx - (int)(who.getJawWidth() * leftX[j]) - 4;
				leftBigY[j] = by + leftY[j] - 4;
			}
		}
		*/
// ************************************************************
	public void drawSmallLine(int iFeature, int eyeSeparation, double stretchFactor) {
		/*
		 * This method draws the simple linear features: 
		 *    eyebrows, brow lines, orb lines, and jowls
		 */
		int x, y;
		Polygon rightPoly = new Polygon();
		Polygon leftPoly = new Polygon();
		
		bufferG2.setColor(Color.black);
		for (int j=0; (j<size); ++j) {
			x = bx + eyeSeparation + (int)(stretchFactor * (double)rightSmallX[j]);
			y = by +  rightSmallY[j];
			rightPoly.addPoint(x, y);
		}
		// thicken right line
		for (int j=size-2; (j>0); --j) {
			x = bx + eyeSeparation + (int)(stretchFactor * (double)rightSmallX[j]);
			y = by + rightSmallY[j];
			double dx = (int)(stretchFactor * (double)(rightSmallX[j+1] - rightSmallX[j-1]));
			double dy = rightSmallY[j+1] - rightSmallY[j-1];
			double t = (double)who.getThickness(j, iFeature) * lineThickness[rightLineThickness[j]];
			Point testPoint = perpendicular(dx, dy, t);
			double newDX = testPoint.getX();
			double newDY = testPoint.getY();
			int newX = x + (int)newDX;
			int newY = y + (int)newDY;
			rightPoly.addPoint(newX, newY);
		}			
		bufferG2.fillPolygon(rightPoly);
		
		for (int j=0; (j<size); ++j) {
			x = bx - eyeSeparation - (int)(stretchFactor * (double)leftSmallX[j]);
			y = by + leftSmallY[j];
			leftPoly.addPoint(x, y);
		}
		// thicken left line
		for (int j=size-2; (j>0); --j) {
			x = bx - eyeSeparation - (int)(stretchFactor * (double)leftSmallX[j]);
			y = by + leftSmallY[j];
			double dx = (int)(stretchFactor * (double)(leftSmallX[j+1] - leftSmallX[j-1]));
			double dy = leftSmallY[j+1] - leftSmallY[j-1];
			double t = (double)who.getThickness(j, iFeature) * lineThickness[leftLineThickness[j]];
			Point testPoint = perpendicular(dx, dy, t);
			double newDX = -testPoint.getX();
			double newDY = testPoint.getY();
			int newX = x + (int)newDX;
			int newY = y + (int)newDY;
			leftPoly.addPoint(newX, newY);
		}
		bufferG2.fillPolygon(leftPoly);
	}
// ************************************************************
	Point perpendicular(double dx, double dy, double length) {
		double dx2 = (double)dx*(double)dx;
		double dy2 = (double)dy*(double)dy;
		double l2 = (double)length*(double)length;
		int deltaX = 0;
		if (dy2>0)
			deltaX = (int)Math.sqrt(l2/(1+dx2/dy2));
		
		int deltaY = 0; 
		if (dx2>0)
			deltaY = (int)Math.sqrt(l2/(1+dy2/dx2));
		
		if ((dx>0)&(dy>0)) { deltaX = -deltaX; }
		if ((dx>0)&(dy<0)) {  }
		if ((dx<0)&(dy>0)) { deltaX = -deltaX; deltaY = -deltaY; }
		if ((dx<0)&(dy<0)) { deltaY = -deltaY; }
		
		Point answer = new Point(deltaX, deltaY);
		return answer;
	}
// ************************************************************
	public void drawBigLine(int iFeature) {
		/*
		 * This method draws the simple linear features: 
		 *    eyebrows, brow lines, orb lines, and jowls
		 */
		for (int j=0; (j<size); ++j) {
			rightBigX[j] = BigCenterLine + rightX[j]-4;
			rightBigY[j] = rightY[j]-4;
		}
		
		for (int j=0; (j<size); ++j) {
			leftBigX[j] = BigCenterLine - leftX[j]-4;
			leftBigY[j] = leftY[j]-4;
		}
	}
// ************************************************************
	public void drawSmallNose() {
		int x = bx + rightSmallX[0];
		int y = by + rightSmallY[0] + who.getSmallVerticalOffsets(Expression.Nose);
		bufferG2.drawImage(who.getSmallNose(), x - who.getSmallNose().getWidth()/2, y, transparent, who);
	}
// ************************************************************
	public void drawBigNose() {
		int x = BigCenterLine + rightX[0];
		int y = rightY[0];
		rightBigX[0] = x-4;
		rightBigY[0] = y-4;
		bufferG2.drawImage(ActorFace.getStandardNose(), x - ActorFace.getStandardNose().getWidth()/2, y, transparent, who);
	}
// ************************************************************
	public void makeItSmall() {
		double a = ActorFace.featureScale; // shorten to make for easier reading
		double b = 0.5/ActorFace.featureScale;  // correction for round-off error
		for (int i=0; (i<size); ++i) {
			rightSmallX[i] = (rightX[i]>=0) ? (int)((rightX[i]+b)*a) : (int)((rightX[i]-b)*a);
			rightSmallY[i] = (int)((rightY[i]+b)*a);
			leftSmallX[i] = (leftX[i]>=0) ? (int)((leftX[i]+b)*a) : (int)((leftX[i]-b)*a);
			leftSmallY[i] = (int)((leftY[i]+b)*a);
		}
	}
// ************************************************************
	public void buildExpression(Feature nullState, Feature fromState, float fromFraction,
			Feature toState, float toFraction, float stepFraction) {
		float v, w;
		
		w = stepFraction * toFraction * (float)toState.participation;

		for (int i=0; (i<size); ++i) {
			v = nullState.rightSmallX[i] + fromFraction * (fromState.rightSmallX[i] - nullState.rightSmallX[i]);
			rightSmallX[i] = (int)(v + w * (toState.rightSmallX[i] - v));

			v = nullState.rightSmallY[i] + fromFraction * (fromState.rightSmallY[i] - nullState.rightSmallY[i]);
			rightSmallY[i] = (int)(v + w * (toState.rightSmallY[i] - v));

			v = nullState.leftSmallX[i] + fromFraction * (fromState.leftSmallX[i] - nullState.leftSmallX[i]);
			leftSmallX[i] = (int)(v + w * (toState.leftSmallX[i] - v));

			v = nullState.leftSmallY[i] + fromFraction * (fromState.leftSmallY[i] - nullState.leftSmallY[i]);
			leftSmallY[i] = (int)(v + w * (toState.leftSmallY[i] - v));
		}
	}
// ************************************************************
}
