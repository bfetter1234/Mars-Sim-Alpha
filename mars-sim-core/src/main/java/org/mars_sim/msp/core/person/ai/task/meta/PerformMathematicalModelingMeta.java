/**
 * Mars Simulation Project
 * PerformMathematicalModelingMeta.java
 * @version 3.08 2015-06-08
 * @author Scott Davis
 */
package org.mars_sim.msp.core.person.ai.task.meta;

import java.io.Serializable;
import java.util.Iterator;
import java.util.logging.Logger;

import org.mars_sim.msp.core.Lab;
import org.mars_sim.msp.core.Msg;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.person.LocationSituation;
import org.mars_sim.msp.core.person.Person;
import org.mars_sim.msp.core.person.ai.job.Job;
import org.mars_sim.msp.core.person.ai.task.PerformLaboratoryExperiment;
import org.mars_sim.msp.core.person.ai.task.PerformMathematicalModeling;
import org.mars_sim.msp.core.person.ai.task.Task;
import org.mars_sim.msp.core.robot.Robot;
import org.mars_sim.msp.core.science.ScienceType;
import org.mars_sim.msp.core.science.ScientificStudy;
import org.mars_sim.msp.core.science.ScientificStudyManager;

/**
 * Meta task for the PerformMathematicalModeling task.
 */
public class PerformMathematicalModelingMeta implements MetaTask, Serializable {

    /** default serial id. */
    private static final long serialVersionUID = 1L;
    
    /** Task name */
    private static final String NAME = Msg.getString(
            "Task.description.performMathematicalModeling"); //$NON-NLS-1$

    /** default logger. */
    private static Logger logger = Logger.getLogger(PerformMathematicalModelingMeta.class.getName());

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Task constructInstance(Person person) {
        return new PerformMathematicalModeling(person);
    }

    @Override
    public double getProbability(Person person) {

        double result = 0D;

        if (person.getLocationSituation() == LocationSituation.IN_VEHICLE) {	
	        // Check if person is in a moving rover.
	        if (PerformLaboratoryExperiment.inMovingRover(person)) {
	            result = -50D;
	            return 0;
	        }
	        else
	        // the penalty for performing experiment inside a vehicle
	        	result = -20D;
        }
        
        if (person.getLocationSituation() == LocationSituation.IN_SETTLEMENT
            	|| person.getLocationSituation() == LocationSituation.IN_VEHICLE) {

	        ScienceType mathematics = ScienceType.MATHEMATICS;

	        // Add probability for researcher's primary study (if any).
	        ScientificStudyManager studyManager = Simulation.instance().getScientificStudyManager();
	        ScientificStudy primaryStudy = studyManager.getOngoingPrimaryStudy(person);
	        if ((primaryStudy != null) && ScientificStudy.RESEARCH_PHASE.equals(primaryStudy.getPhase())) {
	            if (!primaryStudy.isPrimaryResearchCompleted()) {
	                if (mathematics == primaryStudy.getScience()) {
	                    try {
	                        Lab lab = PerformMathematicalModeling.getLocalLab(person);
	                        if (lab != null) {
	                            double primaryResult = 50D;

	                            // Get lab building crowding modifier.
	                            primaryResult *= PerformMathematicalModeling.getLabCrowdingModifier(person, lab);

	                            // If researcher's current job isn't related to study science, divide by two.
	                            Job job = person.getMind().getJob();
	                            if (job != null) {
	                                ScienceType jobScience = ScienceType.getJobScience(job);
	                                if (primaryStudy.getScience() != jobScience) {
	                                    primaryResult /= 2D;
	                                }
	                            }

	                            result += primaryResult;
	                        }
	                    }
	                    catch (Exception e) {
	                        logger.severe("getProbability(): " + e.getMessage());
	                    }
	                }
	            }
	        }

	        // Add probability for each study researcher is collaborating on.
	        Iterator<ScientificStudy> i = studyManager.getOngoingCollaborativeStudies(person).iterator();
	        while (i.hasNext()) {
	            ScientificStudy collabStudy = i.next();
	            if (ScientificStudy.RESEARCH_PHASE.equals(collabStudy.getPhase())) {
	                if (!collabStudy.isCollaborativeResearchCompleted(person)) {
	                    ScienceType collabScience = collabStudy.getCollaborativeResearchers().get(person);
	                    if (mathematics == collabScience) {
	                        try {
	                            Lab lab = PerformMathematicalModeling.getLocalLab(person);
	                            if (lab != null) {
	                                double collabResult = 25D;

	                                // Get lab building crowding modifier.
	                                collabResult *= PerformMathematicalModeling.getLabCrowdingModifier(person, lab);

	                                // If researcher's current job isn't related to study science, divide by two.
	                                Job job = person.getMind().getJob();
	                                if (job != null) {
	                                    ScienceType jobScience = ScienceType.getJobScience(job);
	                                    if (collabScience != jobScience) {
	                                        collabResult /= 2D;
	                                    }
	                                }

	                                result += collabResult;
	                            }
	                        }
	                        catch (Exception e) {
	                            logger.severe("getProbability(): " + e.getMessage());
	                        }
	                    }
	                }
	            }
	        }

	        // Effort-driven task modifier.
	        result *= person.getPerformanceRating();

	        // Job modifier.
	        Job job = person.getMind().getJob();
	        if (job != null) {
	            result *= job.getStartTaskProbabilityModifier(PerformMathematicalModeling.class);
	        }

	        // Modify if lab experimentation is the person's favorite activity.
	        if (person.getFavorite().getFavoriteActivity().equalsIgnoreCase("Lab Experimentation")) {
	            result *= 2D;
	        }

	        // 2015-06-07 Added Preference modifier
            if (result > 0)
            	result = result + result * person.getPreference().getPreferenceScore(this)/5D;

	        if (result < 0) result = 0;

        }

        return result;
    }

	@Override
	public Task constructInstance(Robot robot) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getProbability(Robot robot) {
		// TODO Auto-generated method stub
		return 0;
	}
}