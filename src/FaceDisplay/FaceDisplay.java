package FaceDisplay;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

// ***********************************************************************************
public class FaceDisplay implements ImageObserver {
	public static final int cActors = 6; // number of characters in the working set
	private ActorFace[] faces = new ActorFace[cActors];
	private ArrayList<Expression> expressions = new ArrayList<Expression>();
	private Expression displayExpression; 
	Color transparent = new Color(0,0,0,0);
	private Graphics2D myBackBuffer;
	
	public FaceDisplay(Graphics2D bBuffer) {	
		expressions = XMLHandler.loadExpressions();
		// generate the small versions of the features
		for (Expression e:expressions) {
			for (int i=0; i<Expression.FeatureCount; ++i) {
				e.getFeature(i).makeItSmall();
			}
		}
		faces = XMLHandler.loadActors();
		for (ActorFace af:faces) {
			af.makeItSmall();
		}
		displayExpression = getExpression(0).clone();	
		displayExpression.setName("display");
		myBackBuffer = bBuffer;
		myBackBuffer.setColor(transparent);
		myBackBuffer.fillRect(0,0,300,310);
	}
// ***********************************************************************************
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return true;
	}
// ***********************************************************************************
	public Expression getExpression(int tIndex) { return expressions.get(tIndex); }
// ***********************************************************************************
	public Expression getDisplayExpression() { return displayExpression; }
// ***********************************************************************************
	public ArrayList<Expression> getExpressions() { return expressions; }
// ***********************************************************************************
	public int getExpressionIndex(String expressionName) {
		int i = 0;
		while ((i<expressions.size()) && (!expressionName.equals(expressions.get(i).getName()))) {
			++i;
		}
		if (i==expressions.size()) {
			System.out.println("FaceDisplay.java unable to find expression "+expressionName);
			i=0;
		}
		return i;
	}
// ***********************************************************************************
	public Expression getExpression(String expressionName) {
		int i = 0;
		while ((i<expressions.size()) && (!expressionName.equals(expressions.get(i).getName()))) {
			++i;
		}
		if (i==expressions.size())
			System.out.println("FaceDisplay.java unable to find expression "+expressionName);
		return expressions.get(i);
	}
// ***********************************************************************************
	public ActorFace getFace(String actorName) { 
		boolean gotcha = false;
		int i = 0;
		do {
			gotcha = (faces[i++].getName().equals(actorName));
		}
		while ((i<cActors) & !gotcha);
		if (!gotcha) 
			System.out.println("FaceDisplay.getFace() cannot find actor: "+actorName);
		return faces[i-1]; 
	}
// ***********************************************************************************
	public int getExpressionCount() { return expressions.size(); }
// ***********************************************************************************
	public void drawFace(String actorName, int tExpression, float expressionMagnitude, int tExcursion, float excursionMagnitude, float tdx) {
		displayExpression.buildExpression(getExpression(0), getExpression(tExpression), 1.0f, getExpression(tExcursion), excursionMagnitude, tdx);
		ActorFace af = getFace(actorName);
		Feature.setUpFeatures(myBackBuffer, af);
		myBackBuffer.drawImage(af.getSmallFace(),0,0,transparent,this);
		displayExpression.drawSmall(0,0);	
	}
// ***********************************************************************************
}
