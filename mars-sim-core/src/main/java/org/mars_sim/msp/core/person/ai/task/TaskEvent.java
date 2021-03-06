/**
 * Mars Simulation Project
 * TaskEvent.java
 * @version 3.1.0 2017-09-13
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task;

import java.io.Serializable;

import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.events.HistoricalEvent;
import org.mars_sim.msp.core.events.HistoricalEventCategory;
import org.mars_sim.msp.core.person.EventType;

/**
 * This class represents the historical actions involving tasks.
 */
public class TaskEvent
extends HistoricalEvent implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * @param person The person performing the task.
	 * @param task The task with the event.
	 * @param eventType The type of event.
	 * @param location The location of the event.
	 * @param description Further description of the event (may be empty string).
	 */
	public TaskEvent(Unit unit, Task task, EventType eventType, String location, String description) {
	
		// Use HistoricalEvent constructor.
		super(HistoricalEventCategory.TASK, 
				eventType, 
				unit, 
				location, 
				task.getDescription() + " " + description);
	}
}
