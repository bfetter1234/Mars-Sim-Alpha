/**
 * Mars Simulation Project
 * Inventory.java
 * @version 3.1.0 2017-11-06
 * @author Scott Davis
 */
package org.mars_sim.msp.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.mars_sim.msp.core.equipment.Container;
import org.mars_sim.msp.core.resource.AmountResource;
import org.mars_sim.msp.core.resource.AmountResourceStorage;
import org.mars_sim.msp.core.resource.ItemResource;
import org.mars_sim.msp.core.resource.PhaseType;
import org.mars_sim.msp.core.resource.ResourceUtil;

/**
 * The Inventory class represents what a unit
 * contains in terms of resources and other units.
 * It has methods for adding, removing and querying
 * what the unit contains.
 * TODO please reduce the textual error messages to absolute minimum to aid in translation.
 */
public class Inventory
implements Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 123L;

    //private static Logger logger = Logger.getLogger(Inventory.class.getName());
    //private static String sourceName = logger.getName().substring(logger.getName().lastIndexOf(".") + 1, logger.getName().length());

    /** Comparison to indicate a small but non-zero amount. */
    private static final double SMALL_AMOUNT_COMPARISON = .0000001D;

    // Data members
    /** The unit that owns this inventory. */
    private Unit owner;
    /** Collection of units in inventory. */
    private Collection<Unit> containedUnits = null;
    /** Map of item resources. */
    private Map<ItemResource, Integer> containedItemResources = null;
    /** General mass capacity of inventory. */
    private double generalCapacity = 0D;
    /** Resource storage. */
    private AmountResourceStorage resourceStorage = new AmountResourceStorage();

    // Cache capacity variables.
/*    
    private transient Map<AmountResource, Double> amountResourceCapacityCache = null;
    private transient Map<AmountResource, Boolean> amountResourceCapacityCacheDirty = null;
    private transient Map<AmountResource, Double> amountResourceContainersCapacityCache = null;
    private transient Map<AmountResource, Boolean> amountResourceContainersCapacityCacheDirty = null;
    private transient Map<AmountResource, Double> amountResourceStoredCache = null;
    private transient Map<AmountResource, Boolean> amountResourceStoredCacheDirty = null;
    private transient Map<AmountResource, Double> amountResourceContainersStoredCache = null;
    private transient Map<AmountResource, Boolean> amountResourceContainersStoredCacheDirty = null;
*/    
    //private transient Set<AmountResource> allStoredAmountResourcesCache = null;
    
    private transient Map<Integer, Double> capacityCache = null;
    private transient Map<Integer, Boolean> capacityCacheDirty = null;
    private transient Map<Integer, Double> containersCapacityCache = null;
    private transient Map<Integer, Boolean> containersCapacityCacheDirty = null;
    private transient Map<Integer, Double> storedCache = null;
    private transient Map<Integer, Boolean> storedCacheDirty = null;
    private transient Map<Integer, Double> containersStoredCache = null;
    private transient Map<Integer, Boolean> containersStoredCacheDirty = null;
    private transient Set<Integer> allStoredARCache = null;
        
    private transient boolean allStoredAmountResourcesCacheDirty = true;
    private transient double totalAmountResourcesStoredCache;
    private transient boolean totalAmountResourcesStoredCacheDirty = true;
    private transient double itemResourceTotalMassCache;
    private transient boolean itemResourceTotalMassCacheDirty = true;
    private transient double unitTotalMassCache;
    private transient boolean unitTotalMassCacheDirty = true;
    private transient double totalInventoryMassCache;
    private transient boolean totalInventoryMassCacheDirty = true;

    // TODO: Switch to using parallel operation in ConcurrentHashMap instead of HashMap.
    // see https://dzone.com/articles/concurrenthashmap-in-java8
    // see https://dzone.com/articles/how-concurrenthashmap-works-internally-in-java
    // see https://dzone.com/articles/concurrenthashmap-isnt-always-enough

	// Add 3 amount resource demand maps
	private Map<String, Integer> amountDemandTotalRequestMap =  new HashMap<String, Integer>();
	private Map<String, Integer> amountDemandMetRequestMap =  new HashMap<String, Integer>();
	private Map<String, Double> amountDemandMap = new HashMap<String, Double>();
	// Add 2 amount resource supply maps
	private Map<String, Double> amountSupplyMap =  new HashMap<String, Double>();
	private Map<String, Integer> amountSupplyRequestMap =  new HashMap<String, Integer>();
	// Add 2 item resource demand maps
	private Map<String, Integer> itemDemandMetRequestMap =  new HashMap<String, Integer>();
	private Map<String, Integer> itemDemandMap = new HashMap<String, Integer>();
	// Add 2 item resource supply maps
	//private Map<String, Integer> itemSupplyMap =  new HashMap<String, Integer>();
	//private Map<String, Integer> itemSupplyRequestMap =  new HashMap<String, Integer>();

    /**
     * Constructor
     * @param owner the unit that owns this inventory
     */
    public Inventory(Unit owner) {
        // Set owning unit.
        this.owner = owner;
    }

    public int getAmountSupplyRequest(String resourceName) {
       	int result;
    	String r = resourceName.toLowerCase();

       	if (amountSupplyRequestMap.containsKey(r)) {
       		result = amountSupplyRequestMap.get(r);
    	}
    	else {
    		amountSupplyRequestMap.put(r, 0);
    		result = 0;
    	}
    	return result;
    }

    public double getAmountSupplyAmount(String resourceName) {
    	double result;
    	String r = resourceName.toLowerCase();

       	if (amountSupplyMap.containsKey(r)) {
       		result = amountSupplyMap.get(r);
    	}
    	else {
    		amountSupplyMap.put(r, 0.0);
    		result = 0.0;
    	}
    	return result;
    }


   	public void addAmountSupplyAmount(AmountResource resource, double amount) {
   		String r = resource.getName();

    	if (amountSupplyMap.containsKey(r)) {

    		double oldAmount = amountSupplyMap.get(r);
    		amountSupplyMap.put(r, amount + oldAmount);

    	}
    	else {
    		amountSupplyMap.put(r, amount);
    	}

    	addAmountSupplyRequest(resource, amount);
   	}

   	public void addAmountSupplyRequest(AmountResource resource, double amount) {
   		String r = resource.getName();

    	if (amountSupplyRequestMap.containsKey(r)) {
    		int oldNum = amountSupplyRequestMap.get(r);
			//System.out.println( resource.getName() + " demandSuccessful : " + oldNum+1);
    		amountSupplyRequestMap.put(r, oldNum + 1);
    	}

    	else {
    		amountSupplyRequestMap.put(r, 1);
    	}
	}

    public double getAmountDemandAmount(String resourceName) {
    	double result;
    	String r = resourceName.toLowerCase();

       	if (amountDemandMap.containsKey(r)) {
       		result = amountDemandMap.get(r);
    	}
    	else {
    		amountDemandMap.put(r, 0.0);
    		result = 0.0;
    	}
    	return result;
    }

    public int getAmountDemandTotalRequest(String resourceName) {
       	int result;
    	String r = resourceName.toLowerCase();

       	if (amountDemandTotalRequestMap.containsKey(r)) {
       		result = amountDemandTotalRequestMap.get(r);
    	}
    	else {
    		amountDemandTotalRequestMap.put(r, 0);
    		result = 0;
    	}
    	return result;
    }

    public int getAmountDemandMetRequest(String resourceName) {
       	int result;
    	String r = resourceName.toLowerCase();

       	if (amountDemandMetRequestMap.containsKey(r)) {
       		result = amountDemandMetRequestMap.get(r);
    	}
    	else {
    		amountDemandMetRequestMap.put(r, 0);
    		result = 0;
    	}

    	return result;
    }

    public int getAmountDemandAmountMapSize() {
    	return amountDemandMap.size();
    }

    public int getDemandTotalRequestMapSize() {
    	return amountDemandTotalRequestMap.size();
    }

    public int getAmountDemandMetRequestMapSize() {
    	return amountDemandMetRequestMap.size();
    }

    public void compactAmountSupplyAmountMap(int sol) {
    	compactMap(amountSupplyMap, sol);
    }

    public void clearAmountSupplyRequestMap() {
    	amountSupplyRequestMap.clear();
    }

    public void clearAmountDemandAmountMap() {
    	amountDemandMap.clear();
    }

    public void compactAmountDemandAmountMap(int sol) {
    	compactMap(amountDemandMap, sol);
    }

    public void compactMap(Map<String, Double> amountMap, int sol) {

    	Map<String, Double> map = amountMap;

    	for (Map.Entry<String, Double> entry : map.entrySet()) {
    	    String key = entry.getKey();
    	    double value = entry.getValue();
    	    value = value / sol;
    	    map.put(key, value);
    	}
    }

    public void clearAmountDemandTotalRequestMap() {
    	amountDemandTotalRequestMap.clear();
    }

    public void clearAmountDemandMetRequestMap() {
    	amountDemandMetRequestMap.clear();
    }

	public void addAmountDemandTotalRequest(AmountResource resource) {
   		String r = resource.getName();

		if (amountDemandTotalRequestMap.containsKey(r)) {

			int oldNum = amountDemandTotalRequestMap.get(r);
			//System.out.println( resource.getName() + " demandTotal : " + oldNum+1);
			amountDemandTotalRequestMap.put(r, oldNum + 1);
		}
		else
	    	amountDemandTotalRequestMap.put(r, 1);
	}

	public void addAmountDemandTotalRequest(int resource) {
   		String r = ResourceUtil.findAmountResource(resource).getName();

		if (amountDemandTotalRequestMap.containsKey(r)) {

			int oldNum = amountDemandTotalRequestMap.get(r);
			//System.out.println( resource.getName() + " demandTotal : " + oldNum+1);
			amountDemandTotalRequestMap.put(r, oldNum + 1);
		}
		else
	    	amountDemandTotalRequestMap.put(r, 1);
	}
	
	/**
	 * Adds the demand of this resource. It prompts for raising its value point (VP).
	 * @param resource
	 * @param amount
	 */
	public void addAmountDemand(AmountResource resource, double amount) {
   		String r = resource.getName();

    	if (amountDemandMap.containsKey(r)) {

    		double oldAmount = amountDemandMap.get(r);
			//System.out.println( resource.getName() + " demandReal : " + amount + oldAmount);
    		amountDemandMap.put(r, amount + oldAmount);

    	}
    	else {
    		amountDemandMap.put(r, amount);
    	}

    	addAmountDemandMetRequest(resource, amount);
   	}

	/**
	 * Adds the demand of this resource. It prompts for raising its value point (VP).
	 * @param resource
	 * @param amount
	 */
	public void addAmountDemand(int resource, double amount) {
   		String r = ResourceUtil.findAmountResource(resource).getName();
 
    	if (amountDemandMap.containsKey(r)) {

    		double oldAmount = amountDemandMap.get(r);
			//System.out.println( resource.getName() + " demandReal : " + amount + oldAmount);
    		amountDemandMap.put(r, amount + oldAmount);

    	}
    	else {
    		amountDemandMap.put(r, amount);
    	}

    	addAmountDemandMetRequest(resource, amount);
   	}	
	
   	public void addItemDemand(ItemResource resource, int number) {
   		String r = resource.getName();

    	if (itemDemandMap.containsKey(r)) {

    		int oldNumber = itemDemandMap.get(r);
			//System.out.println( resource.getName() + " demandReal : " + amount + oldAmount);
    		itemDemandMap.put(r, number + oldNumber);

    	}
    	else {
    		itemDemandMap.put(r, number);
    	}

    	addItemDemandMetRequest(resource, number);
   	}

   	public void addAmountDemandMetRequest(AmountResource resource, double amount) {
   		String r = resource.getName();

    	if (amountDemandMetRequestMap.containsKey(r)) {
    		int oldNum = amountDemandMetRequestMap.get(r);
			//System.out.println( resource.getName() + " demandSuccessful : " + oldNum+1);
    		amountDemandMetRequestMap.put(r, oldNum + 1);
    	}

    	else {
    		amountDemandMetRequestMap.put(r, 1);
    	}
	}
   	
	
   	public void addAmountDemandMetRequest(int resource, double amount) {
   		String r = ResourceUtil.findAmountResource(resource).getName();
   		
    	if (amountDemandMetRequestMap.containsKey(r)) {
    		int oldNum = amountDemandMetRequestMap.get(r);
			//System.out.println( resource.getName() + " demandSuccessful : " + oldNum+1);
    		amountDemandMetRequestMap.put(r, oldNum + 1);
    	}

    	else {
    		amountDemandMetRequestMap.put(r, 1);
    	}
	}
   	
   	public void addItemDemandMetRequest(ItemResource resource, double number) {
   		String r = resource.getName();

    	if (itemDemandMetRequestMap.containsKey(r)) {
    		int oldNum = itemDemandMetRequestMap.get(r);
			//System.out.println( resource.getName() + " demandSuccessful : " + oldNum+1);
    		itemDemandMetRequestMap.put(r, oldNum + 1);
    	}

    	else {
    		itemDemandMetRequestMap.put(r, 1);
    	}
	}

    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the extra capacity amount (kg).
     */
    public void addAmountResourceTypeCapacity(AmountResource resource, double capacity) {
    	addARTypeCapacity(resource.getID(), capacity);
/*    	
        // Set capacity cache to dirty because capacity values are changing.
        setAmountResourceCapacityCacheDirty(resource);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.addAmountResourceTypeCapacity(resource, capacity);
*/        
    }

    /**
     * Adds capacity for a resource type.
     * @param resource the resource.
     * @param capacity the extra capacity amount (kg).
     */
    public void addARTypeCapacity(int resource, double capacity) {
    	//AmountResource ar = ResourceUtil.findAmountResource(resource);
        // Set capacity cache to dirty because capacity values are changing.
        setARCapacityCacheDirty(resource);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.addARTypeCapacity(resource, capacity);
    }
    
    /**
     * Removes capacity for a resource type.
     * @param resource the resource
     * @param capacity the capacity amount (kg).
     */
    public void removeAmountResourceTypeCapacity(AmountResource resource,
            double capacity) {

    	removeARTypeCapacity(resource.getID(), capacity);
 /*   	
        // Set capacity cache to dirty because capacity values are changing.
        setAmountResourceCapacityCacheDirty(resource);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.removeAmountResourceTypeCapacity(resource, capacity);
 */       
    }

    /**
     * Removes capacity for a resource type.
     * @param resource the resource
     * @param capacity the capacity amount (kg).
     */
    public void removeARTypeCapacity(int resource, double capacity) {

        // Set capacity cache to dirty because capacity values are changing.
        setARCapacityCacheDirty(resource);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.removeAmountResourceTypeCapacity(resource, capacity);
    }
    
    /**
     * Adds capacity for a resource phase.
     * @param phase the phase
     * @param capacity the capacity amount (kg).
     */
    public void addAmountResourcePhaseCapacity(PhaseType phase, double capacity) {
        // Set capacity cache to all dirty because capacity values are changing.
        setAmountResourceCapacityCacheAllDirty(false);
        // Initialize resource storage if necessary.
        if (resourceStorage == null) {
            resourceStorage = new AmountResourceStorage();
        }
        resourceStorage.addAmountResourcePhaseCapacity(phase, capacity);
    }

    /**
     * Checks if storage has capacity for a resource.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource, boolean allowDirty) {
        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
        return (getAmountResourceCapacityCacheValue(resource, allowDirty) > 0D);
    }

    /**
     * Checks if storage has capacity for an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if storage capacity.
     */
    public boolean hasAmountResourceCapacity(AmountResource resource, double amount,
            boolean allowDirty) {

        if (resource == null) {
            throw new IllegalArgumentException("resource cannot be null.");
        }
        if (amount < 0D) {
            throw new IllegalArgumentException("amount cannot be a negative value.");
        }
        return (getAmountResourceCapacityCacheValue(resource, allowDirty) >= amount);
    }

    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return capacity amount (kg).
     */
    public double getAmountResourceCapacity(AmountResource resource, boolean allowDirty) {
        return getAmountResourceCapacityCacheValue(resource, allowDirty);
    }

    /**
     * Gets the storage capacity for a resource.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return capacity amount (kg).
     */
    public double getARCapacity(int resource, boolean allowDirty) {
        return getAmountResourceCapacityCacheValue(ResourceUtil.findAmountResource(resource), allowDirty);
    }
    
    /**
     * Gets the storage capacity for a resource not counting containers.
     * @param resource the resource.
     * @return capacity amount (kg).
     */
    public double getAmountResourceCapacityNoContainers(AmountResource resource) {
        double result = 0D;

        if (resourceStorage != null) {
            result = resourceStorage.getAmountResourceCapacity(resource);
        }

        return result;
    }

    /**
     * Gets the amount of a resource stored.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored amount (kg).
     */
    public double getAmountResourceStored(AmountResource resource, boolean allowDirty) {
        return getAmountResourceStoredCacheValue(resource, allowDirty);
    }
    
    /**
     * Gets the amount of a resource stored.
     * @param resource the resource.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored amount (kg).
     */
    public double getARStored(int resource, boolean allowDirty) {
        return getAmountResourceStoredCacheValue(ResourceUtil.findAmountResource(resource), allowDirty);
    }
    
    /**
     * Gets all of the amount resources stored.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return set of amount resources.
     */
    public Set<AmountResource> getAllAmountResourcesStored(boolean allowDirty) {
        return new HashSet<AmountResource>(getAllStoredAmountResourcesCache(allowDirty));
    }

    /**
     * Gets all of the amount resources stored.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return set of amount resources.
     */
    public Set<Integer> getAllARStored(boolean allowDirty) {
        return new HashSet<Integer>(getAllStoredARCache(allowDirty));
    }
    
    /**
     * Gets the total mass of amount resources stored.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored amount (kg).
     */
    private double getTotalAmountResourcesStored(boolean allowDirty) {
        return getTotalAmountResourcesStoredCache(allowDirty);
    }

    /**
     * Gets the remaining capacity available for a resource.
     * @param resource the resource.
     * @param useContainedUnits should the capacity of contained units be added?
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return remaining capacity amount (kg).
     */
    public double getAmountResourceRemainingCapacity(AmountResource resource,
            boolean useContainedUnits, boolean allowDirty) {

        double result = 0D;

        if (useContainedUnits) {
            double capacity = getAmountResourceCapacity(resource, allowDirty);
            double stored = getAmountResourceStored(resource, allowDirty);
            result += capacity - stored;
        } else if (resourceStorage != null) {
            result += resourceStorage.getAmountResourceRemainingCapacity(resource);
        }

        // Check if remaining capacity exceeds container unit's remaining general capacity.
        double containerUnitLimit = getContainerUnitGeneralCapacityLimit(allowDirty);
        if (result > containerUnitLimit) {
            result = containerUnitLimit;
        }

        return result;
    }

    /**
     * Gets the remaining capacity available for a resource.
     * @param resource the resource.
     * @param useContainedUnits should the capacity of contained units be added?
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return remaining capacity amount (kg).
     */
    public double getARRemainingCapacity(int resource,
            boolean useContainedUnits, boolean allowDirty) {

        double result = 0D;

        if (useContainedUnits) {
            double capacity = getARCapacity(resource, allowDirty);
            double stored = getARStored(resource, allowDirty);
            result += capacity - stored;
        } else if (resourceStorage != null) {
            result += resourceStorage.getARRemainingCapacity(resource);
        }

        // Check if remaining capacity exceeds container unit's remaining general capacity.
        double containerUnitLimit = getContainerUnitGeneralCapacityLimit(allowDirty);
        if (result > containerUnitLimit) {
            result = containerUnitLimit;
        }

        return result;
    }
    
    /**
     * Store an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @param useContainedUnits
     */
    public void storeAmountResource(AmountResource resource, double amount,
            boolean useContainedUnits) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
        }

        if (amount > 0D) {

            if (amount <= getAmountResourceRemainingCapacity(resource, useContainedUnits, false)) {

                // Set modified cache values as dirty.
                setAmountResourceCapacityCacheAllDirty(false);
                setAmountResourceStoredCacheAllDirty(false);
                setAllStoredAmountResourcesCacheDirty();
                setTotalAmountResourcesStoredCacheDirty();

                double remainingAmount = amount;
                double remainingStorageCapacity = 0D;
                if (resourceStorage != null) {
                    remainingStorageCapacity += resourceStorage.getAmountResourceRemainingCapacity(resource);
                }

                // Check if local resource storage can hold resources if not using contained units.
                if (!useContainedUnits && (remainingAmount > remainingStorageCapacity)) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally stored. Remaining: " + (remainingAmount -
                                    remainingStorageCapacity));
                }

                // Store resource in local resource storage.
                double storageAmount = remainingAmount;
                if (storageAmount > remainingStorageCapacity) {
                    storageAmount = remainingStorageCapacity;
                }
                if ((storageAmount > 0D) && (resourceStorage != null)) {
                    resourceStorage.storeAmountResource(resource, storageAmount);
                    remainingAmount -= storageAmount;
                }

                // Store remaining resource in contained units in general capacity.
                if (useContainedUnits && (remainingAmount > 0D) && (containedUnits != null)) {
                    for (Unit unit : containedUnits) {
                        // Use only contained units that implement container interface.
                        if (unit instanceof Container) {
                            Inventory unitInventory = unit.getInventory();
                            double remainingUnitCapacity = unitInventory.getAmountResourceRemainingCapacity(
                                    resource, false, false);
                            double unitStorageAmount = remainingAmount;
                            if (unitStorageAmount > remainingUnitCapacity) {
                                unitStorageAmount = remainingUnitCapacity;
                            }
                            if (unitStorageAmount > 0D) {
                                unitInventory.storeAmountResource(resource, unitStorageAmount, false);
                                remainingAmount -= unitStorageAmount;
                            }
                        }
                    }
                }

                if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally stored. Remaining: " + remainingAmount);
                }

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
                }
            } else {
                throw new IllegalStateException("Insufficient capacity to store " + resource.getName() +
                        ", capacity: " + getAmountResourceRemainingCapacity(resource, useContainedUnits,
                                false) + ", attempted: " + amount);
            }
        }
    }

    
    /**
     * Store an amount of a resource.
     * @param resource the resource.
     * @param amount the amount (kg).
     * @param useContainedUnits
     */
    public void storeAR(int resource, double amount, boolean useContainedUnits) {
        AmountResource ar = ResourceUtil.findAmountResource(resource);
        
        if (amount < 0D) {
            throw new IllegalStateException("Cannot store negative amount of resource: " + amount);
        }

        if (amount > 0D) {

            if (amount <= getARRemainingCapacity(resource, useContainedUnits, false)) {

                // Set modified cache values as dirty.
                setAmountResourceCapacityCacheAllDirty(false);
                setAmountResourceStoredCacheAllDirty(false);
                setAllStoredAmountResourcesCacheDirty();
                setTotalAmountResourcesStoredCacheDirty();

                double remainingAmount = amount;
                double remainingStorageCapacity = 0D;
                if (resourceStorage != null) {
                    remainingStorageCapacity += resourceStorage.getARRemainingCapacity(resource);
                }

                // Check if local resource storage can hold resources if not using contained units.
                if (!useContainedUnits && (remainingAmount > remainingStorageCapacity)) {
                    throw new IllegalStateException(ar.getName()
                            + " could not be totally stored. Remaining: " + (remainingAmount -
                                    remainingStorageCapacity));
                }

                // Store resource in local resource storage.
                double storageAmount = remainingAmount;
                if (storageAmount > remainingStorageCapacity) {
                    storageAmount = remainingStorageCapacity;
                }
                if ((storageAmount > 0D) && (resourceStorage != null)) {
                    resourceStorage.storeAmountResource(resource, storageAmount);
                    remainingAmount -= storageAmount;
                }

                // Store remaining resource in contained units in general capacity.
                if (useContainedUnits && (remainingAmount > 0D) && (containedUnits != null)) {
                    for (Unit unit : containedUnits) {
                        // Use only contained units that implement container interface.
                        if (unit instanceof Container) {
                            Inventory unitInventory = unit.getInventory();
                            double remainingUnitCapacity = unitInventory.getARRemainingCapacity(
                                    resource, false, false);
                            double unitStorageAmount = remainingAmount;
                            if (unitStorageAmount > remainingUnitCapacity) {
                                unitStorageAmount = remainingUnitCapacity;
                            }
                            if (unitStorageAmount > 0D) {
                                unitInventory.storeAR(resource, unitStorageAmount, false);
                                remainingAmount -= unitStorageAmount;
                            }
                        }
                    }
                }

                if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
                    throw new IllegalStateException(ar.getName()
                            + " could not be totally stored. Remaining: " + remainingAmount);
                }

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, ar);
                }
            } else {
                throw new IllegalStateException("Insufficient capacity to store " + ar.getName() +
                        ", capacity: " + getARRemainingCapacity(resource, useContainedUnits,
                                false) + ", attempted: " + amount);
            }
        }
    }

    
    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    public void retrieveAmountResource(AmountResource resource, double amount) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot retrieve negative amount of resource: " + amount);
        }

        if (amount > 0D ) {

            if (amount <= getAmountResourceStored(resource, false)) {

                // Set modified cache values as dirty.
                setAmountResourceCapacityCacheAllDirty(false);
                setAmountResourceStoredCacheAllDirty(false);
                setAllStoredAmountResourcesCacheDirty();
                setTotalAmountResourcesStoredCacheDirty();

                double remainingAmount = amount;

                // Retrieve from local resource storage.
                double resourceStored = 0D;
                if (resourceStorage != null) {
                    resourceStored += resourceStorage.getAmountResourceStored(resource);
                }
                double retrieveAmount = remainingAmount;
                if (retrieveAmount > resourceStored) {
                    retrieveAmount = resourceStored;
                }
                if ((retrieveAmount > 0D) && (resourceStorage != null)) {
                    resourceStorage.retrieveAmountResource(resource, retrieveAmount);
                    remainingAmount -= retrieveAmount;
                }

                // Retrieve remaining resource from contained units.
                if ((remainingAmount > 0D) && (containedUnits != null)) {
                    for (Unit unit : containedUnits) {
                        if (unit instanceof Container) {
                            Inventory unitInventory = unit.getInventory();
                            double unitResourceStored = unitInventory.getAmountResourceStored(resource,
                                    false);
                            double unitRetrieveAmount = remainingAmount;
                            if (unitRetrieveAmount > unitResourceStored) {
                                unitRetrieveAmount = unitResourceStored;
                            }
                            if (unitRetrieveAmount > 0D) {
                                unitInventory.retrieveAmountResource(resource, unitRetrieveAmount);
                                remainingAmount -= unitRetrieveAmount;
                            }
                        }
                    }
                }

                if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally retrieved. Remaining: " + remainingAmount);
                }

                // Update caches.
                updateAmountResourceCapacityCache(resource);
                updateAmountResourceStoredCache(resource);

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
                }
            } else {
                throw new IllegalStateException("Insufficient stored amount to retrieve " +
                        resource.getName() + ". Storage Amount : " + getAmountResourceStored(resource, false) +
                        " kg. Attempted Amount : " + amount + " kg");
            }
        }
    }
    

    /**
     * Retrieves an amount of a resource from storage.
     * @param resource the resource.
     * @param amount the amount (kg).
     */
    public void retrieveAR(int resource, double amount) {

        if (amount < 0D) {
            throw new IllegalStateException("Cannot retrieve negative amount of resource: " + amount);
        }

        
        if (amount > 0D ) {

            AmountResource ar = ResourceUtil.findAmountResource(resource);
            
            if (amount <= getARStored(resource, false)) {

                // Set modified cache values as dirty.
                setAmountResourceCapacityCacheAllDirty(false);
                setAmountResourceStoredCacheAllDirty(false);
                setAllStoredAmountResourcesCacheDirty();
                setTotalAmountResourcesStoredCacheDirty();

                double remainingAmount = amount;

                // Retrieve from local resource storage.
                double resourceStored = 0D;
                if (resourceStorage != null) {
                    resourceStored += resourceStorage.getARStored(resource);
                }
                double retrieveAmount = remainingAmount;
                if (retrieveAmount > resourceStored) {
                    retrieveAmount = resourceStored;
                }
                if ((retrieveAmount > 0D) && (resourceStorage != null)) {
                    resourceStorage.retrieveAR(resource, retrieveAmount);
                    remainingAmount -= retrieveAmount;
                }

                // Retrieve remaining resource from contained units.
                if ((remainingAmount > 0D) && (containedUnits != null)) {
                    for (Unit unit : containedUnits) {
                        if (unit instanceof Container) {
                            Inventory unitInventory = unit.getInventory();
                            double unitResourceStored = unitInventory.getARStored(resource,
                                    false);
                            double unitRetrieveAmount = remainingAmount;
                            if (unitRetrieveAmount > unitResourceStored) {
                                unitRetrieveAmount = unitResourceStored;
                            }
                            if (unitRetrieveAmount > 0D) {
                                unitInventory.retrieveAR(resource, unitRetrieveAmount);
                                remainingAmount -= unitRetrieveAmount;
                            }
                        }
                    }
                }
   
                if (remainingAmount > SMALL_AMOUNT_COMPARISON) {
                    throw new IllegalStateException(ar.getName()
                            + " could not be totally retrieved. Remaining: " + remainingAmount);
                }

                // Update caches.
                updateAmountResourceCapacityCache(ar);
                updateAmountResourceStoredCache(ar);

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, ar);
                }
            } else {
                throw new IllegalStateException("Insufficient stored amount to retrieve " +
                        ar.getName() + ". Storage Amount : " + getARStored(resource, false) +
                        " kg. Attempted Amount : " + amount + " kg");
            }
        }
    }

    /**
     * Adds a capacity to general capacity.
     * @param capacity amount capacity (kg).
     */
    public void addGeneralCapacity(double capacity) {
        generalCapacity += capacity;
        // Mark amount resource capacity cache as dirty.
        setAmountResourceCapacityCacheAllDirty(false);
    }

    /**
     * Gets the general capacity.
     * @return amount capacity (kg).
     */
    public double getGeneralCapacity() {
        return generalCapacity;
    }

    /**
     * Gets the mass stored in general capacity.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored mass (kg).
     */
    public double getGeneralStoredMass(boolean allowDirty) {
        return getItemResourceTotalMass(allowDirty) + getUnitTotalMass(allowDirty);
    }

    /**
     * Gets the remaining general capacity available.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return amount capacity (kg).
     */
    public double getRemainingGeneralCapacity(boolean allowDirty) {
        double result = generalCapacity - getGeneralStoredMass(allowDirty);
        double containerUnitGeneralCapacityLimit = getContainerUnitGeneralCapacityLimit(allowDirty);
        if (result > containerUnitGeneralCapacityLimit) {
            result = containerUnitGeneralCapacityLimit;
        }
        return result;
    }

    /**
     * Checks if storage has an item resource.
     * @param resource the resource.
     * @return true if has resource.
     */
    public boolean hasItemResource(ItemResource resource) {
        boolean result = false;
        if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
            if (containedItemResources.get(resource) > 0) {
                result = true;
            }
        } else if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (!result && i.hasNext()) {
                if (i.next().getInventory().hasItemResource(resource)) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Gets the number of an item resource in storage.
     * @param resource the resource.
     * @return number of resources.
     */
    public int getItemResourceNum(ItemResource resource) {
        int result = 0;
        if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
            result += containedItemResources.get(resource);
        }
        return result;
    }

    /**
     * Gets a set of all the item resources in storage.
     * @return set of item resources.
     */
    public Set<ItemResource> getAllItemResourcesStored() {
        Set<ItemResource> result = null;
        if (containedItemResources != null) {
            result = containedItemResources.keySet();
        } else {
            result = new HashSet<ItemResource>();
        }
        return result;
    }

    /**
     * Gets the total mass of item resources in storage.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return the total mass (kg).
     */
    private double getItemResourceTotalMass(boolean allowDirty) {
        return getItemResourceTotalMassCache(allowDirty);
    }

    /**
     * Stores item resources.
     * @param resource the resource to store.
     * @param number the number of resources to store.
     */
    public void storeItemResources(ItemResource resource, int number) {

        if (number < 0) {
            throw new IllegalStateException("Cannot store negative number of resources.");
        }

        double totalMass = resource.getMassPerItem() * number;

        if (number > 0) {
            if (totalMass <= getRemainingGeneralCapacity(false)) {

                // Mark caches as dirty.
                setAmountResourceCapacityCacheAllDirty(false);
                setItemResourceTotalMassCacheDirty();

                // Initialize contained item resources if necessary.
                if (containedItemResources == null) {
                    containedItemResources = new ConcurrentHashMap<ItemResource, Integer>();
                }

                int totalNum = number + getItemResourceNum(resource);
                if (totalNum > 0) {
                    containedItemResources.put(resource, totalNum);
                }

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
                }
            }
            else {
                throw new IllegalStateException("Could not store item resources.");
            }
        }
    }

    /**
     * Retrieves item resources.
     * @param resource the resource to retrieve.
     * @param number the number of resources to retrieve.
     */
    public void retrieveItemResources(ItemResource resource, int number) {

        if (number < 0) {
            throw new IllegalStateException("Cannot retrieve negative number of resources.");
        }

        if (number > 0) {
            if (number <= getItemResourceNum(resource)) {
                int remainingNum = number;

                // Mark caches as dirty.
                setAmountResourceCapacityCacheAllDirty(false);
                setItemResourceTotalMassCacheDirty();

                // Retrieve resources from local storage.
                if ((containedItemResources != null) && containedItemResources.containsKey(resource)) {
                    int storedLocal = containedItemResources.get(resource);
                    int retrieveNum = remainingNum;
                    if (retrieveNum > storedLocal) {
                        retrieveNum = storedLocal;
                    }
                    int remainingLocal = storedLocal - retrieveNum;
                    if (remainingLocal > 0) {
                        containedItemResources.put(resource, remainingLocal);
                    } else {
                        containedItemResources.remove(resource);
                    }
                    remainingNum -= retrieveNum;
                }

                // Fire inventory event.
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
                }

                if (remainingNum > 0) {
                    throw new IllegalStateException(resource.getName()
                            + " could not be totally retrieved. Remaining: " + remainingNum);
                }
            }
            else {
                throw new IllegalStateException("Insufficient stored number to retrieve " +
                        resource.getName() + ", stored: " + getItemResourceNum(resource) +
                        ", attempted: " + number);
            }
        }
    }

    /**
     * Gets the total unit mass in storage.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return total mass (kg).
     */
    public double getUnitTotalMass(boolean allowDirty) {
        return getUnitTotalMassCache(allowDirty);
    }

    /**
     * Gets a collection of all the stored units.
     * @return Collection of all units
     */
    public Collection<Unit> getContainedUnits() {
        Collection<Unit> result = null;
        if (containedUnits != null) {
            result = new ArrayList<Unit>(containedUnits);
        } else {
            result = new ArrayList<Unit>(0);
        }
        return result;
    }

    public Collection<Unit> getAllContainedUnits() {
    	return containedUnits;
    }

    /**
     * Checks if a unit is in storage.
     * @param unit the unit.
     * @return true if unit is in storage.
     */
    public boolean containsUnit(Unit unit) {
        boolean result = false;
        if (containedUnits != null) {
            result = containedUnits.contains(unit);
        }
        return result;
    }

    /**
     * Checks if any of a given class of unit is in storage.
     * @param unitClass the unit class.
     * @return true if class of unit is in storage.
     */
    private boolean containsUnitClassLocal(Class<? extends Unit> unitClass) {
        boolean result = false;
        if (containedUnits != null) {
            Iterator<Unit> i = containedUnits.iterator();
            while (!result && i.hasNext()) {
                if (unitClass.isInstance(i.next())) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Checks if any of a given class of unit is in storage.
     * @param unitClass the unit class.
     * @return if class of unit is in storage.
     */
    public boolean containsUnitClass(Class<? extends Unit> unitClass) {
        boolean result = false;
        // Check if unit of class is in inventory.
        if (containsUnitClassLocal(unitClass)) {
            result = true;
        }
        return result;
    }

    /**
     * Finds a unit of a given class in storage.
     * @param unitClass the unit class.
     * @return the instance of the unit class or null if none.
     */
    public Unit findUnitOfClass(Class<? extends Unit> unitClass) {
        Unit result = null;
        if (containsUnitClass(unitClass)) {
            Iterator<Unit> i = containedUnits.iterator();
            while ((result == null) && i.hasNext()) {
                Unit unit = i.next();
                if (unitClass.isInstance(unit)) {
                    result = unit;
                }
            }
        }
        return result;
    }

    /**
     * Finds all of the units of a class in storage.
     * @param unitClass the unit class.
     * @return collection of units or empty collection if none.
     */
    public Collection<Unit> findAllUnitsOfClass(Class<? extends Unit> unitClass) {
        Collection<Unit> result = new ConcurrentLinkedQueue<Unit>();
        if (containsUnitClass(unitClass)) {
            for (Unit unit : containedUnits) {
                if (unitClass.isInstance(unit)) {
                    result.add(unit);
                }
            }
        }
        return result;
    }

    /**
     * Finds the number of units of a class that are contained in storage.
     * @param unitClass the unit class.
     * @return number of units
     */
    public int findNumUnitsOfClass(Class<? extends Unit> unitClass) {
        int result = 0;
        if (containsUnitClass(unitClass)) {
            for (Unit unit : containedUnits) {
                if (unitClass.isInstance(unit)) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Finds the number of units of a class that are contained in
     * storage and have an empty inventory.
     * @param unitClass the unit class.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return number of empty units.
     */
    public int findNumEmptyUnitsOfClass(Class<? extends Unit> unitClass, boolean allowDirty) {
        int result = 0;
        if (containsUnitClass(unitClass)) {
            for (Unit unit : containedUnits) {
                if (unitClass.isInstance(unit)) {
                    Inventory inv = unit.getInventory();
                    if ((inv != null) && inv.isEmpty(allowDirty)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Checks if a unit can be stored.
     * @param unit the unit.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if unit can be added to inventory
     */
    public boolean canStoreUnit(Unit unit, boolean allowDirty) {
        boolean result = false;
        if (unit != null) {
            if (unit.getMass() <= getRemainingGeneralCapacity(allowDirty)) {
                result = true;
            }
            else {
                result = false;
            }

            if (unit == owner) {
                result = false;
            }
            if (containsUnit(unit)) {
                result = false;
            }
            if (unit.getInventory().containsUnit(owner)) {
                result = false;
            }
        }
        return result;
    }

    /**
     * Stores a unit.
     * @param unit the unit
     */
    public void storeUnit(Unit unit) {

        if (canStoreUnit(unit, false)) {

            // Set modified cache values as dirty.
            setAmountResourceCapacityCacheAllDirty(true);
            setAmountResourceStoredCacheAllDirty(true);
            setAllStoredAmountResourcesCacheDirty();
            setTotalAmountResourcesStoredCacheDirty();
            setUnitTotalMassCacheDirty();

            // Initialize containedUnits if necessary.
            if (containedUnits == null) {
                containedUnits = new ConcurrentLinkedQueue<Unit>();
            }

            containedUnits.add(unit);
            unit.setContainerUnit(owner);

            // Try to empty amount resources into parent if container.
            if (unit instanceof Container) {
                Inventory containerInv = unit.getInventory();
                for (AmountResource resource : containerInv.getAllAmountResourcesStored(false)) {
                    double containerAmount = containerInv.getAmountResourceStored(resource, false);
                    if (getAmountResourceRemainingCapacity(resource, false, false) >= containerAmount) {
                        containerInv.retrieveAmountResource(resource, containerAmount);
                        storeAmountResource(resource, containerAmount, false);
                    }
                }
            }

            // Update owner
            if (owner != null) {
                unit.setCoordinates(owner.getCoordinates());
                owner.fireUnitUpdate(UnitEventType.INVENTORY_STORING_UNIT_EVENT, unit);
                for (AmountResource resource : unit.getInventory().getAllAmountResourcesStored(false)) {
                    updateAmountResourceCapacityCache(resource);
                    updateAmountResourceStoredCache(resource);
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
                }
                for (ItemResource itemResource : unit.getInventory().getAllItemResourcesStored()) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, itemResource);
                }
            }
        }
        else {
        	throw new IllegalStateException("Unit: " + unit + " could not be stored.");
    	    //LogConsolidated.log(logger, Level.WARNING, 5000, sourceName + "::storeUnit",
    	    // 		"Unit: " + unit + " could not be stored.", null);
        }
    }

    /**
     * Retrieves a unit from storage.
     * @param unit the unit.
     */
    public void retrieveUnit(Unit unit) {

        boolean retrieved = false;

        if (containsUnit(unit)) {

            // Set modified cache values as dirty.
            setAmountResourceCapacityCacheAllDirty(true);
            setAmountResourceStoredCacheAllDirty(true);
            setAllStoredAmountResourcesCacheDirty();
            setTotalAmountResourcesStoredCacheDirty();
            setUnitTotalMassCacheDirty();

            if (containedUnits.contains(unit)) {

                containedUnits.remove(unit);

                // Update owner
                if (owner != null) {
                    owner.fireUnitUpdate(UnitEventType.INVENTORY_RETRIEVING_UNIT_EVENT, unit);

                    for (AmountResource resource : unit.getInventory().getAllAmountResourcesStored(false)) {
                        updateAmountResourceCapacityCache(resource);
                        updateAmountResourceStoredCache(resource);
                        owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, resource);
                    }
                    for (ItemResource itemResource : unit.getInventory().getAllItemResourcesStored()) {
                        owner.fireUnitUpdate(UnitEventType.INVENTORY_RESOURCE_EVENT, itemResource);
                    }
                }

                retrieved = true;
            }
        }

        if (retrieved) {
            unit.setContainerUnit(null); // this can cause person.getSettlement() = null
        } else {
            throw new IllegalStateException("Unit: " + unit + " could not be retrieved.");
    	    //LogConsolidated.log(logger, Level.WARNING, 5000, sourceName + "::retrieveUnit",
    	    //		"Unit: " + unit + " could not be retrieved.", null);
        }
    }

    /**
     * Sets the coordinates of all units in the inventory.
     * @param newLocation the new coordinate location
     */
    public void setCoordinates(Coordinates newLocation) {

        if (containedUnits != null) {
            for (Unit unit : containedUnits) {
                unit.setCoordinates(newLocation);
            }
        }
    }

    /**
     * Gets the total mass stored in inventory.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return stored mass (kg).
     */
    public double getTotalInventoryMass(boolean allowDirty) {

        return getTotalInventoryMassCache(allowDirty);
    }

    /**
     * Checks if inventory is empty.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return true if empty.
     */
    public boolean isEmpty(boolean allowDirty) {

        return (getTotalInventoryMass(allowDirty) == 0D);
    }

    /**
     * Gets any limits in the owner's general capacity.
     * @param allowDirty will allow dirty (possibly out of date) results.
     * @return owner general capacity limit (kg).
     */
    private double getContainerUnitGeneralCapacityLimit(boolean allowDirty) {

        double result = Double.MAX_VALUE;

        if ((owner != null) && (owner.getContainerUnit() != null)) {
            Inventory containerInv = owner.getContainerUnit().getInventory();
            if (containerInv.getRemainingGeneralCapacity(allowDirty) < result) {
                result = containerInv.getRemainingGeneralCapacity(allowDirty);
            }

            if (containerInv.getContainerUnitGeneralCapacityLimit(allowDirty) < result) {
                result = containerInv.getContainerUnitGeneralCapacityLimit(allowDirty);
            }
        }

        return result;
    }

    /**
     * Initializes the amount resource capacity cache.
     */
    public synchronized void initializeAmountResourceCapacityCache() {
    	initializeARCapacityCache();
/*    	
        Collection<AmountResource> resources = ResourceUtil.getInstance().getAmountResources();
        amountResourceCapacityCache = new HashMap<AmountResource, Double>();
        amountResourceCapacityCacheDirty = new HashMap<AmountResource, Boolean>();
        amountResourceContainersCapacityCache = new HashMap<AmountResource, Double>();
        amountResourceContainersCapacityCacheDirty = new HashMap<AmountResource, Boolean>();

        for (AmountResource resource : resources) {
            amountResourceCapacityCache.put(resource, 0D);
            amountResourceCapacityCacheDirty.put(resource, true);
            amountResourceContainersCapacityCache.put(resource, 0D);
            amountResourceContainersCapacityCacheDirty.put(resource, true);
        }
*/        
    }

    /**
     * Initializes the amount resource capacity cache.
     */
    public synchronized void initializeARCapacityCache() {

        Collection<Integer> resources = ResourceUtil.getInstance().getARIDs();
        capacityCache = new HashMap<Integer, Double>();
        capacityCacheDirty = new HashMap<Integer, Boolean>();
        containersCapacityCache = new HashMap<Integer, Double>();
        containersCapacityCacheDirty = new HashMap<Integer, Boolean>();

        for (int resource : resources) {
            capacityCache.put(resource, 0D);
            capacityCacheDirty.put(resource, true);
            containersCapacityCache.put(resource, 0D);
            containersCapacityCacheDirty.put(resource, true);
        }
    }
    
    /**
     * Checks if the amount resource capacity cache is dirty for a resource.
     * @param resource the resource to check.
     * @return true if resource is dirty in cache.
     */
    private boolean isAmountResourceCapacityCacheDirty(AmountResource resource) {
    	return isARCapacityCacheDirty(resource.getID());
/*    	
        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }
        // 2016-12-21 Check if amountResourceCapacityCacheDirty contains the resource
        if (amountResourceCapacityCacheDirty.containsKey(resource))
        	return amountResourceCapacityCacheDirty.get(resource);
        else
        	return true;
*/
    }

    /**
     * Checks if the amount resource capacity cache is dirty for a resource.
     * @param resource the resource to check.
     * @return true if resource is dirty in cache.
     */
    private boolean isARCapacityCacheDirty(int resource) {

        // Initialize amount resource capacity cache if necessary.
        if (capacityCache == null) {
            initializeAmountResourceCapacityCache();
        }
        // 2016-12-21 Check if amountResourceCapacityCacheDirty contains the resource
        if (capacityCacheDirty.containsKey(resource))
        	return capacityCacheDirty.get(resource);
        else
        	return true;

    }
    
    /**
     * Sets a resource in the amount resource capacity cache to dirty.
     * @param resource the dirty resource.
     */
    private void setAmountResourceCapacityCacheDirty(AmountResource resource) {
    	setARCapacityCacheDirty(resource.getID());
/*   	
        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        amountResourceCapacityCacheDirty.put(resource, true);
*/        
    }

    /**
     * Sets a resource in the amount resource capacity cache to dirty.
     * @param resource the dirty resource.
     */
    private void setARCapacityCacheDirty(int resource) {

        // Initialize amount resource capacity cache if necessary.
        if (capacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        capacityCacheDirty.put(resource, true);
    }
    
    /**
     * Sets all of the resources in the amount resource capacity cache to dirty.
     * @param containersDirty true if containers cache should be marked as all dirty.
     */
    private void setAmountResourceCapacityCacheAllDirty(boolean containersDirty) {
    	setARCapacityCacheAllDirty(containersDirty);
/*
        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        for (AmountResource amountResource : ResourceUtil.getInstance().getAmountResources()) {
            setAmountResourceCapacityCacheDirty(amountResource);

            if (containersDirty) {
                amountResourceContainersCapacityCacheDirty.put(amountResource, true);
            }
        }

        // Set owner unit's amount resource capacity cache as dirty (if any).
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAmountResourceCapacityCacheAllDirty(true);
            }
        }
*/        
    }

    /**
     * Sets all of the resources in the amount resource capacity cache to dirty.
     * @param containersDirty true if containers cache should be marked as all dirty.
     */
    private void setARCapacityCacheAllDirty(boolean containersDirty) {

        // Initialize amount resource capacity cache if necessary.
        if (capacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        for (int amountResource : ResourceUtil.getInstance().getARIDs()) {
            setARCapacityCacheDirty(amountResource);

            if (containersDirty) {
                containersCapacityCacheDirty.put(amountResource, true);
            }
        }

        // Set owner unit's amount resource capacity cache as dirty (if any).
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAmountResourceCapacityCacheAllDirty(true);
            }
        }
    }
    
    /**
     * Gets the cached capacity value for an amount resource.
     * @param resource the amount resource.
     * @param allowDirty true if cache value can be dirty.
     * @return capacity (kg) for the amount resource.
     */
    private double getAmountResourceCapacityCacheValue(AmountResource resource, boolean allowDirty) {
    	return getARCapacityCacheValue(resource.getID(), allowDirty);
/*    	
        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        // Update amount resource capacity cache if it is dirty.
        if (isAmountResourceCapacityCacheDirty(resource) && !allowDirty) {
            updateAmountResourceCapacityCache(resource);
        }

        // 2017-03-21 Check if amountResourceCapacityCache contains the resource
        if (amountResourceCapacityCache.containsKey(resource))
        	return amountResourceCapacityCache.get(resource);
        else {
        	amountResourceCapacityCache.put(resource, 0D);
        	return 0;
        }
        //return amountResourceCapacityCache.get(resource);
*/        
    }

    /**
     * Gets the cached capacity value for an amount resource.
     * @param resource the amount resource.
     * @param allowDirty true if cache value can be dirty.
     * @return capacity (kg) for the amount resource.
     */
    private double getARCapacityCacheValue(int resource, boolean allowDirty) {

        // Initialize amount resource capacity cache if necessary.
        if (capacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        // Update amount resource capacity cache if it is dirty.
        if (isARCapacityCacheDirty(resource) && !allowDirty) {
            updateARCapacityCache(resource);
        }

        // 2017-03-21 Check if amountResourceCapacityCache contains the resource
        if (capacityCache.containsKey(resource))
        	return capacityCache.get(resource);
        else {
        	capacityCache.put(resource, 0D);
        	return 0;
        }
        //return amountResourceCapacityCache.get(resource);
    }
    
    /**
     * Update the amount resource capacity cache for an amount resource.
     * @param resource the resource to update.
     */
    private void updateAmountResourceCapacityCache(AmountResource resource) {
    	updateARCapacityCache(resource.getID());
/*    	
        // Initialize amount resource capacity cache if necessary.
        if (amountResourceCapacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        // Determine local resource capacity.
        double capacity = 0D;
        if (resourceStorage != null) {
            capacity += resourceStorage.getAmountResourceCapacity(resource);
        }

        // Determine capacity for all contained units.
        double containedCapacity = 0D;
        // 2016-12-21 Check for null
        if (amountResourceContainersCapacityCacheDirty.containsKey(resource)) {
	        if (amountResourceContainersCapacityCacheDirty.get(resource)) {
	            if (containedUnits != null) {
	                for (Unit unit : containedUnits) {
	                    if (unit instanceof Container) {
	                        containedCapacity += unit.getInventory().getAmountResourceCapacity(resource, false);
	                    }
	                }
	            }
	            amountResourceContainersCapacityCache.put(resource, containedCapacity);
	            amountResourceContainersCapacityCacheDirty.put(resource, false);
	        }
	        // 2016-12-21 Check for null
	        else if (amountResourceContainersCapacityCache.containsKey(resource)) {
	            containedCapacity = amountResourceContainersCapacityCache.get(resource);
	        }
        }
        // 2016-12-21 Check for null
        else if (amountResourceContainersCapacityCache.containsKey(resource)) {
            containedCapacity = amountResourceContainersCapacityCache.get(resource);
        }

        // Determine stored resources for all contained units.
        double containedStored = 0D;
        if (amountResourceContainersStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        // 2017-03-21 Add checking for amountResourceContainersStoredCacheDirty and add if else clause
        if (amountResourceContainersStoredCacheDirty.containsKey(resource)) {
         	if (amountResourceContainersStoredCacheDirty.get(resource)) {
	            if (containedUnits != null) {
	                for (Unit unit : containedUnits) {
	                    if (unit instanceof Container) {
	                        containedStored += unit.getInventory().getAmountResourceStored(resource, false);
	                    }
	                }
	            }
	            amountResourceContainersStoredCache.put(resource,  containedStored);
	            amountResourceContainersStoredCacheDirty.put(resource, false);
	        }
	        else {
	            containedStored = amountResourceContainersStoredCache.get(resource);
	        }
        }
        else {
            //amountResourceContainersStoredCacheDirty.put(resource, false);
            //containedStored = amountResourceContainersStoredCache.get(resource);
        }

        // Limit container capacity to this inventory's remaining general capacity.
        // Add container's resource stored as this is already factored into inventory's
        // remaining general capacity.
        double generalResourceCapacity = getRemainingGeneralCapacity(false) + containedStored;
        if (containedCapacity > generalResourceCapacity) {
            containedCapacity = generalResourceCapacity;
        }

        capacity += containedCapacity;

        amountResourceCapacityCache.put(resource, capacity);
        amountResourceCapacityCacheDirty.put(resource, false);
*/        
    }

    
    /**
     * Update the amount resource capacity cache for an amount resource.
     * @param resource the resource to update.
     */
    private void updateARCapacityCache(int resource) {

        // Initialize amount resource capacity cache if necessary.
        if (capacityCache == null) {
            initializeAmountResourceCapacityCache();
        }

        // Determine local resource capacity.
        double capacity = 0D;
        if (resourceStorage != null) {
            capacity += resourceStorage.getARCapacity(resource);
        }

        // Determine capacity for all contained units.
        double containedCapacity = 0D;
        // 2016-12-21 Check for null
        if (containersCapacityCacheDirty.containsKey(resource)) {
	        if (containersCapacityCacheDirty.get(resource)) {
	            if (containedUnits != null) {
	                for (Unit unit : containedUnits) {
	                    if (unit instanceof Container) {
	                        containedCapacity += unit.getInventory().getARCapacity(resource, false);
	                    }
	                }
	            }
	            containersCapacityCache.put(resource, containedCapacity);
	            containersCapacityCacheDirty.put(resource, false);
	        }
	        // 2016-12-21 Check for null
	        else if (containersCapacityCache.containsKey(resource)) {
	            containedCapacity = containersCapacityCache.get(resource);
	        }
        }
        // 2016-12-21 Check for null
        else if (containersCapacityCache.containsKey(resource)) {
            containedCapacity = containersCapacityCache.get(resource);
        }

        // Determine stored resources for all contained units.
        double containedStored = 0D;
        if (containersStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        // 2017-03-21 Add checking for amountResourceContainersStoredCacheDirty and add if else clause
        if (containersStoredCacheDirty.containsKey(resource)) {
         	if (containersStoredCacheDirty.get(resource)) {
	            if (containedUnits != null) {
	                for (Unit unit : containedUnits) {
	                    if (unit instanceof Container) {
	                        containedStored += unit.getInventory().getARStored(resource, false);
	                    }
	                }
	            }
	            containersStoredCache.put(resource,  containedStored);
	            containersStoredCacheDirty.put(resource, false);
	        }
	        else {
	            containedStored = containersStoredCache.get(resource);
	        }
        }
        else {
            //amountResourceContainersStoredCacheDirty.put(resource, false);
            //containedStored = amountResourceContainersStoredCache.get(resource);
        }

        // Limit container capacity to this inventory's remaining general capacity.
        // Add container's resource stored as this is already factored into inventory's
        // remaining general capacity.
        double generalResourceCapacity = getRemainingGeneralCapacity(false) + containedStored;
        if (containedCapacity > generalResourceCapacity) {
            containedCapacity = generalResourceCapacity;
        }

        capacity += containedCapacity;

        capacityCache.put(resource, capacity);
        capacityCacheDirty.put(resource, false);
    }

    
    /**
     * Initializes the amount resource stored cache.
     */
    private synchronized void initializeAmountResourceStoredCache() {
    	initializeARStoredCache();
/*    	
        Collection<AmountResource> resources = ResourceUtil.getInstance().getAmountResources();
        amountResourceStoredCache = new HashMap<AmountResource, Double>();
        amountResourceStoredCacheDirty = new HashMap<AmountResource, Boolean>();
        amountResourceContainersStoredCache = new HashMap<AmountResource, Double>();
        amountResourceContainersStoredCacheDirty = new HashMap<AmountResource, Boolean>();

        for (AmountResource resource : resources) {
            amountResourceStoredCache.put(resource, 0D);
            amountResourceStoredCacheDirty.put(resource, true);
            amountResourceContainersStoredCache.put(resource, 0D);
            amountResourceContainersStoredCacheDirty.put(resource, true);
        }
*/        
    }

    /**
     * Initializes the amount resource stored cache.
     */
    private synchronized void initializeARStoredCache() {
        Collection<Integer> resources = ResourceUtil.getInstance().getARIDs();
        storedCache = new HashMap<Integer, Double>();
        storedCacheDirty = new HashMap<Integer, Boolean>();
        containersStoredCache = new HashMap<Integer, Double>();
        containersStoredCacheDirty = new HashMap<Integer, Boolean>();

        for (int resource : resources) {
            storedCache.put(resource, 0D);
            storedCacheDirty.put(resource, true);
            containersStoredCache.put(resource, 0D);
            containersStoredCacheDirty.put(resource, true);
        }
    }
    
    /**
     * Checks if the amount resource stored cache is dirty for a resource.
     * @param resource the resource to check.
     * @return true if resource is dirty in cache.
     */
    private boolean isAmountResourceStoredCacheDirty(AmountResource resource) {
    	return isARStoredCacheDirty(resource.getID());
    	/* 
    	// Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCacheDirty == null) {
            initializeAmountResourceStoredCache();
        }

        // 2016-12-21 Check if amountResourceStoredCacheDirty contains the resource
        if (amountResourceStoredCacheDirty.containsKey(resource))
        	return amountResourceStoredCacheDirty.get(resource);
        else
        	return true;
*/        
    }

    /**
     * Checks if the amount resource stored cache is dirty for a resource.
     * @param resource the resource to check.
     * @return true if resource is dirty in cache.
     */
    private boolean isARStoredCacheDirty(int resource) {
        // Initialize amount resource stored cache if necessary.
        if (storedCacheDirty == null) {
            initializeAmountResourceStoredCache();
        }

        // 2016-12-21 Check if amountResourceStoredCacheDirty contains the resource
        if (storedCacheDirty.containsKey(resource))
        	return storedCacheDirty.get(resource);
        else
        	return true;
    }
    
    /**
     * Sets a resource in the amount resource stored cache to dirty.
     * @param resource the dirty resource.
     */
    private void setAmountResourceStoredCacheDirty(AmountResource resource) {
    	setARStoredCacheDirty(resource.getID());
/*    	
        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        amountResourceStoredCacheDirty.put(resource, true);
*/        
    }

    /**
     * Sets a resource in the amount resource stored cache to dirty.
     * @param resource the dirty resource.
     */
    private void setARStoredCacheDirty(int resource) {

        // Initialize amount resource stored cache if necessary.
        if (storedCache == null) {
            initializeAmountResourceStoredCache();
        }

        storedCacheDirty.put(resource, true);
    }
    
    /**
     * Sets all of the resources in the amount resource stored cache to dirty.
     * @param containersDirty true if containers cache should be marked as all dirty.
     */
    private void setAmountResourceStoredCacheAllDirty(boolean containersDirty) {
    	setARStoredCacheAllDirty(containersDirty);
/*    	
        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        for (AmountResource amountResource : ResourceUtil.getInstance().getAmountResources()) {
            setAmountResourceStoredCacheDirty(amountResource);

            if (containersDirty) {
                amountResourceContainersStoredCacheDirty.put(amountResource, true);
            }
        }

        // Set owner unit's amount resource stored cache as dirty (if any).
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAmountResourceStoredCacheAllDirty(true);
            }
        }
*/        
    }

    /**
     * Sets all of the resources in the amount resource stored cache to dirty.
     * @param containersDirty true if containers cache should be marked as all dirty.
     */
    private void setARStoredCacheAllDirty(boolean containersDirty) {

        // Initialize amount resource stored cache if necessary.
        if (storedCache == null) {
            initializeAmountResourceStoredCache();
        }

        for (int amountResource : ResourceUtil.getInstance().getARIDs()) {
            setARStoredCacheDirty(amountResource);

            if (containersDirty) {
                containersStoredCacheDirty.put(amountResource, true);
            }
        }

        // Set owner unit's amount resource stored cache as dirty (if any).
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setARStoredCacheAllDirty(true);
            }
        }
    }
    
    /**
     * Gets the cached stored value for an amount resource.
     * @param resource the amount resource.
     * @param allowDirty true if cache value can be dirty.
     * @return stored amount (kg) for the amount resource.
     */
    private double getAmountResourceStoredCacheValue(final AmountResource resource, final boolean allowDirty) {
    	return getARStoredCacheValue(resource.getID(), allowDirty);
/*   	
        // Initialize amount resource stored cache if necessary.
        if (amountResourceStoredCache == null) {
            initializeAmountResourceStoredCache();
        }

        // Update amount resource stored cache if it is dirty.
        if (!allowDirty && isAmountResourceStoredCacheDirty(resource)) {
            updateAmountResourceStoredCache(resource);
        }

        // 2017-03-21 Check if amountResourceStoredCache contains the resource
        if (amountResourceStoredCache.containsKey(resource))
        	return amountResourceStoredCache.get(resource);
        else {
        	amountResourceCapacityCache.put(resource, 0D);
        	return 0;
        }

        //return amountResourceStoredCache.get(resource);
*/
    }

    /**
     * Gets the cached stored value for an amount resource.
     * @param resource the amount resource.
     * @param allowDirty true if cache value can be dirty.
     * @return stored amount (kg) for the amount resource.
     */
    private double getARStoredCacheValue(final int resource, final boolean allowDirty) {

        // Initialize amount resource stored cache if necessary.
        if (storedCache == null) {
            initializeARStoredCache();
        }

        // Update amount resource stored cache if it is dirty.
        if (!allowDirty && isARStoredCacheDirty(resource)) {
            updateARStoredCache(resource);
        }

        // 2017-03-21 Check if amountResourceStoredCache contains the resource
        if (storedCache.containsKey(resource))
        	return storedCache.get(resource);
        else {
        	capacityCache.put(resource, 0D);
        	return 0;
        }

        //return amountResourceStoredCache.get(resource);

    }
    
    /**
     * Update the amount resource stored cache for an amount resource.
     * @param resource the resource to update.
     */
    private void updateAmountResourceStoredCache(AmountResource resource) {
    	updateARStoredCache(resource.getID());
/*    	
        double stored = 0D;

        if (resourceStorage != null) {
            stored += resourceStorage.getAmountResourceStored(resource);
        }

        double containerStored = 0D;
        // 2016-12-21 Add checking for amountResourceContainersStoredCacheDirty
        if (amountResourceContainersStoredCacheDirty.containsKey(resource)) {
	        if (amountResourceContainersStoredCacheDirty.get(resource)) {
	            if (containedUnits != null) {
	                for (Unit unit : containedUnits) {
	                    if (unit instanceof Container) {
	                        containerStored += unit.getInventory().getAmountResourceStored(resource, false);
	                    }
	                }
	            }
	            amountResourceContainersStoredCache.put(resource, containerStored);
	            amountResourceContainersStoredCacheDirty.put(resource, false);
	        }
	        else {
	            containerStored = amountResourceContainersStoredCache.get(resource);
	        }
        }
        // 2016-12-21 Add checking amountResourceContainersStoredCache
        else if (amountResourceContainersStoredCache.containsKey(resource)) {
        	containerStored = amountResourceContainersStoredCache.get(resource);
        }
        else {
        	//containerStored = amountResourceContainersStoredCache.get(resource);
        }

        stored += containerStored;

        amountResourceStoredCache.put(resource, stored);
        amountResourceStoredCacheDirty.put(resource, false);
*/        
    }

    /**
     * Update the amount resource stored cache for an amount resource.
     * @param resource the resource to update.
     */
    private void updateARStoredCache(int resource) {

        double stored = 0D;

        if (resourceStorage != null) {
            stored += resourceStorage.getARStored(resource);
        }

        double containerStored = 0D;
        // 2016-12-21 Add checking for amountResourceContainersStoredCacheDirty
        if (containersStoredCacheDirty.containsKey(resource)) {
	        if (containersStoredCacheDirty.get(resource)) {
	            if (containedUnits != null) {
	                for (Unit unit : containedUnits) {
	                    if (unit instanceof Container) {
	                        containerStored += unit.getInventory().getARStored(resource, false);
	                    }
	                }
	            }
	            containersStoredCache.put(resource, containerStored);
	            containersStoredCacheDirty.put(resource, false);
	        }
	        else {
	            containerStored = containersStoredCache.get(resource);
	        }
        }
        // 2016-12-21 Add checking amountResourceContainersStoredCache
        else if (containersStoredCache.containsKey(resource)) {
        	containerStored = containersStoredCache.get(resource);
        }
        else {
        	//containerStored = amountResourceContainersStoredCache.get(resource);
        }

        stored += containerStored;

        storedCache.put(resource, stored);
        storedCacheDirty.put(resource, false);
    }
    
    /**
     * Initializes the all stored amount resources cache.
     */
    private synchronized void initializeAllStoredAmountResourcesCache() {
    	initializeAllStoredARCache();
        //allStoredAmountResourcesCache = new HashSet<AmountResource>();
        //allStoredAmountResourcesCacheDirty = true;
    }

    /**
     * Initializes the all stored amount resources cache.
     */
    private synchronized void initializeAllStoredARCache() {

        allStoredARCache = new HashSet<Integer>();
        allStoredAmountResourcesCacheDirty = true;
    }
    
    /**
     * Sets the all stored amount resources cache as dirty.
     */
    private void setAllStoredAmountResourcesCacheDirty() {
    	setAllStoredARCacheDirty();
    	/*
        // Update all stored amount resources cache if it hasn't been initialized.
        if (allStoredAmountResourcesCache == null) {
            initializeAllStoredAmountResourcesCache();
        }

        allStoredAmountResourcesCacheDirty = true;

        // Mark owner unit's all stored amount resources stored as dirty, if any.
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAllStoredAmountResourcesCacheDirty();
            }
        }
*/        
    }

    /**
     * Sets the all stored amount resources cache as dirty.
     */
    private void setAllStoredARCacheDirty() {

        // Update all stored amount resources cache if it hasn't been initialized.
        if (allStoredARCache == null) {
            initializeAllStoredAmountResourcesCache();
        }

        allStoredAmountResourcesCacheDirty = true;

        // Mark owner unit's all stored amount resources stored as dirty, if any.
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setAllStoredARCacheDirty();
            }
        }
    }    
    /**
     * Gets the all stored amount resources cache.
     * @param allowDirty true if cache value can be dirty.
     * @return all stored amount resources cache value.
     */
    private Set<AmountResource> getAllStoredAmountResourcesCache(boolean allowDirty) {
    	Set<AmountResource> set = new HashSet<>();
    	for (int ar : getAllStoredARCache(allowDirty)) {
    		set.add(ResourceUtil.findAmountResource(ar));
    	}
    	return set;
    	
/*    	
        // Update all stored amount resources cache if it hasn't been initialized.
        if (allStoredAmountResourcesCache == null) {
            initializeAllStoredAmountResourcesCache();
        }

        if (allStoredAmountResourcesCacheDirty && !allowDirty) {
            updateAllStoredAmountResourcesCache();
        }

        return allStoredAmountResourcesCache;
*/        
    }

    /**
     * Gets the all stored amount resources cache.
     * @param allowDirty true if cache value can be dirty.
     * @return all stored amount resources cache value.
     */
    private Set<Integer> getAllStoredARCache(boolean allowDirty) {

        // Update all stored amount resources cache if it hasn't been initialized.
        if (allStoredARCache == null) {
            initializeAllStoredARCache();
        }

        if (allStoredAmountResourcesCacheDirty && !allowDirty) {
            updateAllStoredARCache();
        }

        return allStoredARCache;
    }
    
    /**
     * Update the all stored amount resources cache as well as the container's cache if any.
     */
    private void updateAllStoredAmountResourcesCache() {
    	updateAllStoredARCache();
/*    	
        Set<AmountResource> tempAllStored = new HashSet<AmountResource>();

        if (resourceStorage != null) {
            tempAllStored.addAll(resourceStorage.getAllAmountResourcesStored(false));
        }

        if (containedUnits != null) {
            for (Unit unit : containedUnits) {
                if (unit instanceof Container) {
                    tempAllStored.addAll(unit.getInventory().getAllAmountResourcesStored(false));
                }
            }
        }

        allStoredAmountResourcesCache = tempAllStored;
        allStoredAmountResourcesCacheDirty = false;
*/        
    }
    
    /**
     * Update the all stored amount resources cache as well as the container's cache if any.
     */
    private void updateAllStoredARCache() {

        Set<Integer> tempAllStored = new HashSet<Integer>();

        if (resourceStorage != null) {
            tempAllStored.addAll(resourceStorage.getAllARStored(false));
        }

        if (containedUnits != null) {
            for (Unit unit : containedUnits) {
                if (unit instanceof Container) {
                    tempAllStored.addAll(unit.getInventory().getAllARStored(false));
                }
            }
        }

        allStoredARCache = tempAllStored;
        allStoredAmountResourcesCacheDirty = false;
    }

    /**
     * Sets the total amount resources stored cache as dirty.
     */
    private void setTotalAmountResourcesStoredCacheDirty() {

        totalAmountResourcesStoredCacheDirty = true;

        // Set total inventory mass cache dirty as well.
        setTotalInventoryMassCacheDirty();

        // Mark owner unit's total resources stored as dirty, if any.
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setTotalAmountResourcesStoredCacheDirty();
            }
        }
    }

    /**
     * Gets the total amount resource stored cache value.
     * @param allowDirty true if cache value can be dirty.
     * @return total amount resources stored cache value.
     */
    public double getTotalAmountResourcesStoredCache(boolean allowDirty) {

        // Update total amount resources stored cache if it is dirty.
        if (!allowDirty && totalAmountResourcesStoredCacheDirty) {
            updateTotalAmountResourcesStoredCache();
        }

        return totalAmountResourcesStoredCache;
    }

    /**
     * Update the total amount resources stored cache as well as the container's cache if any.
     */
    private void updateTotalAmountResourcesStoredCache() {

        double tempStored = 0D;
        if (resourceStorage != null) {
            tempStored += resourceStorage.getTotalAmountResourcesStored(false);
        }

        if (containedUnits != null) {
            for (Unit unit : containedUnits) {
                tempStored = unit.getInventory().getTotalAmountResourcesStored(false);
            }
        }

        totalAmountResourcesStoredCache = tempStored;
        totalAmountResourcesStoredCacheDirty = false;
    }

    /**
     * Sets the item resource total mass cache as dirty.
     */
    private void setItemResourceTotalMassCacheDirty() {

        itemResourceTotalMassCacheDirty = true;

        // Set total inventory mass cache dirty as well.
        setTotalInventoryMassCacheDirty();
    }

    /**
     * Gets the total amount resource stored cache value.
     * @param allowDirty true if cache value can be dirty.
     * @return total amount resources stored cache value.
     */
    private double getItemResourceTotalMassCache(boolean allowDirty) {

        // Update item resource total mass cache if it is dirty.
        if (itemResourceTotalMassCacheDirty && !allowDirty) {
            updateItemResourceTotalMassCache();
        }

        return itemResourceTotalMassCache;
    }

    /**
     * Update the item resource total mass cache.
     */
    private void updateItemResourceTotalMassCache() {

        double tempMass = 0D;

        if (containedItemResources != null) {
            Set<Entry<ItemResource, Integer>> es = containedItemResources.entrySet();
            for(Entry<ItemResource, Integer> e : es){
                tempMass += e.getValue() * e.getKey().getMassPerItem();
            }
        }

        itemResourceTotalMassCache = tempMass;
        itemResourceTotalMassCacheDirty = false;
    }

    /**
     * Sets the unit total mass cache as dirty.
     */
    private void setUnitTotalMassCacheDirty() {

        unitTotalMassCacheDirty = true;

        // Set total inventory mass cache dirty as well.
        setTotalInventoryMassCacheDirty();
    }

    /**
     * Gets the unit total mass cache value.
     * @param allowDirty true if cache value can be dirty.
     * @return unit total mass cache value.
     */
    private double getUnitTotalMassCache(boolean allowDirty) {

        // Update unit total mass cache if it is dirty.
        if (!allowDirty && unitTotalMassCacheDirty) {
            updateUnitTotalMassCache();
        }

        return unitTotalMassCache;
    }

    /**
     * Update the unit total mass cache.
     */
    private void updateUnitTotalMassCache() {
        double tempMass = 0D;
        if (containedUnits != null) {
            for (Unit unit : containedUnits) {
                tempMass += unit.getMass();
            }
        }
        unitTotalMassCache = tempMass;
        unitTotalMassCacheDirty = false;
    }

    /**
     * Sets the total inventory mass cache as dirty.
     */
    private void setTotalInventoryMassCacheDirty() {

        totalInventoryMassCacheDirty = true;

        // Set owner's unit total mass to dirty, if any.
        if (owner != null) {
            Unit container = owner.getContainerUnit();
            if (container != null) {
                container.getInventory().setUnitTotalMassCacheDirty();
            }
        }
    }

    /**
     * Gets the total inventory mass cache value.
     * @param allowDirty true if cache value can be dirty.
     * @return total inventory mass cache value.
     */
    private double getTotalInventoryMassCache(boolean allowDirty) {

        // Update total inventory mass cache if it is dirty.
        if (!allowDirty && totalInventoryMassCacheDirty) {
            updateTotalInventoryMassCache();
        }

        return totalInventoryMassCache;
    }

    /**
     * Update the total inventory mass cache.
     */
    private void updateTotalInventoryMassCache() {

        double tempMass = 0D;

        // Add total amount resource mass stored.
        tempMass += getTotalAmountResourcesStored(false);

        // Add general storage mass.
        tempMass += getGeneralStoredMass(false);

        totalInventoryMassCache = tempMass;
        totalInventoryMassCacheDirty = false;
    }

    /**
     * Creates a clone of this inventory (not including the inventory contents).
     * @param owner the unit owner of the inventory (or null).
     * @return inventory clone.
     */
    public Inventory clone(Unit owner) {

        Inventory result = new Inventory(owner);
        result.addGeneralCapacity(generalCapacity);

        if (resourceStorage != null) {
            for (Entry<AmountResource,Double> entry : resourceStorage.getAmountResourceTypeCapacities().entrySet()) {
                result.addAmountResourceTypeCapacity(
                        entry.getKey(),
                        entry.getValue()
                        );
            }
            for (Entry<PhaseType,Double> entry : resourceStorage.getAmountResourcePhaseCapacities().entrySet()) {
                result.addAmountResourcePhaseCapacity(
                        entry.getKey(),
                        entry.getValue()
                        );
            }
        }

        return result;
    }

    public Unit getOwner() {
    	return owner;
    }
    
    public void restoreARs(AmountResource[] ars) {
    	if (resourceStorage != null)
    		resourceStorage.restoreARs(ars);
    }
    
    //public AmountResourceStorage getAmountResourceStorage() {
    //	return resourceStorage;
    //}
    
    
    /**
     * Prepare object for garbage collection.
     */
    public void destroy() {

        owner = null;
        //if (containedUnits != null) containedUnits.clear();
        containedUnits = null;
        //if (containedItemResources != null) containedItemResources.clear();
        containedItemResources = null;
        
        if (resourceStorage != null) resourceStorage.destroy();
        resourceStorage = null;
        //if (amountResourceCapacityCache != null) amountResourceCapacityCache.clear();
        //amountResourceCapacityCache = null;
        //if (amountResourceCapacityCacheDirty != null) amountResourceCapacityCacheDirty.clear();
        //amountResourceCapacityCacheDirty = null;
        //if (amountResourceStoredCache != null) amountResourceStoredCache.clear();
        //amountResourceStoredCache = null;
        //if (amountResourceStoredCacheDirty != null) amountResourceStoredCacheDirty.clear();
        //amountResourceStoredCacheDirty = null;
        //if (allStoredAmountResourcesCache != null) allStoredAmountResourcesCache.clear();
        //allStoredAmountResourcesCache = null;
        capacityCache = null;
        capacityCacheDirty = null;
        storedCacheDirty = null;
        allStoredARCache = null;

        containersCapacityCache = null;
        containersCapacityCacheDirty = null;
        storedCache = null;
        containersStoredCache = null;
        containersStoredCacheDirty = null;
    }

    /**
     * Implementing readObject method for serialization.
     * @param in the input stream.
     * @throws IOException if error reading from input stream.
     * @throws ClassNotFoundException if error creating class.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        // Initialize transient variables that need it.
        allStoredAmountResourcesCacheDirty = true;
        totalAmountResourcesStoredCacheDirty = true;
        itemResourceTotalMassCacheDirty = true;
        unitTotalMassCacheDirty = true;
        totalInventoryMassCacheDirty = true;
    }
}