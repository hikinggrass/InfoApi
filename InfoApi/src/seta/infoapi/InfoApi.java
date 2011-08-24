package seta.infoapi;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class InfoApi extends JavaPlugin {
    Logger log = Logger.getLogger("Minecraft");
    Server server;
    // ChatLog chatLog;

    @Override
    public void onEnable() {

	Config configuration = new Config();
	server = new Server(configuration);
	
	server.start();
	
	if(!server.isAlive()) {
	    server.start();
	    log.info("InfoApi HTTP Listener was resurrected");
	}

	log.info("InfoAPI (Janka flavoured) sucessfully started.");
    }

    // @SuppressWarnings("deprecation")
    @Override
    public void onDisable() {
	// server.destroy();
	// server.interrupt();
	// server.kill();
	server.close();
	log.info("InfoAPI (Janka flavoured) sucessfully ended");
    }

}