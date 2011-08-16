package seta.infoapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

	static String name = "InfoApi";
	
	static String mainDirectory = "plugins/"+name;
	static File configs = new File(mainDirectory + File.separator + "config.cfg");
	static Properties config = new Properties();
	
	Config(){
		File mainDir = new File(mainDirectory);
		if(!mainDir.exists()){
			mainDir.mkdir();
		}
		
		if(!configs.exists()){ 
			try {
				configs.createNewFile(); 
				FileOutputStream out = new FileOutputStream(configs);
				config.put("secret", "secret");
				config.put("port", "25577");
				config.put("name", "&a[&bServer&a]&f");
				config.put("enablechatlog", "true");
				config.put("loglength", "25");
				config.put("opplayer", "kingseta");
				config.store(out, name+" Config file");
				out.flush(); 
				out.close();
			} catch (IOException ex) { 
				ex.printStackTrace();
			}
		}
		
		try {
			FileInputStream inStream = new FileInputStream(configs);
			config.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getConfig(String key){
		return config.getProperty(key);
	}
	
	public void setConfig(String string, String string2) {
		config.setProperty(string, string2);
		this.saveConfig();
	}
	
	private void saveConfig() {
		try {
			FileOutputStream out = new FileOutputStream(configs);
			config.store(out, name+" Config file");
			out.flush(); 
			out.close();
		} catch (IOException ex) { 
			ex.printStackTrace();
		}
	}
}
