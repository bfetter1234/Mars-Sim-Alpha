/**
 * Mars Simulation Project
 * UnitIterator.java
 * @version 2.73 2001-10-24
 * @author Scott Davis 
 */

package org.mars_sim.msp.simulation;

/** 
 *  The UnitIterator class is an iterator over a UnitCollection. 
 */
public interface UnitIterator {
 
    /**
     *  Returns the next element in the iteration.
     *  @return the next element in the iteration.
     */
    public Unit next();

    /**
     *  Returns true if the iteration has more elements.
     *  @return true if the iterator has more elements.
     */
    public boolean hasNext();

    /**
     *  Removes from the underlying collection the 
     *  last element returned by the iterator.
     */
    public void remove();
}
