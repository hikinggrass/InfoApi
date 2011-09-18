package seta.infoapi;

public enum ControlCharacter {
    CHAIN ("+"),
    WORLD ("/"),
    END ("?"),
    HTTP_NEWLINE("\r\n");
    
    private final String commandChar;
    ControlCharacter(String cmdChar) {
	this.commandChar = cmdChar;
    }
    
    public String getCommandChar() {
	return this.commandChar;
    }
}
