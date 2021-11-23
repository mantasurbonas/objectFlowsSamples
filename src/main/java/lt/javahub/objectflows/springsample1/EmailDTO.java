package lt.javahub.objectflows.springsample1;

import java.sql.Timestamp;
import java.util.Date;

public class EmailDTO {
	private String from;
	private String to;
	private String subject;
	private String body;
	private String correlationId;
	private Timestamp sentTime;

	public String getFrom() {
		return from;
	}

	public EmailDTO setFrom(String from) {
		this.from = from;
		return this;
	}

	public String getTo() {
		return to;
	}

	public EmailDTO setTo(String to) {
		this.to = to;
		return this;
	}

	public String getSubject() {
		return subject;
	}

	public EmailDTO setSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public String getBody() {
		return body;
	}

	public EmailDTO setBody(String body) {
		this.body = body;
		return this;
	}

	public Timestamp getSentTime() {
		return sentTime;
	}

	public EmailDTO setSentTime(Timestamp sentTime) {
		this.sentTime = sentTime;
		return this;
	}

	public EmailDTO setSentTime(Date sentDate) {
		if (sentDate == null)
			return this;
		
		return setSentTime(new Timestamp(sentDate.getTime()));
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public EmailDTO setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
		return this;
	}


}
