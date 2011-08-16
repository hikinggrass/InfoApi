package seta.infoapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class Server extends Thread {

	Socket skt;
	ServerSocket srvr;
	PrintWriter out;
	boolean doit = true;
	Config cfg;
	ChatLog cl;
	
	Server(Config asd){
		cfg = asd;
	}
	
	Server(Config asd, ChatLog ci){
		cfg = asd;
		cl = ci;
	}
	
    public static String colorize(String s){
        if(s == null) return null;
        return s.replaceAll("&([0-9a-f])", "\u00A7$1");
    }
	
	public void run() {
	  if(doit == true){
	      String data = "ERROR: Unknown request";
	      try {
	    	 srvr = new ServerSocket(Integer.parseInt(cfg.getConfig("port")));
	         skt = srvr.accept();
	         BufferedReader in = new BufferedReader(new InputStreamReader(skt.getInputStream()));
	         String inp = in.readLine().toString();
	         if(inp.contains("?"+cfg.getConfig("secret"))){
	        	 inp = inp.replace("?"+cfg.getConfig("secret")," ");
		         if(inp.contains("GET /onlineplayer/")){
		        	 if(inp.split("onlineplayer/")[1].split(" ")[0].equals("all")){
		        		 data = Bukkit.getServer().getOnlinePlayers().length+""; 
		        	 } else {
			        	 if(Bukkit.getServer().getWorlds().toString().contains(inp.split("onlineplayer/")[1].split(" ")[0])){
			        		 data = Bukkit.getServer().getWorld(inp.split("onlineplayer/")[1].split(" ")[0]+"").getPlayers().toArray().length+"";
			        	 } else {
			        		 data = "ERROR: Unknown World";
			        	 }
		        	 }
		         } else if(inp.contains("GET /onlineplayer")){
		        	 data = Bukkit.getServer().getOnlinePlayers().length+""; 
		         } else if(inp.contains("GET /maxplayer")){
		        	 data = Bukkit.getServer().getMaxPlayers()+""; 
		         } else if(inp.contains("GET /time/")){
		        	 if(Bukkit.getServer().getWorlds().toString().contains(inp.split("time/")[1].split(" ")[0])){
		        		 data = Bukkit.getServer().getWorld(inp.split("time/")[1].split(" ")[0]+"").getTime()+"";
		        	 } else {
		        		 data = "ERROR: Unknown World";
		        	 }
		         } else if(inp.contains("GET /temp/")){
		        	 if(Bukkit.getServer().getWorlds().toString().contains(inp.split("temp/")[1].split(" ")[0])){
		        		 data = Bukkit.getServer().getWorld(inp.split("temp/")[1].split(" ")[0]+"").getSpawnLocation().getBlock().getTemperature()+"";
		        	 } else {
		        		 data = "ERROR: Unknown World";
		        	 }
		         } else if(inp.contains("GET /playerlist/")){
		        	 if(Bukkit.getServer().getWorlds().toString().contains(inp.split("playerlist/")[1].split(" ")[0])){
			        	 data = "";
			        	 for (Player player : Bukkit.getServer().getWorld(inp.split("playerlist/")[1].split(" ")[0]).getPlayers()){
			        		 data += player.getName()+", ";
						 }
			        	 data += "none";
			        	 data = data.replace(", none", "");
		        	 } else {
		        		 data = "ERROR: Unknown World";
		        	 }
		         } else if(inp.contains("GET /playerlist")){
		        	 data = "";
		        	 for (Player player : Bukkit.getServer().getOnlinePlayers()){
		        		 data += player.getName()+", ";
					 }
		        	 data += "none";
		        	 data = data.replace(", none", "");
		         } else if(inp.contains("GET /onlinemode")){
		        	 data = Bukkit.getServer().getOnlineMode()+"";
		         } else if(inp.contains("GET /version")){
		        	 data = Bukkit.getServer().getVersion()+"";
		         } else if(inp.contains("GET /say/")){
		        	 Bukkit.getServer().broadcastMessage(colorize(cfg.getConfig("name"))+" "+colorize(inp.split("say/")[1].split(" ")[0].replace("_", " ")));
		        	 data = "ok";
		         } else if(inp.contains("GET /cmd/")){
		        	 CommandSender s = null;
		        	 Bukkit.getServer().dispatchCommand(s,inp.split("cmd/")[1].split(" ")[0].replace("_"," "));
		        	 data = "ok";
		         } else if(inp.contains("GET /chat")){
		        	 if(cfg.getConfig("enablechatlog").equals("true")){
		        		 data = cl.list();
		        	 } else {
		        		 data = "ERROR: Chatlog is disabled in config";
		        	 }
		         }
	         } else {
	        	 data = "ERROR: Unknown request or wrong secret";
	         }
	         out = new PrintWriter(skt.getOutputStream(), true);
	         out.print(data);
	         out.close();
	         skt.close();
	         srvr.close();
	         this.run();
	      }
	      catch(Exception e) {
	         System.out.print("Whoops! Api didn't work!\n");
	         out.close();
	         try {
				skt.close();
		        srvr.close();
		        this.run();
	         } catch (IOException e1) {
				e1.printStackTrace();
	         }
	      }
	  }
   }
   

	public void kill(){
		   doit = false;
	       try {
	    	   out.close();
	    	   skt.close();
		       srvr.close();
	       } catch (IOException e) {
				e.printStackTrace();
	       }
	}
}