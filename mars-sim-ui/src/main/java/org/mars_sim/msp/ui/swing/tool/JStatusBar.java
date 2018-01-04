/**
 * Mars Simulation Project
 * JStatusBar.java
 * @version 3.07 2015-01-06
 * Modified by Manny Kung
 */

package org.mars_sim.msp.ui.swing.tool;

//import com.jgoodies.forms.layout.CellConstraints;
//import com.jgoodies.forms.layout.FormLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class JStatusBar extends JPanel {

	public JPanel contentPanel ;
	//public FormLayout layout;
	
    private static final long serialVersionUID = 1L;
    	   
    protected JPanel leftPanel;
    protected JPanel rightPanel;
    	 
    public JStatusBar() {
    	createPartControl();
    	//setOpaque(false);
    	//setBackground(new Color(0,0,0,128));
    }
    	 
    protected void createPartControl() {    

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(getWidth(), 23));
 
        leftPanel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 5, 3));
        //leftPanel.setOpaque(false);
		//leftPanel.setBackground(new Color(0,0,0,128));
        add(leftPanel, BorderLayout.WEST);
        
        rightPanel = new JPanel(new FlowLayout(
                FlowLayout.TRAILING, 5, 3));
        //rightPanel.setOpaque(false);
		//rightPanel.setBackground(new Color(0,0,0,128));
        add(rightPanel, BorderLayout.EAST);
        
    }

    
    public void addRightComponent(JComponent component, boolean isCornerIcon) {
        JPanel panel = new JPanel(new FlowLayout(
                FlowLayout.LEADING, 5, 0));
        if (!isCornerIcon) 
        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        panel.add(component);
        rightPanel.add(panel);
    }
    
    public void setLeftComponent(JComponent component, boolean separator) {
        leftPanel.add(component);
        if (separator) 
        	leftPanel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
        
    }
    
    /*
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int y = 0;
        g.setColor(new Color(156, 154, 140));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(196, 194, 183));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(218, 215, 201));
        g.drawLine(0, y, getWidth(), y);
        y++;
        g.setColor(new Color(233, 231, 217));
        g.drawLine(0, y, getWidth(), y);

        y = getHeight() - 3;
        g.setColor(new Color(233, 232, 218));
        g.drawLine(0, y, getWidth(), y);
       
        y++;
        g.setColor(new Color(233, 231, 216));
        g.drawLine(0, y, getWidth(), y);
       
        y = getHeight() - 1;
        g.setColor(new Color(221, 221, 220));
        g.drawLine(0, y, getWidth(), y);

    }
*/
}
