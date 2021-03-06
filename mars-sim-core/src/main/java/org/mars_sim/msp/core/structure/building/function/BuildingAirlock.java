/**
 * Mars Simulation Project
 * BuildingAirlock.java
 * @version 3.1.0 2017-03-09
 * @author Scott Davis
 */

package org.mars_sim.msp.core.structure.building.function;

import java.awt.geom.Point2D;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Airlock;
import org.mars_sim.msp.core.Inventory;
import org.mars_sim.msp.core.LocalAreaUtil;
import org.mars_sim.msp.core.Unit;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.structure.CompositionOfAir;
import org.mars_sim.msp.core.structure.Settlement;
import org.mars_sim.msp.core.structure.building.Building;
import org.mars_sim.msp.core.structure.building.BuildingManager;

/**
 * The BuildingAirlock class represents an airlock for a building.
 */
public class BuildingAirlock extends Airlock {

    /** default serial id. */
    private static final long serialVersionUID = 1L;

    private static Logger logger = Logger.getLogger(BuildingAirlock.class.getName());
	
    //private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

	public static final double HEIGHT = 2; // assume an uniform height of 2 meters in all airlocks
	
	/** The volume of an airlock in cubic meters. */
	public static final double AIRLOCK_VOLUME_IN_CM = 12D; //3 * 2 * 2; //in m^3
	
    // Data members.
	private Settlement settlement;
    private Building building; // The building this airlock is for.
    private Inventory inv;
    private CompositionOfAir air;
    private Heating heating;
    
    private Point2D airlockInsidePos;
    private Point2D airlockInteriorPos;
    private Point2D airlockExteriorPos;

    /**
     * Constructor
     * @param building the building this airlock of for.
     * @param capacity number of people airlock can hold.
     */
    public BuildingAirlock(Building building, int capacity, double xLoc, double yLoc,
            double interiorXLoc, double interiorYLoc, double exteriorXLoc, double exteriorYLoc) {
        // User Airlock constructor
        super(capacity);

        this.building = building;
        
        settlement = building.getBuildingManager().getSettlement();

        inv = building.getSettlementInventory();
        
        //air = building.getSettlement().getCompositionOfAir();
        
        //heating = building.getThermalGeneration().getHeating();
        
        //if (building == null) {
        //    throw new IllegalArgumentException("building is null.");
        //}

        // Determine airlock interior position.
        airlockInteriorPos = LocalAreaUtil.getLocalRelativeLocation(interiorXLoc, interiorYLoc, building);

        // Determine airlock exterior position.
        airlockExteriorPos = LocalAreaUtil.getLocalRelativeLocation(exteriorXLoc, exteriorYLoc, building);

        // Determine airlock inside position.
        airlockInsidePos = LocalAreaUtil.getLocalRelativeLocation(xLoc, yLoc, building);
    }

    protected void exitAirlock(Person person) {
        //Inventory inv = building.getSettlementInventory();

        if (inAirlock(person)) {

            if (PRESSURIZED.equals(getState())) { 
            	// check if the airlock has been sealed from outside and pressurized, ready to 
            	// open the inner door to release the person into the settlement
            	
                // Pump air into the airlock to make it breathable
            	if (air == null)
            		air = building.getSettlement().getCompositionOfAir();
                air.pumpOrRecaptureAir(building.getInhabitableID(), true, building);

                if (person.isOutside()) {
                	//logger.fine(
                	//LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, person 
                	//		+ " has got inside the airlock at " + building + " in " 
                	//		+ building.getBuildingManager().getSettlement()
                	//		+ ". The airlock has been pressurized and is ready to open the inner door to release the person. ", null);
                	inv.storeUnit(person);
                    BuildingManager.addPersonOrRobotToBuildingSameLocation(person, building);

                }
                else {
                	//if (LocationSituation.BURIED != person.getLocationSituation()) {
                    throw new IllegalStateException(person + " is entering " + getEntityName() +
                            " from an airlock but is not outside.");
                }
            }
            
            else if (DEPRESSURIZED.equals(getState())) { 
            	// check if the airlock has been depressurized, ready to open the outer door to 
            	// get exposed to the outside air and release the person
            	
                // Upon depressurization, there is heat loss to the Martian air in Heating class
            	if (heating == null)
            		heating = building.getThermalGeneration().getHeating();
                heating.flagHeatLostViaAirlockOuterDoor(true);
                
                // Recapture air from the airlock before depressurizing it
            	if (air == null)
            		air = building.getSettlement().getCompositionOfAir();
                air.pumpOrRecaptureAir(building.getInhabitableID(), false, building);
                
                if (person.isInSettlement()) {
                   	//logger.fine(
                	//LogConsolidated.log(logger, Level.SEVERE, 5000, sourceName, person 
                	//		+ " has got inside the airlock at " + building + " in " 
                	//		+ building.getBuildingManager().getSettlement()
                	//		+ ". The airlock has been depressurized and is ready to open the outer door to release the person. ", null);
                    inv.retrieveUnit(person);
                    BuildingManager.removePersonOrRobotFromBuilding(person, building);

                    // Recapture air from the airlock and store for us
                    air.pumpOrRecaptureAir(building.getInhabitableID(), true, building);

                }
                else {
                	//if (LocationSituation.BURIED != person.getLocationSituation()) {
                    throw new IllegalStateException(person + " is exiting " + getEntityName() +
                            " from an airlock but is not inside.");
                }
            }
            else {
                logger.severe("Building airlock in incorrect state for exiting: " + getState());
            }
        }
        else {
            throw new IllegalStateException(person.getName() + " not in airlock of " + getEntityName());
        }
    }

    protected void exitAirlock(Robot robot) {

    }
    
    
    @Override
    public String getEntityName() {
        return building.getNickName() + " in " + settlement.getName();

    }

    @Override
    public Inventory getEntityInventory() {
        return inv;//building.getSettlementInventory();
    }

    @Override
    public Object getEntity() {
        return building;
    }

    @Override
    public Point2D getAvailableInteriorPosition() {
        return airlockInteriorPos;
    }

    @Override
    public Point2D getAvailableExteriorPosition() {
        return airlockExteriorPos;
    }

    @Override
    public Point2D getAvailableAirlockPosition() {
        return airlockInsidePos;
    }

	@Override
	protected void exitAirlock(Unit occupant) {

        Person person = null;
        Robot robot = null;

        if (occupant instanceof Person) {
         	person = (Person) occupant;
         	exitAirlock(person);

        }
        else if (occupant instanceof Robot) {
        	robot = (Robot) occupant;
        	exitAirlock(robot);

        }
	}
	
	public void destroy() {
		settlement = null;
	    building = null;
	    inv = null;
	    air = null;
	    heating = null; 
	    airlockInsidePos = null;
	    airlockInteriorPos = null;
	    airlockExteriorPos = null;
	}
	
}