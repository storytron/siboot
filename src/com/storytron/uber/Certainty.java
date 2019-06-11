package com.storytron.uber;

import com.storytron.enginecommon.Utils;

public final class Certainty extends Word {
	private static final long serialVersionUID = 1l;
	public static String[] predefinedCertaintyLabels = {
		"very uncertain",
		"uncertain",
		"slightly uncertain",
		"slightly certain",
		"certain",
		"very certain",
	};
	int iD;
	private float value;
// **********************************************************************	
	public Certainty(String label,int iD) {
			super(label);
			this.iD = iD;
			value = boundedTransform(iD-2.5f);
		}
// **********************************************************************	
	public int getID() { return iD; }
// **********************************************************************
	public float getValue() { return value; }
// **********************************************************************	
	private float boundedTransform(float unboundedNumber) {
		if (unboundedNumber > 0.0f) {
			if (unboundedNumber>Utils.MAXI_NVALUE)
				unboundedNumber = Utils.MAXI_NVALUE;
			return 1.0f - (1.0f / (1.0f + unboundedNumber));
		}
		else
		{
			if (unboundedNumber<Utils.MINI_NVALUE)
				unboundedNumber = Utils.MINI_NVALUE;
			return (1.0f / (1.0f - unboundedNumber)) -1.0f;
		}
	}
// **********************************************************************
	public static String getCertaintyLabel(int certaintyIndex) {
		return predefinedCertaintyLabels[certaintyIndex];
	}

}
