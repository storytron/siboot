package FaceDisplay;

public class Expression {
	// An expression is a set of X, Y coordinates that specify how the facial features are to
	//  be drawn
	public static final int FeatureCount = 9;
	
	public static final int Eye = 0;
	public static final int Iris = 1;
	public static final int OrbLine = 2;
	public static final int BrowLine = 3;
	public static final int Eyebrow = 4;
	public static final int Nose = 5;	
	public static final int Jowl = 6;
	public static final int UpperLip = 7;
	public static final int LowerLip = 8;
	
	public static final int Sad = 1;
	public static final int Happy = 2;
	public static final int Lying = 3;
	public static final int Sincere = 4;
	public static final int Fearful = 5;
	public static final int Angry = 6;

	
	private String name = new String();
	private Feature[] features = new Feature[FeatureCount];	
	private int attack, sustain, decay, mood;
// ************************************************************
	public Expression() {
		attack = 0;
		sustain = 0;
		decay = 0;
		mood = 0;
		for (int i=0; (i<FeatureCount); ++i) 
			features[i] = new Feature();
	}
//**********************************************************************	
	public Expression clone() {
		Expression newExpression = new Expression();
		newExpression.name = name;
		newExpression.attack = attack;
		newExpression.sustain = sustain;
		newExpression.decay = decay;
		
		// now clone the Features
		for (int i=0; (i<FeatureCount); ++i) {
			newExpression.features[i] = features[i].clone();
		}
		return newExpression;
	}
// ************************************************************
	public Feature getFeature(int tIndex) { return features[tIndex]; }
// ************************************************************
	public String getName() { return name; }
// ************************************************************
	public void setName(String tName) { name = tName; }
// ************************************************************
	public int getAttack() { return attack; }
// ************************************************************
	public void setAttack(int newValue) { attack = newValue; }
// ************************************************************
	public int getSustain() { return sustain; }
// ************************************************************
	public void setSustain(int newValue) { sustain = newValue; }
// ************************************************************
	public int getDecay() { return decay; }
// ************************************************************
	public void setDecay(int newValue) { decay = newValue; }
// ************************************************************
	public int getMood() { return mood; }
// ************************************************************
	public void setMood(int newValue) { mood = newValue; }
// ************************************************************
// ************************************************************
// ************************************************************
	public void drawBig() {
		for (int i=0; (i<FeatureCount); ++i) {
			features[i].drawBig(i);
		}
	}
// ************************************************************
	public void drawSmall(int baseX, int baseY) {
		Feature.setOrigin(baseX, baseY);
		for (int i=2; (i<FeatureCount); ++i) {
			features[i].drawSmall(i);
		}
		features[0].drawSmall(0);
		features[1].drawSmall(1);
	}
// ************************************************************
	public void makeItSmall() {
		for (int i=0; (i<FeatureCount); ++i) { features[i].makeItSmall(); }
	}
// ************************************************************
	public void buildExpression(Expression nullExpression, Expression emotion, float emoFraction, 
			Expression excursion, float excFraction, float animationFraction) {
		for (int i=0; (i<FeatureCount); ++i) { 
			features[i].buildExpression(nullExpression.features[i], emotion.features[i], emoFraction, 
					excursion.features[i], excFraction, animationFraction); 
		}
	}
// ************************************************************

}
