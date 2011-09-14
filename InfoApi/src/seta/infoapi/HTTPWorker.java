package seta.infoapi;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

public class HTTPWorker {

    static Logger log = Logger.getLogger("Minecraft");

    public static String addHTTPHeader(String resultString) {
	String finishedString = "";
	String byteLengthOfFinishedString = "";

	// Newline Character as specified in RFC 2616
	String newLine = "\r\n";

	// Check if there was an Error
	if (!resultString.contains("ERROR")) {
	    try {
		byteLengthOfFinishedString = Integer.toString(resultString.getBytes("UTF8").length);
	    } catch (UnsupportedEncodingException e) {
		log.info("InfoApi had some Problems while getting Bytesize of String");
	    }

	    finishedString += "HTTP/1.1 200 OK" + newLine;
	    finishedString += "Content-Language:en" + newLine;
	    finishedString += "Content-Length:" + byteLengthOfFinishedString + newLine;
	    finishedString += "Content-Type:text/html; charset=utf-8" + newLine;
	    finishedString += newLine;
	    finishedString += resultString;
	} else {
	    finishedString += "HTTP/1.1 500 Internal Server Error" + newLine;
	    finishedString += "Content-Language:en" + newLine;
	    // Well - we don't return Anything?
	    finishedString += "Content-Length:0" + newLine;
	    finishedString += "Content-Type:text/html; charset=utf-8" + newLine;
	    finishedString += newLine;
	}

	return finishedString;
    }
}
