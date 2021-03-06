/**
 * Mars Simulation Project
 * EventTableModel.java
 * @version 3.1.0 2017-03-09
 * @author Barry Evans
 */
package org.mars_sim.msp.ui.swing.tool.monitor;


import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.controlsfx.control.Notifications;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.events.HistoricalEventListener;
import org.mars_sim.msp.core.events.HistoricalEventManager;
import org.mars_sim.msp.core.malfunction.Malfunctionable;
import org.mars_sim.msp.core.person.EventType;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.time.ClockListener;
import org.mars_sim.msp.ui.javafx.MainScene;
import org.mars_sim.msp.ui.javafx.MainSceneMenu;
import org.mars_sim.msp.ui.swing.MainDesktopPane;
import org.mars_sim.msp.ui.swing.notification.NotificationMenu;
import org.mars_sim.msp.ui.swing.notification.NotificationWindow;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
/**
 * This class provides a table model for use with the MonitorWindow that
 * provides a mean to display the Historical Event. This is actually an
 * Adapter onto the existing Event Manager.
 */
@SuppressWarnings("restriction")
public class EventTableModel
extends AbstractTableModel
implements MonitorModel, HistoricalEventListener, ClockListener {

	 /** default logger.   */
	//private static Logger logger = Logger.getLogger(EventTableModel.class.getName());

	// Column names
	private static final int TIMESTAMP = 0;
	private static final int DESC = 1;
	private static final int TYPE = 2;
	private static final int CATEGORY = 3;
	private static final int ACTOR = 4;
	private static final int LOCATION = 5;
	
	private static final int COLUMNCOUNT = 6;

	private boolean showMedical = true;
	private boolean showMedicalCache = true;
	private boolean showMalfunction = true;
	private boolean showMalfunctionCache = true;

	private boolean noFiring = false;
	/** Names of the displayed columns. */
	static private String columnNames[];
	/** Types of the individual columns. */
	static private Class<?> columnTypes[];

	static {
		columnNames = new String[COLUMNCOUNT];
		columnTypes = new Class[COLUMNCOUNT];
		columnNames[TIMESTAMP] = Msg.getString("EventTableModel.column.time");		columnTypes[TIMESTAMP] = String.class; //$NON-NLS-1$
		columnNames[DESC] = Msg.getString("EventTableModel.column.description");	columnTypes[DESC] = String.class; //$NON-NLS-1$
		columnNames[TYPE] = Msg.getString("EventTableModel.column.eventType");		columnTypes[TYPE] = String.class; //$NON-NLS-1$
		columnNames[CATEGORY] = Msg.getString("EventTableModel.column.category");	columnTypes[CATEGORY] = String.class; //$NON-NLS-1$
		columnNames[ACTOR] = Msg.getString("EventTableModel.column.actor");			columnTypes[ACTOR] = Object.class; //$NON-NLS-1$
		columnNames[LOCATION] = Msg.getString("EventTableModel.column.location");	columnTypes[LOCATION] = Object.class; //$NON-NLS-1$
	}

	private HistoricalEventManager manager;
	private NotificationWindow notifyBox;
	private MainDesktopPane desktop;
	private NotificationMenu nMenu;
	private MainSceneMenu mainSceneMenu;

	private List<HistoricalEvent> cachedEvents = new ArrayList<HistoricalEvent>();

	// Event categories to be displayed.
	private boolean displayMalfunction = true;
	private boolean displayMedical = true;
	private boolean displayMission = true;
	
	private boolean displayTask = false;
	private boolean displayTransport = false;

	/**
	 * constructor.
	 * Create a new Event model based on the specified event manager.
	 * @param manager Manager to extract events from.
	 * @param notifyBox to present notification message to user.
	 */
	// 2014-11-29 Added NotificationWindow as param
	// 2015-01-14 Added desktop as param
	public EventTableModel(HistoricalEventManager manager, NotificationWindow notifyBox, MainDesktopPane desktop) {
		this.manager = manager;
		this.notifyBox = notifyBox;
		this.desktop = desktop;

		desktop.setEventTableModel(this);

		// Update the cached events.
		updateCachedEvents();

		// Add this model as an event listener.
		manager.addListener(this);

	}

	private void updateCachedEvents() {
		// Clean out cached events.
		cachedEvents = new ArrayList<HistoricalEvent>();


		// Filter events based on category.
		for (int x = 0; x < manager.size(); x++) {
			HistoricalEvent event = manager.getEvent(x);
			HistoricalEventCategory category = event.getCategory();

			if (category.equals(HistoricalEventCategory.MALFUNCTION) && displayMalfunction
				&& (event.getType() == EventType.MALFUNCTION_OCCURRED
					|| event.getType() == EventType.MALFUNCTION_FIXED)) {
				cachedEvents.add(event);
			}
			
			else if (category.equals(HistoricalEventCategory.MEDICAL) && displayMedical
				&& (event.getType() == EventType.MEDICAL_STARTS
					|| event.getType() == EventType.MEDICAL_DEATH)) {
				cachedEvents.add(event);
			}
			
			else if (category.equals(HistoricalEventCategory.MISSION) && displayMission
				&& (event.getType() == EventType.MISSION_EMERGENCY_BEACON_ON
			            || event.getType() == EventType.MISSION_EMERGENCY_DESTINATION
			            || event.getType() == EventType.MISSION_RESCUE_PERSON
			            || event.getType() == EventType.MISSION_RENDEZVOUS)) {
				cachedEvents.add(event);
			}
/*
			else if (category.equals(HistoricalEventCategory.TASK) && displayTask)
				cachedEvents.add(event);

			if (category.equals(HistoricalEventCategory.TRANSPORT) && displayTransport)
				cachedEvents.add(event);
*/
		}

		// Update all table listeners.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				fireTableDataChanged();
			}
		});

	}

	/**
	 * Return the number of columns
	 * @return column count.
	 */
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * Return the type of the column requested.
	 * @param columnIndex Index of column.
	 * @return Class of specified column.
	 */
	public Class<?> getColumnClass(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnTypes.length)) {
			return columnTypes[columnIndex];
		}
		return Object.class;
	}

	/**
	 * Return the name of the column requested.
	 * @param columnIndex Index of column.
	 * @return name of specified column.
	 */
	public String getColumnName(int columnIndex) {
		if ((columnIndex >= 0) && (columnIndex < columnNames.length)) {
			return columnNames[columnIndex];
		}
		return Msg.getString("unknown"); //$NON-NLS-1$
	}

	/**
	 * Get the name of the model.
	 * @return model name.
	 */
	public String getName() {
		return Msg.getString("EventTableModel.tabName"); //$NON-NLS-1$
	}

	/**
	 * Get the number of rows in the model.
	 * @return the number of Events.
	 */
	public int getRowCount() {
		if (cachedEvents != null)
			return cachedEvents.size();
		else
			return 0;
	}

	/**
	 * Get the unit at the specified row.
	 * @param row Indexes of Unit to retrieve.
	 * @return Unit associated with the Event as the specified position.
	 */
	public Object getObject(int row) {
		HistoricalEvent event = cachedEvents.get(row);
		Object result = null;
		if (event != null) {
			Object source = event.getSource();
			if (source instanceof Unit) result = source;
			else if (source instanceof Building)
				result = ((Building) source).getBuildingManager().getSettlement();
		}
		return result;
	}

	/**
	 * Is this model already ordered according to some external criteria.
	 * @return TRUE as the events are time ordered.
	 */
	public boolean getOrdered() {
		return true;
		//return false; // 2015-01-14 if false, events will be missing and # events will be out of sync
	}

	/**
	 * Return the value of a Cell
	 * @param rowIndex Row index of the cell.
	 * @param columnIndex Column index of the cell.
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		Object result = null;

		//if (rowIndex == 0 && columnIndex == 2)
		// check if event.getCategory() == MEDICAL or MALFUNCTION
		//	System.out.println("getValueAt() : rowIndex is " + rowIndex + ", columnIndex is " + columnIndex);

		if (rowIndex < cachedEvents.size()) {
			HistoricalEvent event = cachedEvents.get(rowIndex);

			if (event != null) {
				// Invoke the appropriate method, switch is the best solution
				// although disliked by some
				switch (columnIndex) {
				case TIMESTAMP : {
					result = event.getTimestamp();
					if (result == null) // at the start of the sim, MarsClock is not ready
						result = "0015-Adir-01 000.000";
					//System.out.println("getValueAt() : timestamp is " + result);
				} break;

				case CATEGORY: {
					result = event.getCategory();
					//System.out.println("getValueAt() : category is " + result);

				} break;

				case ACTOR: {
					result = event.getSource();
					//System.out.println("getValueAt() : actor is " + result);

				} break;

				case LOCATION: {
					result = event.getLocation();
					//System.out.println("getValueAt() : actor is " + result);

				} break;
				
				case DESC : {
					result = event.getDescription();
					//System.out.println("getValueAt() : description is " + result);

				} break;

				case TYPE : {
					result = event.getType();
					//System.out.println("getValueAt() : type is " + result);

				} break;
				}
			} // end of if event
		}

		return result;
	}

	/**
	 * Gets the model count string.
	 */
	public String getCountString() {
		return " " + Msg.getString(
			"EventTableModel.numberOfEvents", //$NON-NLS-1$
			Integer.toString(cachedEvents.size())
		);
	}

	/**
	 * A new event has been added at the specified manager.
	 *
	 * @param index Index of new event in the manager.
	 * @param event The new event added.
	 */
	// 2015-05-04 Modified eventAdded to use controlsfx's notification window for javaFX UI
	public void eventAdded(int index, HistoricalEvent event) {
		// TODO: include historical events and ai.task.TaskEvent, filtered by user's options

		updateCachedEvents();
		// fireTableRowsInserted(index, index);
		if (desktop.getMainWindow() != null) {
			if (nMenu == null) {
				try {
					//MainWindowMenu mwm = desktop.getMainWindow().getMainWindowMenu();
					//NotificationMenu nMenu = mwm.getNotificationMenu();
					//nMenu = mwm.getNotificationMenu();
					nMenu = desktop.getMainWindow().getMainWindowMenu().getNotificationMenu();
				} catch (NullPointerException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}

			} else if (nMenu != null) {

				// 2015-01-14 Added noFiring condition
				//Boolean noFiring = false;

				showMedical = nMenu.getShowMedical();
				if (showMedical != showMedicalCache ) {
					showMedicalCache = showMedical;
				}

				showMalfunction = nMenu.getShowMalfunction();
				if (showMalfunction != showMalfunctionCache ) {
					showMalfunctionCache = showMalfunction;
				}

				if (!showMedical && !showMalfunction) {
					notifyBox.emptyQueue();
					noFiring = true;
				}

				if (!noFiring && index == 0 && event != null) {
					SwingUtilities.invokeLater(new NotifyBoxLauncher(event));
				}

			}

		} else if (desktop.getMainScene() != null) {

			if (mainSceneMenu == null) {
				try {
					mainSceneMenu = desktop.getMainScene().getMainSceneMenu();
				} catch (NullPointerException e) {
				}

			} else if (mainSceneMenu != null) {

				if (!noFiring && index == 0 && event != null) {

					int type = 0;
					String header = null ;
					String message = event.getDescription(); //event.getType().toString();//  //.toUpperCase();
					// reset willNotify to false
					boolean willNotify = false;

					if (message != null && message != "" ) {

					    HistoricalEventCategory category = event.getCategory();

					    if (category.equals(HistoricalEventCategory.MALFUNCTION)) {
					           // && showMalfunction ) {
					        header = Msg.getString("NotificationManager.message.malfunction"); //$NON-NLS-1$

					        // Only display notification window when malfunction has occurred, not when fixed.
					        if (event.getType() == EventType.MALFUNCTION_OCCURRED) {
					            willNotify = true;
					        }
					        
					        Malfunctionable mal = (Malfunctionable) event.getSource();
					        
					        message = message + " (" + mal.getNickName() + ") in " + mal.getLongLocationName();
					        
					        type = 0;
					    }

					    else if (category.equals(HistoricalEventCategory.MEDICAL)) {
					            // && showMedical )	{

					        header = Msg.getString("NotificationManager.message.medical"); //$NON-NLS-1$

					        // Only display notification windows when medical problems are starting or person has died.
					        if ((event.getType() == EventType.MEDICAL_STARTS) ||
					                (event.getType() == EventType.MEDICAL_DEATH)) {
					            willNotify = true;
					        }

					        type = 1;
					    }
					    
					    else if (category.equals(HistoricalEventCategory.MISSION)) {
					           // && showMalfunction ) {
					        header = Msg.getString("NotificationManager.message.mission"); //$NON-NLS-1$

					        // Only display notification window when malfunction has occurred, not when fixed.
					        if (event.getType() == EventType.MISSION_EMERGENCY_BEACON_ON
					            || event.getType() == EventType.MISSION_EMERGENCY_DESTINATION
					            || event.getType() == EventType.MISSION_RESCUE_PERSON
					            || event.getType() == EventType.MISSION_RENDEZVOUS) {
					            	willNotify = true;
					        }
					       

					        type = 2;
					    }
					    
					}

					if (willNotify)
						Platform.runLater(new NotifyFXLauncher(header, message, type));
				}
			}
		}
	}

	/**
	 * A consecutive sequence of events have been removed from the manager.
	 *
	 * @param startIndex First exclusive index of the event to be removed.
	 * @param endIndex Last exclusive index of the event to be removed..
	 */
	public void eventsRemoved(int startIndex, int endIndex) {
		updateCachedEvents();
		// fireTableRowsDeleted(startIndex, endIndex);
	}

	/**
	 * Checks if malfunction events are to be displayed.
	 * @return true if displayed
	 */
	public boolean getDisplayMalfunction() {
		return displayMalfunction;
	}

	/**
	 * Sets if malfunction events are to be displayed.
	 * @param display true if displayed
	 */
	public void setDisplayMalfunction(boolean display) {
		displayMalfunction = display;
		updateCachedEvents();
	}

	/**
	 * Checks if medical events are to be displayed.
	 * @return true if displayed
	 */
	public boolean getDisplayMedical() {
		return displayMedical;
	}

	/**
	 * Sets if medical events are to be displayed.
	 * @param display true if displayed
	 */
	public void setDisplayMedical(boolean display) {
		displayMedical = display;
		updateCachedEvents();
	}

	/**
	 * Checks if mission events are to be displayed.
	 * @return true if displayed
	 */
	public boolean getDisplayMission() {
		return displayMission;
	}

	/**
	 * Sets if mission events are to be displayed.
	 * @param display true if displayed
	 */
	public void setDisplayMission(boolean display) {
		displayMission = display;
		updateCachedEvents();
	}

	/**
	 * Checks if task events are to be displayed.
	 * @return true if displayed
	 */
	public boolean getDisplayTask() {
		return displayTask;
	}

	/**
	 * Sets if task events are to be displayed.
	 * @param display true if displayed
	 */
	public void setDisplayTask(boolean display) {
		displayTask = display;
		updateCachedEvents();
	}

	/**
	 * Checks if transport events are to be displayed.
	 * @return true if displayed
	 */
	public boolean getDisplayTransport() {
		return displayTransport;
	}

	/**
	 * Sets if transport events are to be displayed.
	 * @param display true if displayed
	 */
	public void setDisplayTransport(boolean display) {
		displayTransport = display;
		updateCachedEvents();
	}

	/**
	 * Internal class for launching a notify window.
	 */
	private class NotifyFXLauncher implements Runnable {
		private String header;
		private String message;
		private Pos pos = null;
		private int type = -1;
		ImageView v = null;


		private NotifyFXLauncher(String header, String message, int type) {
			this.header = header;
			this.message = message;
	   	    this.type = type;

			if (type == 0) {
				pos = Pos.BOTTOM_LEFT;
		   	    v = new ImageView(getClass().getResource("/icons/notification/medical_48.png").toExternalForm());

			}

			else if (type == 1) {
				pos = Pos.BOTTOM_RIGHT;
				v = new ImageView(getClass().getResource("/icons/notification/malfunction_48.png").toExternalForm());

			}

			else if (type == 2) {
				pos = Pos.TOP_RIGHT;
				v = new ImageView(getClass().getResource("/icons/notification/siren_48.png").toExternalForm());

			}
			
			else {
				pos = Pos.TOP_LEFT;

			}

		}

	    public void run() {
	    	//Notifications.create().darkStyle().title(header).text(message).position(pos).owner(desktop.getMainScene().getStage()).showWarning();

	    	if (type == 0 || type == 1 || type == 2) {
	    		
	    		int theme = MainScene.getTheme();
	    		
	    		if (theme == 7) {
			    	Notifications.create()
		    		.title(header)
		    		.text(message)
		    		.position(pos)
		    		.onAction(new EventHandler<ActionEvent>() {
		    			@Override
		    			public void handle(ActionEvent event){
		    				System.out.println("[Placeholder] The notification box '" + message + "' has just been clicked.");
		    			}
		    		})
		    		.graphic(v)
		    		.darkStyle()
		    		.owner(desktop.getMainScene().getStage())
		    		.show();
		    	}

		    	else {
			    	Notifications.create()
		    		.title(header)
		    		.text(message)
		    		.position(pos)
		    		.onAction(new EventHandler<ActionEvent>() {
		    			@Override
		    			public void handle(ActionEvent event){
		    				System.out.println("[Placeholder] You just clicked on the message '" + message + "'.");
		    			}
		    		})
		    		.graphic(v)
		    		.owner(desktop.getMainScene().getStage())
		    		.showWarning();
		    	}
	    	}
	    }
	}
	
	/**
	 * Internal class for launching a notify window.
	 */
	private class NotifyBoxLauncher implements Runnable {

	    private HistoricalEvent event;
	    private NotifyBoxLauncher(HistoricalEvent event) {
	        this.event = event;
	    }

	    public void run() {
			notifyBox.validateMsg(event);
			// Note: adding try-catch can cause UI significant slow down here
	    }
	}

	public void setNoFiring(boolean value) {
		noFiring = value;
	}

	public boolean isNoFiring() {
		return noFiring;
	}

	@Override
	public void pauseChange(boolean isPaused, boolean showPane) {
		noFiring = isPaused;
	}
	
	// 2014-12-17 Added clockPulse()
	public void clockPulse(double time) {

	}

	/**
	 * Prepares the model for deletion.
	 */
	public void destroy() {
		manager.removeListener(this);
		manager = null;
		cachedEvents.clear();
		cachedEvents = null;
		notifyBox = null;
		desktop = null;
		nMenu = null;
		mainSceneMenu = null;
	}

}
