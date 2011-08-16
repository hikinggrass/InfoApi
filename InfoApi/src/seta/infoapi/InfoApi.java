package seta.infoapi;

import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class InfoApi extends JavaPlugin {
	Logger log = Logger.getLogger("Minecraft");
	Server s;
	ChatLog cl;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onDisable() {
		log.info("[*] InfoApi disabled.");
		s.destroy();
		s.interrupt();
		s.kill();
	}
	
	@Override
	public void onEnable() {
		
    	Config cfg = new Config();
    	if(cfg.getConfig("enablechatlog").equals("true")){
    		cl = new ChatLog(cfg);
            PluginManager pm = getServer().getPluginManager();
            pm.registerEvent(Event.Type.PLAYER_CHAT, cl, Priority.High, this);
    		s = new Server(cfg,cl);
    		s.start();
    	} else {
    		s = new Server(cfg);
    		s.start();
    	}

		log.info("[*] InfoApi enabled.");
	}

}