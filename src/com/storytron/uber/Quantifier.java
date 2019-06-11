package com.storytron.uber;

import com.storytron.enginecommon.Utils;

public final class Quantifier extends Word {
	private static final long serialVersionUID = 1l;
	public static String[] predefinedQuantifierLabels = {
		"very negative",
		"negative",
		"slightly negative",
		"zero",
		"slightly positive",
		"positive",
		"very positive",
	};
	int iD;
	private float value;
// **********************************************************************	
	public Quantifier(String label,int iD) {
			super(label);
			this.iD = iD;
			value = boundedTransform(iD-3);
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
	/** 
	 * @param shortNeither ask for "neither" to be returned like that. If false, "neither"
	 *                     is returned as "neither t nor g".
	 * */
	public static String getQuantifierLabel(String bipolarLabel, int quantifierIndex, boolean shortNeither) {

		// special-case hack for Siboot
		if (bipolarLabel.endsWith("gon")) {
			return String.valueOf(quantifierIndex-3);
		}
		else { // this is the original code
			int _index = bipolarLabel.indexOf('_');
			if (_index==0 || _index ==-1 || _index == bipolarLabel.length() 
					|| _index!=bipolarLabel.lastIndexOf('_'))
				if (0<=quantifierIndex && quantifierIndex<predefinedQuantifierLabels.length) 
					return predefinedQuantifierLabels[quantifierIndex];
				else {
					System.out.println("WordButton:replaceQuantifierLabel:Quantifier out of range: "+quantifierIndex);
					return "";
				}

			String newLabel = "";
			switch (quantifierIndex) {
				case 0: { newLabel = "very "; break; }
				case 1: { newLabel = " "; break; }
				case 2: { newLabel = "slightly "; break; }
				case 3: { newLabel = " "; break; }
				case 4: { newLabel = "slightly "; break; }
				case 5: { newLabel = " "; break; }
				case 6: { newLabel = "very "; break; }
				default:;
			}
			if (quantifierIndex < 3) {
				String suffix = bipolarLabel.substring(0, _index);
				suffix = suffix.toLowerCase();
				newLabel = newLabel.concat(suffix);
			} else if (quantifierIndex==3) {
				if (shortNeither)
					newLabel = "neither";
				else
					newLabel = "neither "+bipolarLabel.substring(0, _index).toLowerCase()+" nor "+
								bipolarLabel.substring(_index+1).toLowerCase();
			} else {
				String suffix = bipolarLabel.substring(_index+1);
				suffix = suffix.toLowerCase();
				newLabel = newLabel.concat(suffix);
			}
			return (newLabel);
		}
	}

}
