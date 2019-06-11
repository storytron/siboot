package FaceDisplay;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class XMLHandler {
// ************************************************************
	public static ArrayList<Expression> loadExpressions() {
		ArrayList<Expression> outputArray = new ArrayList<Expression>();
		Expression thisExpression;		
		FileInputStream inputStream = null;
//      String tName = System.getProperty("user.dir")+"/res/images/"+fileName;
		try { inputStream = new FileInputStream("res/FaceRes/expressions.xml"); } 
		catch (Exception e) { System.out.println("couldn't get expressions.xml open"); }
		DocumentBuilder builder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try { builder = factory.newDocumentBuilder(); }
		catch (ParserConfigurationException e) { e.printStackTrace(); }
		
		Document doc=null;
		try { doc = builder.parse(inputStream); } 
		catch(Exception e) { System.out.println("couldn't parse expressions.xml"); }
		
		NodeList expressionList = doc.getElementsByTagName("expression");
		for (int i=0; i<expressionList.getLength(); i++) {
			Node current = expressionList.item(i);
			if (current.getAttributes()!=null) {
				thisExpression = new Expression();				
				thisExpression.setName(current.getAttributes().getNamedItem("aLabel").getNodeValue());
				thisExpression.setAttack(new Integer(current.getAttributes().getNamedItem("attack").getNodeValue()));
				thisExpression.setSustain(new Integer(current.getAttributes().getNamedItem("bSustain").getNodeValue()));
				thisExpression.setDecay(new Integer(current.getAttributes().getNamedItem("decay").getNodeValue()));
//				thisExpression.setMood(new Integer(current.getAttributes().getNamedItem("mood").getNodeValue()));
				NodeList featureList = current.getChildNodes();
				int jHits=0;
				for (int j=0; j<featureList.getLength(); ++j) {
					Node feature = featureList.item(j);
					if (feature.getAttributes()!=null) {						
						thisExpression.getFeature(jHits).setLabel(feature.getAttributes().getNamedItem("name").getNodeValue());
						thisExpression.getFeature(jHits).setSize(new Integer(feature.getAttributes().getNamedItem("size").getNodeValue()));
						thisExpression.getFeature(jHits).setParticipation(new Double(feature.getAttributes().getNamedItem("participation").getNodeValue()));
						NodeList pointList = feature.getChildNodes();
						int kHits=0;
						Feature shortF = thisExpression.getFeature(jHits);
						for (int k=0; (k<pointList.getLength()); ++k) {
							Node point = pointList.item(k);
							if (point.getAttributes()!=null) {
								shortF.setRightX(kHits, new Integer(point.getAttributes().getNamedItem("rightX").getNodeValue()));
								shortF.setRightY(kHits, new Integer(point.getAttributes().getNamedItem("rightY").getNodeValue()));
								shortF.setRightLineThickness(kHits, new Integer(point.getAttributes().getNamedItem("rightLineThickness").getNodeValue()));
								shortF.setLeftX(kHits, new Integer(point.getAttributes().getNamedItem("leftX").getNodeValue()));
								shortF.setLeftY(kHits, new Integer(point.getAttributes().getNamedItem("leftY").getNodeValue()));
								shortF.setLeftLineThickness(kHits, new Integer(point.getAttributes().getNamedItem("leftLineThickness").getNodeValue()));
								++kHits;
							}
						}
						++jHits;
					}
				}
				outputArray.add(thisExpression);
			}
		}
		return outputArray;
	}		
// ************************************************************
	public static void saveExpressions(FaceDisplay fd) {
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream("res/FaceRes/expressions.xml");
		} catch (Exception e) { }
		DocumentBuilder builder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element expressionSet, express, feature, point;

		try {
			builder = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = builder.newDocument();

		// write to doc here.
		expressionSet = doc.createElement("expressionSet");
		doc.appendChild(expressionSet);
		
		expressionSet.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		expressionSet.setAttribute("xsi:noNamespaceSchemaLocation", "expressionSet.xsd");
		ArrayList<Expression> expressions = fd.getExpressions();

		for (Expression fe:expressions) {
			express = doc.createElement("expression");
			expressionSet.appendChild(express);
			express.setAttribute("aLabel", fe.getName());
			express.setAttribute("attack", String.valueOf(fe.getAttack()));
			express.setAttribute("bSustain", String.valueOf(fe.getSustain()));
			express.setAttribute("decay", String.valueOf(fe.getDecay()));
			express.setAttribute("mood", String.valueOf(fe.getMood()));
			for (int j=0; (j<Expression.FeatureCount); ++j) {
				feature = doc.createElement("feature");
				express.appendChild(feature);
				feature.setAttribute("name", fe.getFeature(j).getLabel());
				feature.setAttribute("size", String.valueOf(fe.getFeature(j).getSize()));
				feature.setAttribute("participation", String.valueOf(fe.getFeature(j).getParticipation()));
				for (int k=0; (k<fe.getFeature(j).getSize()); ++k) {
					point = doc.createElement("point");
					point.setAttribute("rightX", String.valueOf(fe.getFeature(j).getRightX(k)));
					point.setAttribute("rightY", String.valueOf(fe.getFeature(j).getRightY(k)));
					point.setAttribute("rightLineThickness", String.valueOf(fe.getFeature(j).getRightLineThickness(k)));
					point.setAttribute("leftX", String.valueOf(fe.getFeature(j).getLeftX(k)));
					point.setAttribute("leftY", String.valueOf(fe.getFeature(j).getLeftY(k)));
					point.setAttribute("leftLineThickness", String.valueOf(fe.getFeature(j).getLeftLineThickness(k)));
					feature.appendChild(point);
				}
			}
		}
		//  *** Transform the DOM model to a text string ***
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			tf.setAttribute("indent-number", new Integer(4));  //			
			Transformer transformer = tf.newTransformer();			
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			
			// initialize StreamResult with File object to save to file			
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source,result);
		} 
		catch (TransformerConfigurationException e) {
			System.out.println("can't tranform the DOM model");
		}
		catch (TransformerException e) {
			System.out.println("can't transform the DOM model");
		}
	}
// ************************************************************
	public static ActorFace[] loadActors() {
		ActorFace[] localFaces = new ActorFace[FaceDisplay.cActors];
		ActorFace thisFace = null;		
		FileInputStream inputStream = null;
		
		try { inputStream = new FileInputStream("res/FaceRes/Actor.xml"); } 
		catch (Exception e) { System.out.println("couldn't get Actor.xml open"); }
		BufferedInputStream bis = new BufferedInputStream(inputStream);
		DocumentBuilder builder = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(false);
		factory.setValidating(false);
		try {
			factory.setFeature("http://xml.org/sax/features/namespaces", false);
			factory.setFeature("http://xml.org/sax/features/validation", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (Exception e) { } 

		try { builder = factory.newDocumentBuilder(); }
		catch (ParserConfigurationException e) { e.printStackTrace(); }
		
		Document doc=null;
		try { doc = builder.parse(bis); } 
		catch(Exception e) { System.out.println("couldn't parse Actor.xml"); }
		
		NodeList actorList = doc.getElementsByTagName("Actor");
		for (int i=0; i<actorList.getLength(); i++) {
			Node thisActor = actorList.item(i);
			if (thisActor.getAttributes()!=null) {
				String name = thisActor.getAttributes().getNamedItem("name").getNodeValue();
				thisFace = new ActorFace(name);				
				NodeList elementList = thisActor.getChildNodes();
				for (int j=0; j<elementList.getLength(); ++j) {
					Node element = elementList.item(j);
					if (element!=null) {
						String nodeName = element.getNodeName();
						if (nodeName == "baseY") thisFace.setBigBaseY(new Integer(element.getTextContent()));
						thisFace.setSmallBaseY(thisFace.getBigBaseY());
						if (nodeName == "jawWidth") thisFace.setJawWidth(new Float(element.getTextContent()));
						if (nodeName == "eyeSize") thisFace.setEyeSize(new Float(element.getTextContent()));
						if (nodeName == "eyeSeparation") thisFace.setEyeSeparation(new Integer(element.getTextContent()));
						if (nodeName == "jowlSeparation") thisFace.setJowlSeparation(new Integer(element.getTextContent()));
						if (nodeName == "verticalOffsets") {
							NodeList offsetsList = element.getChildNodes();
							for (int k=0; (k<offsetsList.getLength()); ++k) {
								Node offset = offsetsList.item(k);
								if (offset.getNodeName() == "eye")
									thisFace.setBigVerticalOffsets(Expression.Eye, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "iris")
									thisFace.setBigVerticalOffsets(Expression.Iris, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "orbLine")
									thisFace.setBigVerticalOffsets(Expression.OrbLine, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "browLine")
									thisFace.setBigVerticalOffsets(Expression.BrowLine, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "eyebrow")
									thisFace.setBigVerticalOffsets(Expression.Eyebrow, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "nose")
									thisFace.setBigVerticalOffsets(Expression.Nose, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "jowl")
									thisFace.setBigVerticalOffsets(Expression.Jowl, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "upperLip")
									thisFace.setBigVerticalOffsets(Expression.UpperLip, new Integer(offset.getTextContent()));
	//							if (offset.getNodeName() == "dimple")
	//								thisFace.setBigVerticalOffsets(Expression.Dimple, new Integer(offset.getTextContent()));
								if (offset.getNodeName() == "lowerLip")
									thisFace.setBigVerticalOffsets(Expression.LowerLip, new Integer(offset.getTextContent()));
							}
						}
						
						// This loads the thicknesses of the features
						int iFeature = -1;
						if (nodeName.endsWith("Thickness")) {
							if (nodeName.startsWith("brow")) { iFeature = Expression.BrowLine; }
							if (nodeName.startsWith("eyebrow")) { iFeature = Expression.Eyebrow; }
							if (nodeName.startsWith("orb")) { iFeature = Expression.OrbLine; }
							if (nodeName.startsWith("jowl")) { iFeature = Expression.Jowl; }
						}
						if (iFeature >= 0) {
							NodeList thicknessList = element.getChildNodes();
							int kHits=0;
							for (int k=0; (k<thicknessList.getLength()); ++k) {
								Node thickness = thicknessList.item(k);
								if (thickness.getNodeName() == "thickness") {
									thisFace.setThickness(kHits, iFeature, new Integer(thickness.getTextContent()));
									++kHits;
								}
							}							
						}
					}
				}
			}
		localFaces[i] = thisFace; 
		}
		return localFaces;
	}
	

}
