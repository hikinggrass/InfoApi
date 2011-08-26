package seta.infoapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

class Server extends Thread {

    protected boolean threadShouldStop = false;
    Logger log = Logger.getLogger("Minecraft");
    Socket socket;
    ServerSocket serverSocket;

    PrintWriter out;
    Config configuration;

    public Server(Config cfg) {
	configuration = cfg;
    }

    public void run() {
	while (!this.isClosing()) {
	    try {
		// Cast to String
		Integer serverPort = Integer.valueOf(configuration.getConfig("port")).intValue();
		String outputString, checkString;
		ServerSocket serverSocket = null;
		Socket clientSocket = null;
		BufferedReader input;
		PrintWriter output;

		try {
		    serverSocket = new ServerSocket(serverPort);
		} catch (IOException e) {
		    log.info("InfoApi couldn't listen to given Port: " + Integer.toString(serverPort));
		}

		try {
		    clientSocket = serverSocket.accept();
		} catch (IOException e) {
		    log.info("InfoApi couldn't accept on: " + Integer.toString(serverPort));
		}

		input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		output = new PrintWriter(clientSocket.getOutputStream());

		checkString = input.readLine();
		if (isValidCommandString(checkString)) {
		    outputString = processCommand(checkString);
		    outputString = addHTTPHeader(outputString);

		    output.println(outputString);
		    output.flush();
		}
		input.close();
		output.close();
		clientSocket.close();
		serverSocket.close();

	    } catch (Exception e) {
		log.info("InfoApi had some Problems while running");
		this.close();
	    }
	}
    }

    private boolean isValidCommandString(String getString) {
	String secretKey = configuration.getConfig("secret").toString();

	if (!getString.isEmpty()) {
	    if (getString.contains("?" + secretKey)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Process Commands
     * 
     * @param getString
     * @return
     */
    private String processCommand(String getString) {
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

		    if (commandOrdinal != Integer.MIN_VALUE) {
			String worldName = wString.substring(wString.indexOf("/") + 1);

			if (isValidWorldName(worldName)) {
			    switch (commandOrdinal) {
			    // ONLINEPLAYER
			    case 0:
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
			    // RETURN IF NOTHING FIT
			    default:
				outputString = "Not Configured";
				break;
			    }
			}

		    }
		}

	    } else {
		if (GeneralCommands.isPart(wString)) {
		    commandOrdinal = GeneralCommands.getOrdinal(wString);

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
		}
	    }
	}

	return outputString;
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

    private String addHTTPHeader(String resultString) {
	String finishedString = "";
	String byteLengthOfFinishedString = "";

	// Newline Character as specified in RFC 2616
	String newLine = "\r\n";

	// Check if there was an Error
	if (!resultString.contains("ERROR")) {
	    try {
		byteLengthOfFinishedString = Integer.toString(resultString.getBytes("UTF8").length);
	    } catch (UnsupportedEncodingException e) {
		log.info("InfoApi had some Problems while getting Bytesize of String");
	    }

	    finishedString += "HTTP/1.1 200 OK" + newLine;
	    finishedString += "Content-Language:en" + newLine;
	    finishedString += "Content-Length:" + byteLengthOfFinishedString + newLine;
	    finishedString += "Content-Type:text/html; charset=utf-8" + newLine;
	    finishedString += newLine;
	    finishedString += resultString;
	} else {
	    finishedString += "HTTP/1.1 500 Internal Server Error" + newLine;
	    finishedString += "Content-Language:en" + newLine;
	    // Well - we don't return Anything?
	    finishedString += "Content-Length:0" + newLine;
	    finishedString += "Content-Type:text/html; charset=utf-8" + newLine;
	    finishedString += newLine;
	}

	return finishedString;
    }

    private String getRuntimeMemoryInformationAsString() {
	String returnString = "";

	// Total Memory of Java Runtime in MB
	Double totalMemory = (Runtime.getRuntime().totalMemory() / Math.pow(10, 6));

	// Free Memory of Java Runtime in MB
	Double freeMemory = (Runtime.getRuntime().freeMemory() / Math.pow(10, 6));

	// Maximum Memory of Java Runtime in MB
	Double maxMemory = (Runtime.getRuntime().maxMemory() / Math.pow(10, 6));

	// Returns totalMemory, freeMemory and maxMemory - separated by slash
	returnString = totalMemory.toString() + "/" + freeMemory.toString() + "/" + maxMemory.toString();

	return returnString;
    }

    /**
     * Stops the Plugin softly
     */
    public synchronized void close() {
	this.threadShouldStop = true;

	try {
	    socket.close();
	    serverSocket.close();
	} catch (Exception e) {
	    log.info("InfoApi had a Problem while closing");
	}
    }

    protected synchronized boolean isClosing() {
	return this.threadShouldStop;
    }
}