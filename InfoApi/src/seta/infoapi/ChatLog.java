package seta.infoapi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class ChatLog extends PlayerListener {

	static ArrayList<String> log = new ArrayList<String>();
	Config cfg;
	public final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	
	public ChatLog(Config cfa) {
		cfg = cfa;
	}

	public void onPlayerChat(PlayerChatEvent e){
		if(log.toArray().length >= Integer.parseInt(cfg.getConfig("loglength"))){
			log.remove(0);
		}
	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		log.add(e.getPlayer().getName()+"!"+sdf.format(cal.getTime())+" :"+e.getMessage());
	}
	
	public String list(){
		String out = "";
		for(String x : log){
			out += x+"\n";
		}
		return out+"";
	}
}
