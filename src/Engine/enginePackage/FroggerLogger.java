package Engine.enginePackage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import com.storytron.enginecommon.Utils;
import com.storytron.swat.loglizard.LogTreeModel;
import com.storytron.uber.Actor;
import com.storytron.uber.Role;
import com.storytron.uber.Script;
import com.storytron.uber.ScriptPath;
import com.storytron.uber.Verb;
import com.storytron.uber.operator.Operator;

/**
 * An extension of the {@link TreeLogger} with custom messages.
 * A message type is used to identify the different messages
 * the engine may log.
 * <p>
 * Each method of this class inserts a message of a different type.
 * <p>
 * Some methods only inserts a new message, and some other methods
 * additionally set the current node as the newly created one. Methods
 * setting the current node have suffix Msg. Methods not setting the current
 * node have suffix MsgChild. 
 * <p>
 * By convention the message type is stored at the first position in the message.
 * <p> 
 * This class provide a mechanism for avoiding generation off all the data.
 * Descendants of nodes of types in the {@link #frontierTypes} set are cut,
 * to avoid the overhead of storing all the logged nodes.
 * <p>
 * Also logging can be turned off at all by calling {@link #setLogging(boolean)}
 * with false.
 * <p>
 * If it is needed to collect all the nodes in a branch, including descendants
 * of the nodes in the {@link #frontierTypes} set, it is possible to specify
 * the specific branches that must be fully collected. This is done through 
 * the {@link #collectables} queue, where requests should be inserted.
 * It is needed, then, that the {@link Engine} execution generating the requested
 * branches be executed again, so the logger can do its work.
 * <p>
 * The requested branches will be returned in the {@link #compressed} queue.
 * Each entry in this queue holds a a branch identifier and a bunch of 
 * consecutive nodes of the log tree. The branch identifier is the index of its
 * root, which will always be a child of the log tree root. 
 * The bunch of nodes starts at the beginning of the first branch requested
 * and ends somewhere in the branch identified in this entry. This identified 
 * branch is guaranteed to be between the first requested branch and the last.
 * <p>
 * The reason to have an entry in the {@link #compressed} queue that does not 
 * contain all the requested nodes is that they could be too many, and therefore
 * have to be collected in stages. Execution of the engine will be suspended
 * if this happens, and will be resumed after the caller gets rid of the first
 * chunk of data.
 * <p>
 * When collecting full branches, the nodes in every other branch will be 
 * discarded. The method {@link #setBranchCounter(int)} can be used to
 * indicate at which branch execution of the {@link Engine} will start.
 * */
public final class FroggerLogger extends JDialog {
	private static final long serialVersionUID = 1L;
	public enum MsgType {
		EXECUTE,
		ROLE,
		OPTION,
		WORDSOCKETS,
		WORDSOCKET,
		DISQUALIFIED,
		CHOOSE_OPTION,
		FATE_REACTING,
		WITNESS,
		DIROBJECT,
		SUBJECT,
		SCRIPT,
		TOKEN,
		PARENTVALUE,
		SIBLINGVALUE,
		POISON,
		ABORT,
		SEARCHMARK
	};
	private JTree tree;
	private DefaultMutableTreeNode top;
	public static final Set<MsgType> frontierTypes = EnumSet.of(MsgType.SCRIPT, MsgType.SUBJECT, MsgType.DIROBJECT,MsgType.WITNESS,MsgType.FATE_REACTING);
	DefaultMutableTreeNode currentNode, newNode;
   DefaultMutableTreeNode category = null;
   DefaultMutableTreeNode book = null;
   private boolean isLogging;
   
// **********************************************************************			
	public FroggerLogger(){
		super();
	   top = new DefaultMutableTreeNode("Once upon a time...");
		tree = new JTree(top);
		currentNode = top;
		newNode = null;
		JScrollPane treeView = new JScrollPane(tree);
		add(treeView);
		isLogging = true;

		// initialization of the window
		tree.setToggleClickCount(0);
		tree.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				if ((e.getModifiers() & KeyEvent.SHIFT_MASK) ==0)
					return;
				
				TreePath tp = tree.getClosestPathForLocation(e.getX(), e.getY());
				if (tp==null)
					return;
				// Only allow clicks on a handle.
				if (tree.getPathForLocation(e.getX(), e.getY())!=null)
					return;
				
				tree.expandPath(tp);
			}
		});
		tree.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.getModifiers()&KeyEvent.SHIFT_MASK)==0 || e.getKeyCode()!=KeyEvent.VK_RIGHT)
					return;
				TreePath tp = tree.getSelectionPath();
				if (tp==null || tree.isExpanded(tp))
					return;
				
				tree.expandPath(tp);
				e.consume();
			}
		});
		tree.setCellRenderer(new DefaultTreeCellRenderer(){
			private static final long serialVersionUID = 1L;
			@Override
			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean sel, boolean expanded, boolean leaf,
					int row, boolean hasFocus) {
				Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
						row, hasFocus);
				if (value instanceof LogTreeModel.Node) {
					LogTreeModel.Node n = (LogTreeModel.Node)value;
					if (Utils.markedBySearch(n.params))
						c.setBackground(Utils.lightGrayBackground);
					else if (isLastRootDescendent(n)) {
							if (containsSearchedDescendant(n))
								c.setBackground(Utils.lightGrayBackground);
							else
								c.setBackground(super.getBackgroundNonSelectionColor());
					} else if (isRootChild(n) && containsSearchedChild(n))
						c.setBackground(Utils.lightGrayBackground);
					else 
						c.setBackground(super.getBackgroundNonSelectionColor());
				} else
					c.setBackground(super.getBackgroundNonSelectionColor());				
				return c;
			}
			/** Tells if a node is the last root child. */
			private boolean isLastRootDescendent(LogTreeModel.Node n){
				final Object root = tree.getModel().getRoot();
				int childCount = tree.getModel().getChildCount(root);
				if (childCount==0)
					return false;
				final LogTreeModel.Node lastChild = (LogTreeModel.Node)tree.getModel().getChild(root,childCount-1); 
				return lastChild==n || isLastDescendent(lastChild,n);
			}
			private boolean isLastDescendent(LogTreeModel.Node root,LogTreeModel.Node n){
				if (root.children==null || root.children.length==0)
					return false;
				else {
					final LogTreeModel.Node lastChild = (LogTreeModel.Node)tree.getModel().getChild(root,root.children.length-1); 
					return lastChild==n || isLastDescendent(lastChild,n);	
				}
			}
			/** Tells if a node is a root child. */
			private boolean isRootChild(LogTreeModel.Node n){
				final Object root = tree.getModel().getRoot();
				return tree.getModel().getIndexOfChild(root,n)>=0;
			}

			private boolean containsSearchedDescendant(LogTreeModel.Node n){
				if (n.children==null || n.children.length==0)
					return false;
				
				int childCount = tree.getModel().getChildCount(n);
				for(int i=0;i<childCount;i++) {
					if (Utils.markedBySearch(((LogTreeModel.Node)tree.getModel().getChild(n,i)).params))
						return true;
						
				}
				return containsSearchedDescendant((LogTreeModel.Node)tree.getModel().getChild(n,n.children.length-1));
			}
			/** Tells if a child is a marked node. */
			private boolean containsSearchedChild(LogTreeModel.Node n){
				if (n.children==null || n.children.length==0)
					return false;
				
				int childCount = tree.getModel().getChildCount(n);
				for(int i=0;i<childCount;i++) {
					if (Utils.markedBySearch(((LogTreeModel.Node)tree.getModel().getChild(n,i)).params))
						return true;
						
				}
				return false;
			}
			
			@Override
			public Color getBackgroundNonSelectionColor() {
				return null;
			}
		});
//		tree.addTreeWillExpandListener(this);
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setOpenIcon(null);
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setClosedIcon(null);
		((DefaultTreeCellRenderer)tree.getCellRenderer()).setLeafIcon(null);
		getContentPane().add(new JScrollPane(tree),BorderLayout.CENTER);
		setSize(600, 400);
		setLocation(100, 200);
		setVisible(false);	
	}
// **********************************************************************
	public void showThyself() {
		tree.expandPath(new TreePath(top));
		setVisible(true);
	}
// **********************************************************************
	public boolean getIsLogging() {
		return isLogging;
	}
// **********************************************************************
	public void setIsLogging(boolean newValue) {
		isLogging = newValue;
	}
// **********************************************************************		
// **********************************************************************
	public void recordHistoryMsg(String page) {
		System.out.println("record History: "+page);
		newNode = new DefaultMutableTreeNode(page);
		top.add(newNode);
		currentNode = newNode;
	}
// **********************************************************************		
	public void fateReactingMsg() { 
		if (isLogging) {
//			System.out.println("Fate considering reacting");
			newNode = new DefaultMutableTreeNode("Fate considering reacting");
			currentNode.add(newNode);
			currentNode = newNode;
		}
	};
// **********************************************************************	
	public void appendFateReactsMsg() {
		if (isLogging) {
			DefaultMutableTreeNode testNode = currentNode;
			while (!((String)testNode.getUserObject()).endsWith("considering reacting")) {
				testNode = (DefaultMutableTreeNode)testNode.getParent();
			}
			testNode.setUserObject((String)testNode.getUserObject()+": REACTS!");
		}
	}
// **********************************************************************		
	public void witnessMsg(Actor witness) { 
		if (isLogging) {
//			System.out.println("Witness reacting");
			newNode = new DefaultMutableTreeNode("Witness "+witness.getLabel()+" considering reacting");
			currentNode.add(newNode);
			currentNode = newNode;
		}
	};
// **********************************************************************	
	public void appendWitnessReactsMsg() {
		if (isLogging) {
			DefaultMutableTreeNode testNode = currentNode;
			while (!((String)testNode.getUserObject()).endsWith("considering reacting")) {
				testNode = (DefaultMutableTreeNode)testNode.getParent();
			}
			testNode.setUserObject((String)testNode.getUserObject()+": REACTS!");
		}
	}
// **********************************************************************		
	public void dirObjectMsg(Actor dirObject) { 
		if (isLogging) {
//			System.out.println("DirObject considering reacting");
			newNode = new DefaultMutableTreeNode("DirObject "+dirObject.getLabel()+" considering reacting");
			currentNode.add(newNode);
			currentNode = newNode;
		}
	};
// **********************************************************************	
	public void appendDirObjectReactsMsg() {
		if (isLogging) {
			DefaultMutableTreeNode testNode = currentNode;
			while (!((String)testNode.getUserObject()).endsWith("considering reacting")) {
				testNode = (DefaultMutableTreeNode)testNode.getParent();
			}
			testNode.setUserObject((String)testNode.getUserObject()+": REACTS!");
		}
	}
// **********************************************************************		
	public void subjectMsg(Actor subject) { 
		if (isLogging) {
//			System.out.println("Subject reacting");
			newNode = new DefaultMutableTreeNode("Subject "+subject.getLabel()+" considering reacting");
			currentNode.add(newNode);
			currentNode = newNode;
		}
	};
// **********************************************************************	
	public void appendSubjectReactsMsg() {
		if (isLogging) {
			DefaultMutableTreeNode testNode = currentNode;
			while (!((String)testNode.getUserObject()).endsWith("considering reacting")) {
				testNode = (DefaultMutableTreeNode)testNode.getParent();
			}
			testNode.setUserObject((String)testNode.getUserObject()+": REACTS!");
		}
	}
// **********************************************************************		
	void wordSocketsMsg() { 
		if (isLogging) {
//			System.out.println("Word Socket");
			newNode = new DefaultMutableTreeNode("Word Sockets:");
			currentNode.add(newNode); 
			currentNode = newNode;
		}
	};
// **********************************************************************		
	void wordSocketMsg(String wordSocketLabel) { 
		if (isLogging) {
//			System.out.println(wordSocketLabel);
			newNode = new DefaultMutableTreeNode(wordSocketLabel);
			currentNode.add(newNode); 
			currentNode = newNode;
		}
	};
// **********************************************************************	
	public void roleMsg(Actor reactingActor, Role.Link role) {
		if (isLogging) {
//			System.out.println("Role execution by "+reactingActor.getLabel());
			newNode = new DefaultMutableTreeNode(reactingActor.getLabel()+" executing Role "+role.getLabel());
			currentNode.add(newNode); 
			currentNode = newNode;	
		}
	}
// **********************************************************************
	public void chooseOptionMsgChild(float bestInclination, Verb verb) {
		if (isLogging) {
			newNode = new DefaultMutableTreeNode("best inclination value: "+bestInclination+"  Chosen option: "+verb.getLabel());
			currentNode.add(newNode); 
			currentNode = newNode;
		}
	}
// **********************************************************************		
	public void disqualifiedMsgChild(Role.Option option, String wordSocketLabel) {
		if (isLogging) {
			newNode = new DefaultMutableTreeNode("      Option ("+option.getLabel()+") disqualified by failure to find an acceptable "+wordSocketLabel);
			currentNode.add(newNode); 
		}
	}
// **********************************************************************		
	public void scriptMsg(ScriptPath sp,Script s) {
//		System.out.println(s.getLabel()+": Script "+sp.getPath(s));
		if (isLogging) {
//			newNode = new DefaultMutableTreeNode(s.getLabel()+": Script "+sp.getPath(s));
//			currentNode.add(newNode); 
	//		currentNode = newNode;
		}
	}
// **********************************************************************		
	public void tokenMsg(Operator op) {
		if (isLogging) {
			newNode = new DefaultMutableTreeNode(op.getLabel());
			currentNode.add(newNode); 
			currentNode = newNode;	
		}
	}
// **********************************************************************		
	void executeMsg(int cTicks,String sentence) {
		if (isLogging) {
			newNode = new DefaultMutableTreeNode(cTicks+": "+sentence);
			currentNode.add(newNode); 
			currentNode = newNode;	
		}
	}
// **********************************************************************		
	void optionMsg(Role.Option opt) {
		if (isLogging) {
			newNode = new DefaultMutableTreeNode("   considering option: "+opt.getLabel());
			currentNode.add(newNode); 
			currentNode = newNode;	
		}
	}
// **********************************************************************	
	public void up() {
		if (isLogging) {
//			System.out.println("      up from "+(String)currentNode.getUserObject()+" to "+(String)((DefaultMutableTreeNode)currentNode.getParent()).getUserObject());
			currentNode = (DefaultMutableTreeNode)currentNode.getParent();
		}
	}
// **********************************************************************		
	void valueMsgChild(String value) {
		if (isLogging) {
			String temp = (String)currentNode.getUserObject();
			temp += "  "+value;
			currentNode.setUserObject(temp);
		}
	};
// **********************************************************************		
/*
	void reactsMsgChild() throws InterruptedException {
			insertChild(new Object[]{MsgType.SIBLINGVALUE});
	};
	void scriptResultMsgChild(Object res) throws InterruptedException {
			insertChild(new Object[]{MsgType.SIBLINGVALUE,res});
	};
	void poisonMsgChild(String cause) throws InterruptedException {
{
			if (aboutToInsert(false)) {
				insertChild(new Object[]{MsgType.POISON,cause});
				markNode(new Object[]{MsgType.SIBLINGVALUE,MsgType.SEARCHMARK});
			} else 
				markNode();
		}
	};
	void abortedMsgChild() throws InterruptedException {
			insertChild(new Object[]{MsgType.PARENTVALUE,MsgType.ABORT});
	};
	void pageMsgChild(int page) throws InterruptedException {
			insertChild(new Object[]{MsgType.PARENTVALUE,page});
	};
*/	
}
