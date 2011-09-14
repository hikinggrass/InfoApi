package seta.infoapi;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class CommandWorker {

    private Config configuration;

    public CommandWorker(Config cfg) {
	configuration = cfg;
    }

    Logger log = Logger.getLogger("Minecraft");

    /**
     * Process Commands Decides what to do with Commands
     * 
     * @param getString
     * @return
     */
    public String processCommand(String getString) {
	String wString;
	// Preset String so it will write something
	String outputString = "ERROR";
	Integer commandOrdinal;

	if (isValidCommandString(getString)) {
	    // Remove HTTP Request Header Parts
	    wString = getString.substring((getString.lastIndexOf("GET /") + 5), (getString.lastIndexOf(" HTTP/1.1")));

	    // Remove Secret Key and leading Question Mark
	    // it should be save now to use just the command
	    wString = wString.substring(0, wString.lastIndexOf("?"));

	    // Check if there is something else needed
	    if (wString.contains("/")) {
		if (WorldCommands.isPart(wString.substring(0, wString.indexOf("/")))) {
		    commandOrdinal = WorldCommands.getOrdinal(wString.substring(0, wString.indexOf("/")));
		    String worldName = wString.substring(wString.indexOf("/") + 1);

		    outputString = workWorldCommand(commandOrdinal, worldName);
		}

	    } else {
		if (GeneralCommands.isPart(wString)) {
		    commandOrdinal = GeneralCommands.getOrdinal(wString);

		    outputString = workGenericCommand(commandOrdinal);
		}
	    }
	}

	return outputString;
    }

    /**
     * Check if it is even a Valid Command at all
     * 
     * @param getString
     * @return
     */
    public boolean isValidCommandString(String getString) {
	String secretKey = configuration.getConfig("secret").toString();

	if (!getString.isEmpty()) {
	    if (getString.contains("?" + secretKey)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Works Out Generic Commands
     * 
     * @param commandOrdinal
     * @return
     */
    private String workGenericCommand(int commandOrdinal) {
	try {
	    String outputString = "ERROR";

	    if (commandOrdinal != Integer.MIN_VALUE) {

		switch (commandOrdinal) {
		// MAXPLAYER
		case 0:
		    outputString = Integer.toString(Bukkit.getServer().getMaxPlayers());
		    break;
		// ONLINEMODE
		case 1:
		    outputString = Boolean.toString(Bukkit.getServer().getOnlineMode());
		    break;
		// VERSION
		case 2:
		    outputString = Bukkit.getServer().getVersion();
		    break;
		// RAM
		case 3:
		    outputString = getRuntimeMemoryInformationAsString();
		    break;
		// CPU
		case 4:
		    outputString = "Not Possible due of JAVA Limitation";
		    break;
		// RETURN IF NOTHING FIT
		default:
		    outputString = "Not Configured";
		    break;
		}
	    }

	    return outputString;
	} catch (Exception e) {
	    log.info("");

	    return "ERROR";
	}
    }

    private String getRuntimeMemoryInformationAsString() {
	String returnString = "";

	// Total Memory of Java Runtime in MB
	Double totalMemory = Math.floor((Runtime.getRuntime().totalMemory() / Math.pow(10, 6)));

	// Free Memory of Java Runtime in MB
	Double freeMemory = Math.floor((Runtime.getRuntime().freeMemory() / Math.pow(10, 6)));

	// Maximum Memory of Java Runtime in MB
	Double maxMemory = Math.floor((Runtime.getRuntime().maxMemory() / Math.pow(10, 6)));

	// Returns totalMemory, freeMemory and maxMemory - separated by slash
	returnString = totalMemory.toString() + "/" + freeMemory.toString() + "/" + maxMemory.toString();

	return returnString;
    }

    /**
     * Works out World Commands
     * 
     * @param commandOrdinal
     * @param worldName
     * @return
     */
    private String workWorldCommand(int commandOrdinal, String worldName) {
	try {
	    String outputString = "ERROR";

	    if (commandOrdinal != Integer.MIN_VALUE) {
		if (isValidWorldName(worldName)) {
		    switch (commandOrdinal) {
		    // ONLINEPLAYER - count of Players Online
		    case 0:
			if (configuration.getConfig("npc-save-mode").equals("true")) {
			    outputString = Integer.toString(getOnlyRealPlayerCount(Bukkit.getServer().getWorld(worldName).getPlayers()));
			} else {
			    outputString = Integer.toString(Bukkit.getServer().getWorld(worldName).getPlayers().size());
			}
			break;
		    // PLAYERLIST - List of Players Online
		    case 1:
			outputString = returnPlayerNames(Bukkit.getServer().getWorld(worldName).getPlayers());
			break;
		    // TEMP
		    case 2:
			outputString = Double.toString(Bukkit.getServer().getWorld(worldName).getSpawnLocation().getBlock().getTemperature());
			break;
		    // TIME
		    case 3:
			outputString = Long.toString(Bukkit.getServer().getWorld(worldName).getTime());
			break;
		    // TEMPC
		    case 4:
			outputString = Integer.toString(getCelsiusFromDoubleTemperature(Bukkit.getServer().getWorld(worldName).getSpawnLocation().getBlock().getTemperature()));
			break;
		    // HUMIDITY
		    case 5:
			outputString = Double.toString(Bukkit.getServer().getWorld(worldName).getSpawnLocation().getBlock().getHumidity());
			break;
		    // RETURN IF NOTHING FIT
		    default:
			outputString = "Not Configured";
			break;
		    }
		}
	    }

	    return outputString;

	} catch (Exception e) {
	    log.info("workWorldCommand has encountered a Problem");
	    return "ERROR";
	}
    }

    /**
     * Checks for Players with the same HostName as the Server
     * 
     * @param playerList
     * @return
     */
    private int getOnlyRealPlayerCount(List<Player> playerList) {
	try {
	    int realPlayers = 0;

	    for (Player player : playerList) {
		String localHostName = InetAddress.getLocalHost().getHostName();
		String playerHostName = player.getAddress().getHostName();

		log.info(localHostName + " not " + playerHostName);

		if (!localHostName.equals(playerHostName)) {
		    realPlayers++;
		}
	    }

	    return realPlayers;
	} catch (Exception e) {
	    log.info("getOnlyRealPlayerCount has a Problem");
	    return 0;
	}
    }

    /**
     * Checks if given String is a valid Name for a available World
     * 
     * @param worldName
     * @return
     */
    private Boolean isValidWorldName(String worldName) {
	try {
	    List<World> availableWorlds = Bukkit.getServer().getWorlds();

	    for (World wrld : availableWorlds) {
		if (wrld.getName().equals(worldName)) {
		    return true;
		}
	    }
	    return false;

	} catch (Exception e) {
	    log.info("We tried to test if you send a valid Worldname but something happened");
	    return false;
	}
    }

    /**
     * Returns fancy formated Playernames
     * 
     * @param playerList
     * @return
     */
    private String returnPlayerNames(List<Player> playerList) {
	try {
	    String returnString = "";

	    if (playerList.size() > 0) {
		for (Player pl : playerList) {
		    log.info(pl.getName());
		    returnString += pl.getName() + " ";
		}
	    } else {
		returnString = "";
	    }

	    return returnString;
	} catch (Exception e) {
	    log.info("returnPlayerNames chrashed");
	    return "";
	}
    }

    private int getCelsiusFromDoubleTemperature(double temperature) {
	int maxCelsius = 60;
	int minCelsius = -16;
	int amountOfSteps = (maxCelsius - minCelsius);

	int[] celsiusSkala = new int[amountOfSteps];
	double multiplikator = (1 / ((double) amountOfSteps + 1));

	int result = 0;

	for (int iteration = 0; iteration < amountOfSteps; iteration++) {
	    celsiusSkala[iteration] = minCelsius + iteration;
	}

	result = celsiusSkala[(int) Math.floor(temperature / multiplikator)];

	return result;

    }
}
