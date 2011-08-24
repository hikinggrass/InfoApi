package seta.infoapi;

public enum GeneralCommands {
    MAXPLAYER,
    ONLINEMODE,
    VERSION,
    RAM,
    CPU;
    
    public static Boolean isPart(String command) {
	for(GeneralCommands gc : GeneralCommands.values()) {
	    if(gc.name().equals(command.toUpperCase())) {
		return true;
	    }
	}
	return false;
    }
    
    public static Integer getOrdinal(String command) {
	for(GeneralCommands gc : GeneralCommands.values()) {
	    if(gc.name().equals(command.toUpperCase())) {
		return gc.ordinal();
	    }
	}
	return Integer.MIN_VALUE;
    }
}
