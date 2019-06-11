package Engine.enginePackage;

import java.awt.*;

import javax.swing.*;
public class Scroller extends JPanel {
   public Scroller() throws HeadlessException {
       final JPanel panel = new JPanel();
       panel.setBorder(BorderFactory.createLineBorder(Color.red));
       panel.setPreferredSize(new Dimension(380, 740));
 		 Icon image = new ImageIcon( System.getProperty("user.dir")+"/res/images/DSCN0055.png" );
 		 panel.add(new JLabel(image));
 
       JScrollPane scroll = new JScrollPane(panel);
       scroll.getViewport().add(panel);
       setLayout(new BorderLayout());
       add(scroll, BorderLayout.CENTER);
//       setSize(300, 300);
       setVisible(true);
   }
}