package com.tracker.apientities;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class APIBaseResponse {
	
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(APIBaseResponse.class);	
	
	public String status = "OK";
	public String error_message = "";

	public APIBaseResponse() {
	}
	
	public APIBaseResponse(String status, String errorMessage) {
		this.status = status;
		this.error_message = errorMessage;
	}

	public static ResponseEntity<APIBaseResponse> createResponse(Exception exc) {
        if (exc instanceof APIException) {
            APIException apiExc = (APIException) exc;
            return new ResponseEntity<APIBaseResponse>(apiExc.createResponseBody(), apiExc.getHttpStatus());
        } else {  // Unknown exception
            APIBaseResponse body = new APIBaseResponse("ERROR", exc.getMessage());
            return new ResponseEntity<APIBaseResponse>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

	public static void logError(Exception exc) {
        if (!(exc instanceof APIException))
            logger.error("Error in API service handler", exc);
	}
}
