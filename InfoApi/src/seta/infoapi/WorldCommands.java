package seta.infoapi;

public enum WorldCommands {
    ONLINEPLAYER,
    PLAYERLIST,
    TEMP,
    TIME,
    TEMPC,
    HUMIDITY;
    
    public static Boolean isPart(String command) {
	for(WorldCommands wc : WorldCommands.values()) {
	    if(wc.name().equals(command.toUpperCase())) {
		return true;
	    }
	}
	return false;
    }
    
    public static Integer getOrdinal(String command) {
	for(WorldCommands wc : WorldCommands.values()) {
	    if(wc.name().equals(command.toUpperCase())) {
		return wc.ordinal();
	    }
	}
	return Integer.MIN_VALUE;
    }
}