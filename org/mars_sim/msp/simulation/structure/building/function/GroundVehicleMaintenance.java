/**
 * Mars Simulation Project
 * GroundVehicleMaintenance.java
 * @version 2.76 2004-06-02
 * @author Scott Davis
 */
package org.mars_sim.msp.simulation.structure.building.function;
 
import java.io.Serializable;
import org.mars_sim.msp.simulation.*;
import org.mars_sim.msp.simulation.structure.building.*;
 
/**
 * The GroundVehicleMaintenance class is a building function for a building
 * capable of maintaining ground vehicles.
 */
public class GroundVehicleMaintenance extends VehicleMaintenance implements Serializable {
    
    public static final String NAME = "Ground Vehicle Maintenance";
    
    /**
     * Constructor
     * @param building the building the function is for.
     * @throws BuildingException if error in construction.
     */
    public GroundVehicleMaintenance(Building building) throws BuildingException {
    	// Call VehicleMaintenance constructor.
    	super(NAME, building);
    	
		BuildingConfig config = SimulationConfig.instance().getBuildingConfiguration();
		
		try {
			vehicleCapacity = config.getVehicleCapacity(building.getName());
		}
		catch (Exception e) {
			throw new BuildingException("GroundVehicleMaintenance.constructor: " + e.getMessage());
		}
    }
}