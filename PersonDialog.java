//************************** Person Detail Window **************************
// Last Modified: 5/19/00

// The PersonDialog class is a detail window for a person.
// It displays information about the person and the person's current status.

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public class PersonDialog extends UnitDialog {

	// Data members

	private Person person;          // Person detail window is about
	private JButton locationButton; // Location button
	private JLabel latitudeLabel;   // Latitude label
	private JLabel longitudeLabel;  // Longitude label
	private JPanel skillListPane;   // Panel containing list of person's skills and their levels.
	private JLabel taskDescription; // Current task description label
	private JLabel taskPhase;       // Current task phase label
	private JLabel taskSubPhase;    // Current task sub-phase label

	// Cached person data
	
	private Coordinates unitCoords;
	private String settlementName;
	private String vehicleName;
	private Hashtable skillList;
	private String[] skillKeys;

	// Constructor

	public PersonDialog(MainDesktopPane parentDesktop, Person person) {

		// Use UnitDialog constructor

		super(parentDesktop, person);
	}
	
	// Load image icon (overridden)
	
	public ImageIcon getIcon() { return new ImageIcon("PersonIcon.gif"); }
	
	// Initialize cached data members
	
	protected void initCachedData() {
		
		unitCoords = new Coordinates(0D, 0D);
		settlementName = "";
		vehicleName = "";
		skillList = new Hashtable();
		skillKeys = new String[0];
			
	}
	
	// Complete update (overridden)

	protected void generalUpdate() {
		updatePosition();
		updateSettlement();
		updateVehicle();
		updateSkills();
		updateTask();
	}

	// Update position

	private void updatePosition() {
		if (!unitCoords.equals(person.getCoordinates())) {
			unitCoords = new Coordinates(person.getCoordinates());
			latitudeLabel.setText("Latitude: " + unitCoords.getFormattedLatitudeString());
			longitudeLabel.setText("Longitude: " + unitCoords.getFormattedLongitudeString());
		}
	}

	// Update settlement

	private void updateSettlement() { 
		Settlement tempSettlement = person.getSettlement();
		if (tempSettlement != null) {
			if (!settlementName.equals(tempSettlement.getName())) {
				settlementName = tempSettlement.getName();
				locationButton.setText(settlementName); 
			}
		}
	}

	// Update vehicle

	private void updateVehicle() { 
		Vehicle tempVehicle = person.getVehicle();
		if (tempVehicle != null) {
			if (!vehicleName.equals(tempVehicle.getName())) {
				vehicleName = tempVehicle.getName();
				locationButton.setText(vehicleName); 
			}
		}
	}
	
	// Update skill list
	
	private void updateSkills() {
		
		boolean change = false;
		SkillManager skillManager = person.getSkillManager();

		if (skillManager.getSkillNum() != skillKeys.length) change = true;
		else if (skillManager.getSkillNum() > 0) {
			String[] newKeys = skillManager.getKeys();
			for (int x=0; x < newKeys.length; x++) {
				if (!newKeys[x].equals(skillKeys[x])) change = true;
				int skillLevel = ((Integer) skillList.get(skillKeys[x])).intValue();
				if (skillLevel != skillManager.getSkillLevel(newKeys[x])) change = true;
			}
		}
		
		if (change) {
			skillKeys = skillManager.getKeys();
			skillList.clear();
			
			for (int x=0; x < skillKeys.length; x++) 
				skillList.put(skillKeys[x], new Integer(skillManager.getSkillLevel(skillKeys[x])));
			
			skillListPane.removeAll();
			for (int x=0; x < skillKeys.length; x++) {
				if (skillManager.getSkillLevel(skillKeys[x]) > 0) {
				
					// Display skill name
				
					JLabel skillName = new JLabel(skillKeys[x] + ":", JLabel.LEFT);
					skillName.setForeground(Color.black);
					skillName.setVerticalAlignment(JLabel.TOP);
					skillListPane.add(skillName);
				
					// Display skill value
			
					JLabel skillValue = new JLabel("" + skillManager.getSkillLevel(skillKeys[x]), JLabel.RIGHT);
					skillValue.setForeground(Color.black);
					skillValue.setVerticalAlignment(JLabel.TOP);
					skillListPane.add(skillValue);
				}
			}
		}
	}
	
	// Update task info
	
	private void updateTask() {
		
		TaskManager taskManager = person.getTaskManager();
	
		// Update task description
		
		String cacheDescription = "None";
		if (taskManager.hasCurrentTask()) cacheDescription = taskManager.getCurrentTaskDescription();
		if (!cacheDescription.equals(taskDescription.getText())) taskDescription.setText(cacheDescription);
		
		// Update task phase
		
		String cachePhase = "";
		if (taskManager.hasCurrentTask()) {
			String phase = taskManager.getCurrentPhase();
			if ((phase != null) && !phase.equals("")) cachePhase = "Phase: " + phase;
		}
		if (!cachePhase.equals(taskPhase.getText())) taskPhase.setText(cachePhase);
		
		// Update task sub-phase
		
		String cacheSubPhase = "";
		if (taskManager.hasCurrentTask()) {
			String subPhase = taskManager.getCurrentSubPhase();
			if ((subPhase != null) && !subPhase.equals("")) cacheSubPhase = "Sub-Phase: " + subPhase;
		}
		if (!cacheSubPhase.equals(taskSubPhase.getText())) taskSubPhase.setText(cacheSubPhase);
	}
	
	// ActionListener method overriden
	
	public void actionPerformed(ActionEvent event) {
		super.actionPerformed(event);
		
		Object button = event.getSource();
			
		// If location button, open window for selected unit
			
		if (button == locationButton) {
			if (person.getSettlement() != null) parentDesktop.openUnitWindow(person.getSettlement().getID());
			else if (person.getVehicle() != null) parentDesktop.openUnitWindow(person.getVehicle().getID());
		}
	}
	
	// Set window size
	
	protected Dimension setWindowSize() { return new Dimension(300, 345); }
	
	// Prepare components
	
	protected void setupComponents() {
		
		super.setupComponents();
		
		// Initialize person

		person = (Person) parentUnit;

		// Prepare tab pane
		
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("Task", setupTaskPane());
		tabPane.addTab("Location", setupLocationPane());
		tabPane.addTab("Attributes", setupAttributePane());
		tabPane.addTab("Skills", setupSkillPane());
		mainPane.add(tabPane, "Center");
	}
	
	// Set up task panel
	
	protected JPanel setupTaskPane() {
	
		// Prepare Task pane
		
		JPanel taskPane = new JPanel(new BorderLayout());
		taskPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		
		// Prepare task label pane

		JPanel taskLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		taskPane.add(taskLabelPane, "North");
		
		// Prepare task label

		JLabel taskLabel = new JLabel("Current Task", JLabel.CENTER);
		taskLabel.setFont(new Font("Helvetica", Font.BOLD, 13));
		taskLabel.setForeground(Color.black);
		taskLabelPane.add(taskLabel);
		
		// Use person's task manager.
		
		TaskManager taskManager = person.getTaskManager();
		
		// Prepare task description pane
		
		JPanel taskDescriptionPane = new JPanel(new GridLayout(3, 1));
		JPanel taskDescriptionTopPane = new JPanel(new BorderLayout());
		taskDescriptionTopPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		taskDescriptionTopPane.add(taskDescriptionPane, "North");
		taskPane.add(new JScrollPane(taskDescriptionTopPane), "Center");
		
		// Display description of current task.
		// Display 'None' if person is currently doing nothing.
		
		taskDescription = new JLabel("None", JLabel.LEFT);
		if (taskManager.hasCurrentTask()) taskDescription.setText(taskManager.getCurrentTaskDescription());
		taskDescription.setForeground(Color.black);
		taskDescriptionPane.add(taskDescription);
		
		// Display name of current phase.
		// Display nothing if current task has no current phase.
		// Display nothing if person is currently doing nothing.
		
		taskPhase = new JLabel("", JLabel.LEFT);
		if (taskManager.hasCurrentTask()) {
			String phase = taskManager.getCurrentPhase();
			if ((phase != null) && !phase.equals("")) taskPhase.setText("Phase: " + phase);
		}
		taskPhase.setForeground(Color.black);
		taskDescriptionPane.add(taskPhase);
		
		// Display name of current sub-phase.
		// Display nothing if current task has no current sub-phase.
		// Display nothing if person is currently doing nothing.
		
		taskSubPhase = new JLabel("", JLabel.LEFT);
		if (taskManager.hasCurrentTask()) {
			String subPhase = taskManager.getCurrentSubPhase();
			if ((subPhase != null) && !subPhase.equals("")) taskSubPhase.setText("Sub-Phase: " + subPhase);
		}
		taskSubPhase.setForeground(Color.black);
		taskDescriptionPane.add(taskSubPhase);
	
		// Return skill panel
		
		return taskPane;
	}
	
	// Set up location panel

	protected JPanel setupLocationPane() {
		
		// Prepare location pane
		
		JPanel locationPane = new JPanel(new BorderLayout());
		locationPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));

		// Prepare location sub pane
		
		JPanel locationSubPane = new JPanel(new GridLayout(2, 1));
		locationPane.add(locationSubPane, "North");

		// Prepare location label pane

		JPanel locationLabelPane = new JPanel(new FlowLayout(FlowLayout.LEFT));
		locationSubPane.add(locationLabelPane);

		// Prepare center map button
		
		centerMapButton = new JButton(new ImageIcon("CenterMap.gif"));
		centerMapButton.setMargin(new Insets(1, 1, 1, 1));
		centerMapButton.addActionListener(this);
		locationLabelPane.add(centerMapButton);

		// Prepare location label

		JLabel locationLabel = new JLabel("Location: ", JLabel.LEFT);
		locationLabel.setForeground(Color.black);
		locationLabelPane.add(locationLabel);

		// Prepare location button
		
		locationButton = new JButton();
		locationButton.setMargin(new Insets(1, 1, 1, 1));
		if (person.getSettlement() != null) locationButton.setText(person.getSettlement().getName());
		else if (person.getVehicle() != null) locationButton.setText(person.getVehicle().getName());
		locationButton.addActionListener(this);
		locationLabelPane.add(locationButton);

		// Prepare location coordinates pane
		
		JPanel locationCoordsPane = new JPanel(new GridLayout(1, 2,  0, 0));
		locationSubPane.add(locationCoordsPane);

		// Prepare latitude label

		latitudeLabel = new JLabel("Latitude: ", JLabel.LEFT);
		latitudeLabel.setForeground(Color.black);
		locationCoordsPane.add(latitudeLabel);

		// Prepare longitude label

		longitudeLabel = new JLabel("Longitude: ", JLabel.LEFT);
		longitudeLabel.setForeground(Color.black);
		locationCoordsPane.add(longitudeLabel);
		
		// Return location panel
		
		return locationPane;
	}
	
	// Set up attribute panel
	
	protected JPanel setupAttributePane() {
	
		// Prepare attribute pane
		
		JPanel attributePane = new JPanel(new BorderLayout());
		attributePane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		
		// Prepare attribute label pane

		JPanel attributeLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		attributePane.add(attributeLabelPane, "North");
		
		// Prepare attribute label

		JLabel attributeLabel = new JLabel("Natural Attributes", JLabel.CENTER);
		attributeLabel.setFont(new Font("Helvetica", Font.BOLD, 13));
		attributeLabel.setForeground(Color.black);
		attributeLabelPane.add(attributeLabel);
		
		// Use person's natural attribute manager.
		
		NaturalAttributeManager attributeManager = person.getNaturalAttributeManager();
		
		// Prepare attribute list pane
		
		JPanel attributeListPane = new JPanel(new GridLayout(attributeManager.getAttributeNum(), 2));
		attributeListPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		attributePane.add(new JScrollPane(attributeListPane), "Center");
		
		// For each natural attribute, display the name and its value.
		
		String[] keyNames = attributeManager.getKeys();
		for (int x=0; x < keyNames.length; x++) {
			
			// Display attribute name
			
			JLabel attributeName = new JLabel(keyNames[x] + ":", JLabel.LEFT);
			attributeName.setForeground(Color.black);
			attributeListPane.add(attributeName);
	
			// Display attribute value
			
			JLabel attributeValue = new JLabel("" + attributeManager.getAttribute(keyNames[x]), JLabel.RIGHT);
			attributeValue.setForeground(Color.black);
			attributeListPane.add(attributeValue);
		}
	
		// Return attribute panel
		
		return attributePane;
	}
	
	// Set up skill panel
	
	protected JPanel setupSkillPane() {
	
		// Prepare skill pane
		
		JPanel skillPane = new JPanel(new BorderLayout());
		skillPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		
		// Prepare skill label pane

		JPanel skillLabelPane = new JPanel(new FlowLayout(FlowLayout.CENTER));
		skillPane.add(skillLabelPane, "North");
		
		// Prepare skill label

		JLabel skillLabel = new JLabel("Skills", JLabel.CENTER);
		skillLabel.setFont(new Font("Helvetica", Font.BOLD, 13));
		skillLabel.setForeground(Color.black);
		skillLabelPane.add(skillLabel);
		
		// Use person's skill manager.
		
		SkillManager skillManager = person.getSkillManager();
		
		// Prepare skill list pane
		
		JPanel skillListTopPane = new JPanel(new BorderLayout());
		skillListTopPane.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
		skillPane.add(new JScrollPane(skillListTopPane), "Center");
		skillListPane = new JPanel(new GridLayout(skillManager.getDisplayableSkillNum(), 2));
		skillListTopPane.add(skillListPane, "North");
		
		
		
		// For each skill, display the name and its value.
		
		String[] keyNames = skillManager.getKeys();
		for (int x=0; x < keyNames.length; x++) {
			if (skillManager.getSkillLevel(keyNames[x]) > 0) {
			
				// Display skill name
			
				JLabel skillName = new JLabel(keyNames[x] + ":", JLabel.LEFT);
				skillName.setForeground(Color.black);
				skillName.setVerticalAlignment(JLabel.TOP);
				skillListPane.add(skillName);
	
				// Display skill value
			
				JLabel skillValue = new JLabel("" + skillManager.getSkillLevel(keyNames[x]), JLabel.RIGHT);
				skillValue.setForeground(Color.black);
				skillValue.setVerticalAlignment(JLabel.TOP);
				skillListPane.add(skillValue);
			}
		}
	
		// Return skill panel
		
		return skillPane;
	}
}

// Mars Simulation Project
// Copyright (C) 1999 Scott Davis
//
// For questions or comments on this project, contact:
//
// Scott Davis
// 1725 W. Timber Ridge Ln. #6206
// Oak Creek, WI  53154
// scud1@execpc.com
// http://www.execpc.com/~scud1/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

