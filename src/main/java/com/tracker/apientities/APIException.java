package com.tracker.apientities;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class APIException extends RuntimeException
{
    private static final String ERROR = "ERROR";
    
	private HttpStatus httpStatus;
    private String jsonStatus;  // status return in json "status" field
    
    public APIException(String jsonStatus) {
    	this(jsonStatus, HttpStatus.OK, jsonStatus);
    }
    
    public APIException(String jsonStatus, HttpStatus httpStatus) {
    	this(jsonStatus, httpStatus, jsonStatus);
    }
    
    public APIException(HttpStatus httpStatus, String format, Object... args) {
        this(ERROR, httpStatus, format, args);
    }

    public APIException(String jsonStatus, HttpStatus httpStatus, String format, Object... args) {
        super(args.length > 0 ? String.format(format, args) : format);
        this.httpStatus = httpStatus;
        this.jsonStatus = jsonStatus;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getJsonStatus() {
        return jsonStatus;
    }
    
    // Can be overriden in descendants to provide more detail
    public APIBaseResponse createResponseBody() {
        return new APIBaseResponse(jsonStatus, getMessage());
    }
}
