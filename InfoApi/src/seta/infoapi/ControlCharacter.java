package seta.infoapi;

public enum ControlCharacter {
    CHAIN ("+"),
    WORLD ("/"),
    END ("?");
    
    private final String commandChar;
    ControlCharacter(String cmdChar) {
	this.commandChar = cmdChar;
    }
    
    public String getCommandChar() {
	return this.commandChar;
    }
}
