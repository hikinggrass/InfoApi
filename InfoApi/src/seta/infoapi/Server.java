package seta.infoapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

class Server extends Thread {

    protected boolean threadShouldStop = false;
    Logger log = Logger.getLogger("Minecraft");
    Socket socket;
    ServerSocket serverSocket;

    PrintWriter out;
    Config configuration;

    CommandWorker comWorker;

    public Server(Config cfg) {
	configuration = cfg;
	comWorker = new CommandWorker(cfg);
    }

    public void run() {
	while (!this.isClosing()) {
	    try {

		kickstartSocket();

	    } catch (Exception e) {
		log.info("InfoApi had some Problems while running");
		this.close();
	    }
	}
    }

    private void kickstartSocket() {
	try {

	    // Cast from String to Integer
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
	    if (comWorker.isValidCommandString(checkString)) {
		outputString = comWorker.processCommand(checkString);
		outputString = HTTPWorker.addHTTPHeader(outputString);

		output.println(outputString);
		output.flush();
	    }
	    input.close();
	    output.close();
	    clientSocket.close();
	    serverSocket.close();

	} catch (Exception e) {
	    log.info("kickstartSocket " + e.getMessage());
	}
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