package com.tracker.db;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class EventMessage extends BaseEntity {
	public Date timestamp; // time at which event-message is actualized
	public Date timeRecorded;
	
	public String type;   
	@ManyToOne
	public TrackingUser sender;
	@ManyToOne
	public TrackingUser receiver;
	
	@ManyToOne
	public TravelOrder travelOrder;
	
	public Boolean sent;    // true if message is of type that is communicated

	public String title;
	
	@ManyToOne
	public MessageBody body;
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public TrackingUser getSender() {
		return sender;
	}
	public void setSender(TrackingUser sender) {
		this.sender = sender;
	}
	public TrackingUser getReceiver() {
		return receiver;
	}
	public void setReceiver(TrackingUser receiver) {
		this.receiver = receiver;
	}
	public Boolean getSent() {
		return sent;
	}
	public void setSent(Boolean sent) {
		this.sent = sent;
	}
	public MessageBody getBody() {
		return body;
	}
	public void setBody(MessageBody body) {
		this.body = body;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public TravelOrder getTravelOrder() {
		return travelOrder;
	}
	public void setTravelOrder(TravelOrder travelOrder) {
		this.travelOrder = travelOrder;
	}
	public Date getTimeRecorded() {
		return timeRecorded;
	}
	public void setTimeRecorded(Date timeRecorded) {
		this.timeRecorded = timeRecorded;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}

/* Possible message types:
 * NOTIFICATION - notification message 
 * START - start/resume tracking
 * STOP - stop/pause tracking
 */ 
