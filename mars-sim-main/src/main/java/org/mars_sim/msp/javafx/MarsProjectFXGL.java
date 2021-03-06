/**
 * Mars Simulation Project
 * MarsProjectFXGL.java
 * @version 3.1.0 2017-11-11
 * @author Manny KUng
 */

package org.mars_sim.msp.javafx;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;
import org.mars_sim.msp.ui.javafx.config.ScenarioConfigEditorFX;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.settings.GameSettings;

import javafx.application.Platform;

@SuppressWarnings("restriction")
public class MarsProjectFXGL extends GameApplication {

	/** initialized logger for this class. */
	private static Logger logger = Logger.getLogger(MarsProjectFXGL.class.getName());

	static String[] args;
 
	private static final String manpage = "\n> java -jar mars-sim-main-[version/build].jar\n"
		+ "                    (Note : start a new sim)\n"
		+ "   or  \n"
		+ "\n"
		+ "> java -jar jarfile [args...]\n"
		+ "                    (Note : start mars-sim with arguments)\n"
		+ "\n"
		+ "  where args include :\n"
		+ "\n"
		+ "    new             start a new sim (by default)\n"
		+ "                    (Note : if 'load' is absent, 'new' is automatically appended.)\n"
		+ "    headless        run in console mode and without the graphical interface\n"
		+ "    0               256MB Min, 1024MB Max (by default)\n"
		+ "    1               256MB Min, 512MB Max\n"
		+ "    2               256MB Min, 768MB Max\n"
		+ "    3               256MB Min, 1024MB Max\n"
		+ "    4               256MB Min, 1536MB Max\n"
		+ "    5               256MB Min, 2048MB Max\n"
		+ "    load            go to directory /.mars-sim/saved/ and wait for user to choose a saved sim\n"
		+ "    load 123.sim    load the saved sim with filename '123.sim'\n"
		+ "                    (Note : '123.sim' must be located at the same directory as the jarfile.)\n";

	/** true if displaying graphic user interface. */
    private boolean headless = false, newSim = false, loadSim = false, savedSim = false;
    /** true if help documents should be generated from config xml files. */
    private boolean generateHTML = false, helpPage = false;

    private String loadFileString;

    private MainMenu mainMenu;

    private List<String> argList;

    private static Simulation sim = Simulation.instance();
    
	@Override
	protected void initSettings(GameSettings settings) {
		settings.setWidth(1024);
		settings.setHeight(768);
		//settings.setStageStyle(StageStyle.UNDECORATED);
		settings.setTitle("Mars Simulation Project");
		settings.setVersion("3.1.0");
		settings.setProfilingEnabled(false); // turn off fps
		settings.setCloseConfirmation(false); // turn off exit dialog
		settings.setIntroEnabled(false); // turn off intro
		settings.setMenuEnabled(false); // turn off menus
		settings.setCloseConfirmation(true);
	}

	@Override
	protected void initInput() {
/*		
		Input input = getInput(); // get input service

		input.addAction(new UserAction("Move Right") {
			@Override
			protected void onAction() {
				player.translateX(5); // move right 5 pixels
				getGameState().increment("pixelsMoved", +5);
			}
		}, KeyCode.D);

		input.addAction(new UserAction("Move Left") {
			@Override
			protected void onAction() {
				player.translateX(-5); // move left 5 pixels
				getGameState().increment("pixelsMoved", +5);
			}
		}, KeyCode.A);

		input.addAction(new UserAction("Move Up") {
			@Override
			protected void onAction() {
				player.translateY(-5); // move up 5 pixels
				getGameState().increment("pixelsMoved", +5);
			}
		}, KeyCode.W);

		input.addAction(new UserAction("Move Down") {
			@Override
			protected void onAction() {
				player.translateY(5); // move down 5 pixels
				getGameState().increment("pixelsMoved", +5);
			}
		}, KeyCode.S);
*/	
	}

	@Override
	protected void initGameVars(Map<String, Object> vars) {
		//vars.put("pixelsMoved", 0);
	}

	//private GameEntity player;

	@Override
	protected void initGame() {
/*
		player = Entities.builder().at(300, 300).viewFromTexture("brick.png").buildAndAttach(getGameWorld());

		getGameState().<Integer>addListener("pixelsMoved", (prev, now) -> {
			if (now % 100 == 0) {
				getAudioPlayer().playSound("drop.wav");
			}
		});
*/		
		setLogging();
		setDirectory();
		// general text antialiasing
		System.setProperty("swing.aatext", "true");

		LogConsolidated.log(logger, Level.INFO, 0, logger.getName(), Simulation.title, null);

		String major, minor, update, build = null, dateStamp = null;

		String bit = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");

		String[] javaVersionElements = Simulation.JAVA_VERSION.split("\\.|-|_| ");

		// e.g. 8.0.111 (Thu Nov 24 14:50:47 UTC 2016) in case of openjdk 8 in linux
		major = javaVersionElements[0];
		minor = javaVersionElements[1];
		update = javaVersionElements[2];

		if (javaVersionElements.length > 3) {
			build = javaVersionElements[3];
			dateStamp = Simulation.JAVA_VERSION.substring(Simulation.JAVA_VERSION.indexOf(build));
		}

		if (!"8".equals(minor) || Double.parseDouble(build) < 77.0) {
			exitWithError("Note: mars-sim requires at least Java 8.0.77. Terminated.");
		}

		else {
			argList = Arrays.asList(args);
			newSim = argList.contains("-new");
			loadSim = argList.contains("-load");
			generateHTML = argList.contains("-html");
			helpPage = argList.contains("-help");
			// savedSim = argList.contains(".sim");

			if (generateHTML || helpPage || argList.contains("-headless"))
				headless = true;

			int size = argList.size();
			boolean flag = true;
			for (int i = 0; i < size; i++) {
				if (argList.get(i).contains(".sim")) {
					if (flag) {
						loadFileString = argList.get(i);
						savedSim = true;
						flag = false;
					} else {
						exitWithError("Cannot load more than one saved sim.");
					}
				}
			}

			// Insert the GameWorld instance into Simulation class
			//sim.setGameWorld(getGameWorld());
			sim.setFXGL(true);
			
			sim.startSimExecutor();
			sim.getSimExecutor().execute(new SimulationTask());
			
		}		
		
	}

	@Override
	protected void initUI() {
	
	   	if (!headless) {
		   //logger.info("start() : in GUI mode, loading the Main Menu");
	   		
	   		mainMenu = new MainMenu();//this);

	   		if (newSim) {
		   		// CASE D1 and D2//
	   	    	logger.info("Starting a new sim in GUI mode in " + Simulation.OS);
	   			mainMenu.initMainMenu(getGameScene());

		   		// Now in the Main Menu, wait for user to pick either options
		   		// 1. 'New Sim' - call runOne(), go to ScenarioConfigEditorFX
		   		// 2. 'Load Sim' -call runTwo(), need to call sim.runStartTask(false);
	   		}

	   		else if (loadSim) {
		   		// CASE E //

	        	if (savedSim) {

					logger.info("Loading user's saved sim in GUI mode in " + Simulation.OS);

	        		File loadFile = new File(loadFileString);

		            try {
		            	// load loadFile directly without opening the FileChooser
		            	mainMenu.loadSim(loadFile);

		            } catch (Exception e) {
		                e.printStackTrace();
		                exitWithError("Could not load the user's saved sim. ", e);

		            }
	        	}

	        	else {
	        		// if user wants to load the default saved sim
					logger.info("Loading a saved sim with FileChooser in GUI mode in " + Simulation.OS);

	        		try {
	                	// load FileChooser instead
			   			mainMenu.loadSim(null);
			   			// Then wait for user to select a saved sim to load in loadSim();

		            } catch (Exception e2) {
		                //e2.printStackTrace();
		            	exitWithError("Could not load the default saved sim. ", e2);

		            }
	        	}
	   		}
		}
/*
		else {
		   	logger.info("Entering headless mode and skip loading the Main Menu");
		   	if (newSim) {
		   	// CASE A //
	   		}
	   		else if (loadSim) {
		   		// CASE B //
	   		}
			else if (generateHTML) {
		   		// CASE C //
			}
			else if (helpPage) {
		   		// CASE D //
			}
		}
*/		
	}

	public void setLogging() {
		try {
			LogManager.getLogManager()
					.readConfiguration(MarsProjectFX.class.getResourceAsStream("/logging.properties"));
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load logging properties", e);
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException e1) {
				logger.log(Level.WARNING, "Could read logging default config", e);
			}
		}
	}

	public void setDirectory() {
		new File(System.getProperty("user.home"), Simulation.MARS_SIM_DIRECTORY + File.separator + "logs").mkdirs();
	}

	public class ConfigEditorTask implements Runnable {
		public void run() {
			new ScenarioConfigEditorFX(mainMenu);
		}
	}

	/**
	 * Start the simulation instance.
	 * @param autosaveDefaultName
	 *            use the default name for autosave
	 */
	public void startSimulation(boolean autosaveDefaultName) {
		// Start the simulation.
		sim.start(autosaveDefaultName);
	}

	/**
	 * Exit the simulation with an error message.
	 * @param message
	 *            the error message.
	 * @param e
	 *            the thrown exception or null if none.
	 */
	private void exitWithError(String message, Exception e) {
		showError(message, e);
		Platform.exit();
		System.exit(1);
	}

	/**
	 * Exit the simulation with an error message.
	 * 
	 * @param message
	 *            the error message.
	 */
	private void exitWithError(String message) {
		logger.log(Level.SEVERE, message);
		Platform.exit();
		System.exit(1);
	}

	/**
	 * Show a modal error message dialog.
	 * 
	 * @param message
	 *            the error message.
	 * @param e
	 *            the thrown exception or null if none.
	 */
	private void showError(String message, Exception e) {
		if (e != null) {
			logger.log(Level.SEVERE, message, e);
		} else {
			logger.log(Level.SEVERE, message);
		}
	}


	public class SimulationTask implements Runnable {
		public void run() {
			prepare();
		}
    }

	public void prepare() {
	   	// NOTE: NOT supposed to start another instance of the singleton Simulation
        SimulationConfig.loadConfig();

	    if (!headless) {
	    	// Using GUI mode
	    	if (Simulation.OS.startsWith ("Windows")) {
	    		System.setProperty( "sun.java2d.noddraw", "false" );
	    		System.setProperty( "sun.java2d.ddscale","true" );
	    		System.setProperty( "sun.java2d.ddforcevram", "true" );
	    	}

		   	//if (newSim) {
		   		// CASE D1 and D2 //
		   		// Note 1 : should NOT run createNewSimulation() until after clicking "Start" in Config Editor
	   		//}

	   		//else if (loadSim) {
		   		// CASE E //
	   		//}

		} else {
			// Using -headless (GUI-less mode)
			if (newSim) {
		   		// CASE A //
				logger.info("Starting a new sim in headless mode in " + Simulation.OS);
				// Initialize the simulation.
	        	Simulation.createNewSimulation();
			    // Start the simulation.
			    startSimulation(true);
			}

			else if (loadSim) {
		   		// CASE B //
				// Initialize the simulation.
	        	Simulation.createNewSimulation();

	        	if (savedSim) {

	                File loadFile = new File(loadFileString);

		            try {
		            	// try to see if user enter his own saved sim after the "load" argument
		                handleLoadSimulation(loadFile);

		            } catch (Exception e) {
		                e.printStackTrace();
		                showError("Could not load the user's saved sim.", e);
		            }
	        	}

	        	else {
	        		// if user wants to load the default saved sim
	        		try {
	                	// try loading default.sim instead
	                	handleLoadDefaultSimulation();

		            } catch (Exception e2) {
		                e2.printStackTrace();
		            	exitWithError("Could not load the default saved sim.", e2);
		                //showError("Could not load the default saved sim. Starting a new sim now. ", e2);
		            }
	        	}
			}
			// 2016-06-06 Generated html files for in-game help
			else if (generateHTML) {
		   		// CASE C //
				logger.info("Generating help files in headless mode in " + Simulation.OS);

				try {
		            SimulationConfig.loadConfig();
		    	    // this will generate html files for in-game help based on config xml files
		    	    // 2016-04-16 Relocated the following to handleNewSimulation() right before calling ScenarioConfigEditorFX.
		    	    HelpGenerator.generateHtmlHelpFiles();
		    	    logger.info("Done creating help files.");
			        Platform.exit();
			        System.exit(1);

		        } catch (Exception e) {
		            e.printStackTrace();
		            exitWithError("Could not generate help files ", e);
		        }
			}
			else if (helpPage) {
		   		// CASE D //
				//logger.info("Displaying help instructions in headless mode in " + Simulation.OS);
	        	System.out.println(manpage);
		        Platform.exit();
		        System.exit(1);
			}
		}
	}
	

    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    void handleLoadSimulation(File loadFile) throws Exception {
		logger.info("Loading user's saved sim in headless mode in " + Simulation.OS);
    	try {

            if (loadFile.exists() && loadFile.canRead()) {

                sim.loadSimulation(loadFile);

			    // Start the simulation.
			    startSimulation(true);

            } else {
                exitWithError("Could not load the simulation. The sim file " + loadFile +
                        " could not be read or found.", null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Error : problem loading the simulation ", e);
            //logger.log(Level.SEVERE, "Problem loading existing simulation", e);
        }
    }

    /**
     * Create a new simulation instance.
     */
    void handleNewSimulation() {
		logger.info("Creating a new sim in " + Simulation.OS);
        try {
            //SimulationConfig.loadConfig(); // located to prepare()
           	sim.getSimExecutor().execute(new ConfigEditorTask());

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Error : could not create a new simulation ", e);
        }
    }


    /**
     * Loads the simulation from the default save file.
     * @throws Exception if error loading the default saved simulation.
     */
    private void handleLoadDefaultSimulation() {
		logger.info("Loading the default saved sim in headless mode in " + Simulation.OS);

    	try {
            // Load the default saved file "default.sim"
            sim.loadSimulation(null);

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Could not load default simulation", e);
        }

	    // Start the simulation.
	    startSimulation(true);
    }
    
    protected void onUpdate(double tpf) {
    	sim.onUpdate(tpf);
    }
    
	public static void main(String[] args) {
    	MarsProjectFXGL.args = args;
		launch(args);
	}
	
    /**
     * Prepare object for garbage collection.
     */
	public void destroy() {
	    mainMenu = null;
	    argList = null;
	    sim = null;
	}
}
