/**
 * Mars Simulation Project
 * MarsProjectFX.java
 * @version 3.1.0 2016-09-30
 * @author Manny Kung
 */
package org.mars_sim.msp.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.mars_sim.msp.core.LogConsolidated;
import org.mars_sim.msp.core.Simulation;
import org.mars_sim.msp.core.SimulationConfig;
import org.mars_sim.msp.ui.javafx.config.ScenarioConfigEditorFX;
import org.mars_sim.msp.ui.javafx.mainmenu.MainMenu;

import org.mars_sim.msp.ui.helpGenerator.HelpGenerator;


/**--------------------------------------------------------------
 * Case A : with '-headless' and '-new' switch, it will load the following :
 **--------------------------------------------------------------
 *
 * 1. main() 								-- on Main Thread
 * 2. Default Constructor 					-- on JavaFX Application Thread
 * 3. init()								-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()					-- on pool-2-thread-1
 * 	- prepare()								-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()			-- on pool-2-thread-1
 *  - Simulation.createNewSimulation()		-- on pool-2-thread-1
 *  - startSimulation(true)					-- on pool-2-thread-1
 * 4. Simulation's start() 					-- on JavaFX Application Thread
 *
 * Note 1 : clockScheduler is initialized on Simulation's start()
 * Note 2 : autosave will save
 *
 **--------------------------------------------------------------
 * Case B : with '-headless' and '-load' switch, it will load the following :
 **--------------------------------------------------------------
 *
 * 1. main() 								-- on Main Thread
 * 2. Default Constructor 					-- on JavaFX Application Thread
 * 3. init()								-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()					-- on pool-2-thread-1
 * 	- prepare()								-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()			-- on pool-2-thread-1
 *  - Simulation.createNewSimulation()		-- on pool-2-thread-1
 *
 *  Two Scenarios:
 *
 *  (a). if user provides a particular filename for the saved sim,
 *  - handleLoadSimulation()				-- on pool-2-thread-1
 *
 *  (b). if user DO NOT provide the filename, load 'default.sim',
 * 	- handleLoadSimulation()				-- on pool-2-thread-1
 *
 *
 *  - startSimulation(true)					-- on pool-2-thread-1
 *
 * 4. start() 								-- on JavaFX Application Thread
 *
 *
 *
 * 6. start() 						-- on JavaFX Application Thread
 * 7. initializeSimulation() 		-- on pool-2-thread-1
 * 8a. handleLoadDefaultSimulation() -- on pool-2-thread-1
 * OR if user provides the filename of the saved sim
 * 8b. handleLoadSimulation()		-- on pool-2-thread-1
 * 9. startSimulation() 			-- on pool-2-thread-1
 * 10. Simulation's start()			-- on pool-2-thread-1
 *
 **--------------------------------------------------------------
 * Case C : with '-html' switch ('-headless' is optional in this case), it will load the following :
 **--------------------------------------------------------------
 *
 * 1. main() 						-- on Main Thread
 * 2. Default Constructor 			-- on JavaFX Application Thread
 * 3. init()						-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()			-- on pool-2-thread-1
 * 	- prepare()						-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()	-- on pool-2-thread-1
 * 4. start() 						-- on JavaFX Application Thread
 *
 *
 * 6. SimulationConfig.loadConfig() -- on pool-2-thread-1
 * 7. HelpGenerator.generateHtmlHelpFiles() in handleNewSimulation()
 *
 *
 **--------------------------------------------------------------
 * Case D_ (D1 or D2) : with only '-new' switch (GUI mode is implied), it will load the Main Menu,
 **--------------------------------------------------------------
 *
 * 1. main() 						-- on Main Thread
 * 2. Default Constructor 			-- on JavaFX Application Thread
 * 3. init()						-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()			-- on pool-2-thread-1
 * 	- prepare()						-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()	-- on pool-2-thread-1
 * 4. start() 						-- on JavaFX Application Thread
 * 	- new MainMenu()				-- on JavaFX Application Thread
 * 	- MainMenu's initMainMenu() 				-- on JavaFX Application Thread
 * 	- MainMenu's setupMainSceneStage()			-- on JavaFX Application Thread
 *
 *
 *
 **--------------------------------------------------------------
 * Case D1 : if choosing the first option 'New Sim' in the Main Menu to start a new sim,
 **--------------------------------------------------------------
 *
 * Step 1 to 4 : same as in Case D_
 *
 * Upon choosing the 'New Sim' option in the Main Menu,
 *
 * 5. MainMenu's runOne() 		 				-- on JavaFX Application Thread
 * 6. new MainScene()							-- on JavaFX Application Thread
 * 7. MarsProjectFX's handleNewSimulation()		-- on JavaFX Application Thread
 * 	- ConfigEditorTask's run()					-- on pool-2-thread-1
 * 8. new ScenarioConfigEditorFX()				-- on pool-2-thread-1
 * 	- LoadSimulationTask's run()				-- on pool-2-thread-1
 *  -- Simulation's createNewSimulation()		-- on pool-2-thread-1
 * 	-- Simulation's start()						-- on pool-2-thread-1
 * 9. MainMenu's finalizeMainScene()			-- on JavaFX Application Thread
 *
 *
 **--------------------------------------------------------------
 * Case D2 : if choosing the second option 'Load Sim' in the Main Menu to load a saved sim,
 **--------------------------------------------------------------
 *
 * Step 1 to 4 : same as in Case D_

 * Upon choosing the 'Load Sim' option in the Main Menu,
 *
 * 5. MainMenu's runTwo() 		 				-- on JavaFX Application Thread
 * 6. loadSim(false)
 *  - new MainScene()							-- on JavaFX Application Thread
 * 7. In MainMenu's loadSim() 					-- on JavaFX Application Thread
 *
 *  - Wait for user to select a saved sim to load in loadSim()...
 *
 *  - Upon choosing a particular saved sim file,
 *
 * 8. Main Menu's LoadSimulationTask's run()	-- on pool-2-thread-1
 *  - Simulation's createNewSimulation()		-- on pool-2-thread-1
 *  - Simulation's loadSimulation()				-- on pool-2-thread-1
 *  - Simulation's start(autosaveTimer)			-- on pool-2-thread-1
 *  - finalizeMainScene()						-- on JavaFX Application Thread
 *
 *--------------------------------------------------------------
 * Case E : with '-load' switch (GUI mode is implied), it will load the following :
 **--------------------------------------------------------------
 *
 * 1. main() 									-- on Main Thread
 * 2. Default Constructor 						-- on JavaFX Application Thread
 * 3. init()									-- on JavaFX-Launcher Thread
 * 	- new SimulationTask()						-- on pool-2-thread-1
 * 	- prepare()									-- on pool-2-thread-1
 * 	- SimulationConfig.loadConfig()				-- on pool-2-thread-1
 * 	- Simulation.createNewSimulation()			-- on pool-2-thread-1
 * 4. start()
 * 	- MainMenu's setupMainSceneStage()			-- on JavaFX Application Thread
 * 	- MainMenu's loadSim() 						-- on JavaFX Application Thread
 * 5. In MainMenu's loadSim() 					-- on JavaFX Application Thread
 *
 *  - Wait for user to select a saved sim to load in loadSim()...
 *
 *  - Upon choosing a particular saved sim file,
 *
 * 6. Main Menu's LoadSimulationTask's run()	-- on pool-2-thread-1
 *  - Simulation's loadSimulation()				-- on pool-2-thread-1
 *  - Simulation's start(autosaveTimer)			-- on pool-2-thread-1
 *  - finalizeMainScene()						-- on JavaFX Application Thread
 *
 *
*/


/**
 * MarsProjectFX is the main class for MSP. It creates JavaFX/8 application thread.
 */
@SuppressWarnings("restriction")
public class MarsProjectFX extends Application  {

    /** initialized logger for this class. */
    private static Logger logger = Logger.getLogger(MarsProjectFX.class.getName());

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

    //private boolean isDone;

    private String loadFileString;

    private MainMenu mainMenu;

    private List<String> argList;

    private static Simulation sim = Simulation.instance();

    /*
     * Default Constructor
     */
    //public MarsProjectFX() {
	   	//logger.info("MarsProjectFX's constructor is on " + Thread.currentThread().getName());
    	//marsProjectFX = this;
/*
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
        System.out.println(javaCompiler.toString());

        Set<SourceVersion> sourceVersion;
        sourceVersion = javaCompiler.getSourceVersions();

        for (SourceVersion version : sourceVersion) {
            System.out.print(version.name() + "\n");
        }

        System.out.print("availableProcessors = " + Runtime.getRuntime().availableProcessors() + "\n");
*/
    //}

	/*
     * Initiates any tasks or methods on a JavaFX-Launcher Thread
     * @see javafx.application.Application#init()
     */
    @SuppressWarnings("restriction")
	@Override
    public final void init() throws Exception {
	   	//logger.info("MarsProjectFX's init() is on " + Thread.currentThread().getName() );
	   	// INFO: MarsProjectFX's init() is on JavaFX-Launcher Thread
		setLogging();
		setDirectory();
        // general text antialiasing
        System.setProperty("swing.aatext", "true");
        //System.setProperty("awt.useSystemAAFontSettings","lcd"); // for newer VMs
        //Properties props = System.getProperties();
        //props.setProperty("swing.jlf.contentPaneTransparent", "true");

    	//logger.info(Simulation.title);
    	LogConsolidated.log(logger, Level.INFO, 0, logger.getName(), Simulation.title, null);

        //System.getProperty("java.version").compareTo("1.7.0_45") >= 0;

        String major, minor, update, build = null, dateStamp = null;

        // see http://docs.oracle.com/javase/7/docs/api/java/lang/System.html#getProperties%28%29

        String bit = (System.getProperty("os.arch").contains("64") ? "64-bit" : "32-bit");

        //String[] javaVersionElements = Simulation.JAVA_VERSION.split("\\.|-|-b| ");
        String[] javaVersionElements = Simulation.JAVA_VERSION.split("\\.|-|_| ");

        // e.g. 8.0.111 (Thu Nov 24 14:50:47 UTC 2016) in case of openjdk 8 in linux
        major = javaVersionElements[0];
        minor   = javaVersionElements[1];
        update  = javaVersionElements[2];

        if (javaVersionElements.length > 3) {
        	build = javaVersionElements[3];
        	dateStamp = Simulation.JAVA_VERSION.substring(Simulation.JAVA_VERSION.indexOf(build));
        }

    	//if (!vendor.startsWith("Oracle") ||  // TODO: find out if other vendor's VM works
    	if (!"8".equals(minor) || Double.parseDouble(build) < 77.0) {
    		//logger.log(Level.SEVERE, "Note: mars-sim requires at least Java 8.0.77. Terminating...");
    		exitWithError("Note: mars-sim requires at least Java 8.0.77. Terminated.");
    	}

    	else {
    		argList = Arrays.asList(args);
            newSim = argList.contains("-new");
            loadSim = argList.contains("-load");
    		generateHTML = argList.contains("-html");
    		helpPage = argList.contains("-help");
    		//savedSim = argList.contains(".sim");

    		if (generateHTML || helpPage || argList.contains("-headless"))
    			headless = true;

        	int size = argList.size();
        	boolean flag = true;
    		for (int i= 0; i<size; i++) {
    			if (argList.get(i).contains(".sim")) {
    				if (flag) {
    					loadFileString = argList.get(i);
    					savedSim = true;
    					flag = false;
    				}
    				else {
    					exitWithError("Cannot load more than one saved sim.");
    				}
    			}
    		}

    	   	sim.startSimExecutor();
    	   	sim.getSimExecutor().execute(new SimulationTask());
    	}

    }

	public class SimulationTask implements Runnable {
		public void run() {
			prepare();
		}
    }

	public void prepare() {
        SimulationConfig.loadConfig();

	    if (!headless) {
	    	// Using GUI mode
	    	if (Simulation.OS.startsWith ("Windows")) {
	    		System.setProperty( "sun.java2d.noddraw", "false" );
	    		System.setProperty( "sun.java2d.ddscale","true" );
	    		System.setProperty( "sun.java2d.ddforcevram", "true" );
	    	}
	    	//logger.info("Running " + Simulation.OS + " in GUI mode");
	    	//if (Simulation.OS.startsWith ("Mac"))
	    	//	System.setProperty("sun.java2d.opengl", "true"); // NOT WORKING IN MACCOSX, causing delay in JPopMenu in Windows
	       	// Enable capability of loading of svg image using regular method
	    	//SvgImageLoaderFactory.install();
		   	if (newSim) {
		   		// CASE D1 and D2 //
		   		// Note 1 : should NOT run createNewSimulation() until after clicking "Start" in Config Editor
	   		}

	   		else if (loadSim) {
		   		// CASE E //
	   		}

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

	@Override
	public void start(Stage primaryStage) {	
	   	//logger.info("MarsProjectFX's start() is on " + Thread.currentThread().getName());
	   	if (!headless) {
		   //logger.info("start() : in GUI mode, loading the Main Menu");

	   		mainMenu = new MainMenu();//this);

	   		if (newSim) {
		   		// CASE D1 and D2//
	   	    	logger.info("Starting a new sim in GUI mode in " + Simulation.OS);
	   			mainMenu.initMainMenu(primaryStage);

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
	}

	public List<String> getArgList() {
		return argList;
	}

    /**
     * Loads the simulation from the default save file.
     * @throws Exception if error loading the default saved simulation.
     */
    private void handleLoadDefaultSimulation() {
		//logger.info("MarsProjectFX's handleLoadDefaultSimulation() is on "+Thread.currentThread().getName());
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


    /**
     * Loads the simulation from a save file.
     * @param argList the command argument list.
     * @throws Exception if error loading the saved simulation.
     */
    void handleLoadSimulation(File loadFile) throws Exception {
		//logger.info("MarsProjectFX's handleLoadSimulation() is on "+Thread.currentThread().getName() );
    	// INFO: MarsProjectFX's handleLoadSimulation() is in JavaFX Application Thread Thread
		logger.info("Loading user's saved sim in headless mode in " + Simulation.OS);
    	try {

            if (loadFile.exists() && loadFile.canRead()) {

                sim.loadSimulation(loadFile);

        		//2017-04-07 Add newInstance()
        		//AmountResource.newInstance();

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
		//logger.info("MarsProjectFX's handleNewSimulation() is on "+Thread.currentThread().getName() );
		// MarsProjectFX's handleNewSimulation() is in JavaFX Application Thread Thread
		//isDone = true;
		logger.info("Creating a new sim in " + Simulation.OS);
        try {
            //SimulationConfig.loadConfig(); // located to prepare()
           	sim.getSimExecutor().execute(new ConfigEditorTask());

        } catch (Exception e) {
            e.printStackTrace();
            exitWithError("Error : could not create a new simulation ", e);
        }
    }

	public class ConfigEditorTask implements Runnable {
		  public void run() {
			  //logger.info("MarsProjectFX's ConfigEditorTask's run() is on " + Thread.currentThread().getName() );
			  new ScenarioConfigEditorFX(mainMenu);
		  }
	}

    /**
     * Start the simulation instance.
     * @param autosaveDefaultName use the default name for autosave
     */
    public void startSimulation(boolean autosaveDefaultName) {
		//logger.info("MarsProjectFX's startSimulation() is on "+Thread.currentThread().getName() );
        // Start the simulation.
        sim.start(autosaveDefaultName);
    }

    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void exitWithError(String message, Exception e) {
        showError(message, e);
        Platform.exit();
        System.exit(1);
    }

    /**
     * Exit the simulation with an error message.
     * @param message the error message.
     */
    private void exitWithError(String message) {
        logger.log(Level.SEVERE, message);
        Platform.exit();
        System.exit(1);
    }

    /**
     * Show a modal error message dialog.
     * @param message the error message.
     * @param e the thrown exception or null if none.
     */
    private void showError(String message, Exception e) {
        if (e != null) {
            logger.log(Level.SEVERE, message, e);
        }
        else {
            logger.log(Level.SEVERE, message);
        }

        //if (!headless) {
        //	if (!Simulation.OS.contains("mac"))
        //		JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        	// Warning: cannot load the editor in macosx if it was a JDialog
        //}
    }

    public void setDirectory() {
        new File(System.getProperty("user.home"), Simulation.MARS_SIM_DIRECTORY + File.separator + "logs").mkdirs();
    }


    public void setLogging() {
    	//logger.info("setLogging() is on " + Thread.currentThread().getName() );
        try {
            LogManager.getLogManager().readConfiguration(MarsProjectFX.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load logging properties", e);
            try {
                LogManager.getLogManager().readConfiguration();
            } catch (IOException e1) {
                logger.log(Level.WARNING, "Could read logging default config", e);
            }
        }
    }

/*
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (appLaunch != null) {
            appLaunch.start(this, primaryStage);
        }
    }

    @Override
    public void stop() throws Exception {
	   	//logger.info("MarsProjectFX's stop is on " + Thread.currentThread().getName() );
    }
*/
    
    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException{
    	//logger.info("MarsProjectFX's main() is on " + Thread.currentThread().getName() + " Thread" );
    	MarsProjectFX.args = args;
/*
        // 2015-10-13  Added command prompt console
        Console console = System.console();
        if(console == null && !GraphicsEnvironment.isHeadless()){
            String filename = MarsProjectFX.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
            Runtime.getRuntime().exec(new String[]{"cmd","/c","start","cmd","/k","java -jar \"" + filename + "\""});
        }else{
        	//MarsProjectFX.main(new String[0]);
            System.out.println("Program has ended, please type 'exit' to close the console");
        }
*/
/*    	
    	for (String env: args) {
            String value = System.getenv(env);
            if (value != null) {
                System.out.format("%s=%s%n",
                                  env, value);
            } else {
                System.out.format("%s is"
                    + " not assigned.%n", env);
            }
        }
*/    	
        launch(args);
    }

}