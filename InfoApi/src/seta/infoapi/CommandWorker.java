package seta.infoapi;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CommandWorker {

    private Config configuration;
    Logger log = Logger.getLogger("Minecraft");

    public CommandWorker(Config cfg) {
	configuration = cfg;
    }

    /**
     * Process Commands Decides what to do with Commands
     * 
     * @param getString
     * @return
     */
    public String processCommand(String getString) {
	try {
	    String outputString = "";
	    String wString;
	    String[] seperatedCommands;
	    Integer commandOrdinal;
	    String worldName;

	    if (isValidCommandString(getString)) {
		// Remove HTTP Request Header Parts
		wString = getString.substring((getString.lastIndexOf("GET /") + 5), (getString.lastIndexOf(" HTTP/")));

		// Remove Secret Key and leading Question Mark
		// it should be save now to use just the command
		wString = wString.substring(0, wString.lastIndexOf(ControlCharacter.END.getCommandChar()));

		seperatedCommands = splitIntoSeperateCommands(wString);

		if (seperatedCommands.length > 0) {
		    for (String singleCommand : seperatedCommands) {
			if (isWorldCommand(singleCommand)) {
			    worldName = extractWorldName(singleCommand);

			    commandOrdinal = WorldCommands.getOrdinal(extractCommand(singleCommand));
			    outputString += workWorldCommand(commandOrdinal, worldName) + "+";
			} else {
			    commandOrdinal = GeneralCommands.getOrdinal(singleCommand);
			    outputString += workGenericCommand(commandOrdinal) + ControlCharacter.CHAIN.getCommandChar();
			}
		    }

		    if (outputString.endsWith(ControlCharacter.CHAIN.getCommandChar())) {
			outputString = outputString.substring(0, outputString.length() - 1);
		    }
		}
	    } else {
		throw new Exception("No Valid Command");
	    }

	    return outputString;
	} catch (Exception e) {
	    log.info("processCommand " + e.getMessage());
	    return "ERROR";
	}
    }

    private String[] splitIntoSeperateCommands(String wString) {
	try {
	    String[] seperatedCommands;

	    if (wString.contains(ControlCharacter.CHAIN.getCommandChar())) {
		seperatedCommands = wString.split("\\" + ControlCharacter.CHAIN.getCommandChar());
	    } else {
		seperatedCommands = new String[1];
		seperatedCommands[0] = wString;
	    }

	    return seperatedCommands;
	} catch (Exception e) {
	    log.info("splitIntoSeperateCommands " + e.getMessage());
	    return null;
	}
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
		// VERSION_SHORT
		case 3:
		    outputString = Bukkit.getServer().getVersion();
		    outputString = outputString.substring(outputString.indexOf("(") + 1, outputString.lastIndexOf(")"));
		    break;
		// RAM
		case 4:
		    outputString = getRuntimeMemoryInformationAsString();
		    break;
		// CPU
		case 5:
		    outputString = "Not Possible due of JAVA Limitation";
		    break;
		 // PLUGINS
		case 6:
		    outputString = returnPluginNames(Bukkit.getServer().getPluginManager().getPlugins());
		    break;
		 // PLUGINS_SHORT
		case 7:
		    outputString = returnPluginShortNames(Bukkit.getServer().getPluginManager().getPlugins());
		    break;
		// RETURN IF NOTHING FIT
		default:
		    outputString = "Not Configured";
		    break;
		}
	    }

	    return outputString;
	} catch (Exception e) {
	    log.info("workGenericCommand " + e.getMessage());

	    return "ERROR";
	}
    }

    private Boolean isWorldCommand(String wString) {
	try {
	    Boolean isWorldCommand = false;

	    if (wString.contains(ControlCharacter.WORLD.getCommandChar())) {
		isWorldCommand = true;
	    }

	    return isWorldCommand;
	} catch (Exception e) {
	    log.info("isWorldCommand " + e.getMessage());
	    return false;
	}
    }

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
	    log.info("workWorldCommand " + e.getMessage());
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
	    log.info("getOnlyRealPlayerCount " + e.getMessage());
	    return 0;
	}
    }

    private String extractWorldName(String wString) {
	try {
	    String worldName = "";

	    worldName = wString.substring(wString.lastIndexOf(ControlCharacter.WORLD.getCommandChar()) + 1);

	    return worldName;
	} catch (Exception e) {
	    log.info("extractWorldName " + e.getMessage());
	    return "";
	}
    }

    private String extractCommand(String wString) {
	try {
	    String commandString;

	    commandString = wString.substring(0, wString.lastIndexOf(ControlCharacter.WORLD.getCommandChar()));

	    return commandString;
	} catch (Exception e) {
	    log.info("extractCommand " + e.getMessage());
	    return "";
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
	    log.info("isValidWorldName " + e.getMessage());
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
	    log.info("returnPlayerNames " + e.getMessage());
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
    
    private String returnPluginShortNames(Plugin[] plugins) {
	try {
	    String returnString = "";

	    if (plugins.length > 0) {
		for (Plugin plugin : plugins) {
		    returnString += plugin.getDescription().getName() + "\r\n";
		}
	    } else {
		returnString = "";
	    }

	    return returnString;
	} catch (Exception e) {
	    log.info("returnPluginShortNames " + e.getMessage());
	    return "";
	}
    }
    
    private String returnPluginNames(Plugin[] plugins) {
	try {
	    String returnString = "";

	    if (plugins.length > 0) {
		for (Plugin plugin : plugins) {
		    returnString += plugin.getDescription().getFullName() + "\r\n";
		}
	    } else {
		returnString = "";
	    }

	    return returnString;
	} catch (Exception e) {
	    log.info("returnPluginNames " + e.getMessage());
	    return "";
	}
    }
}
