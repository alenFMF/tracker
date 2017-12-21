package com.tracker.apientities.notifications;

public class APISendSmsRequest {

	public String commaSeparatedNumbers;
	public String content;
	public String franchiseUid;
	public String recipient;
	public String type;
	public String user;
	
	public APISendSmsRequest() {};
	public APISendSmsRequest(String commaSeparatedNumbers, String content, String franchiseUid, String recipient, String type) {
		this.commaSeparatedNumbers = commaSeparatedNumbers;
		this.content = content;
		this.franchiseUid = franchiseUid;
		this.recipient = recipient;
		this.type = type;
	};
}
