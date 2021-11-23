package lt.javahub.objectflows.springsample1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lt.javahub.objectflows.utils.Gmail;

@Service
public class EmailService {

	private Gmail gmail;
	
	@Value("${email.url}")
	private String imapUrl;
	
	@Value("${email.login}")
	private String login;
	
	@Value("${email.pwd}")
	private String pwd;


	private static Logger log = LogManager.getLogger(EmailService.class);
	
	
	@PostConstruct
	public void postConstruct() {
		this.gmail = new Gmail()
							.setImapUrl(imapUrl)
							.setLogin(login)
							.setPwd(pwd);
	}
	
	public void sendEmail(String recipient, String subj, String content, String correlationID){
		try {
			log.info("about to send THE email");
			
			gmail.send(recipient, subj + " " + "CASE-ID:" + correlationID, content);
			
			log.info("just sent an email");
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public List<EmailDTO> fetchEmails() {
		try {
			Folder inbox = gmail.openFolder("INBOX");
			
			Message[] messages = inbox.getMessages();
			if (messages == null || messages.length<=0)
				return Collections.emptyList();
			
			return convert(messages);
			
		} catch (Exception e) {
			log.error(e);
			return Collections.emptyList();
		}
	}

	
	private List<EmailDTO> convert(Message[] messages) throws MessagingException, IOException {
		List<EmailDTO> ret= new ArrayList<>();
		
		for (Message message: messages) {
			String from = parseFrom(message.getFrom());
			String subject = parseSubject(message.getSubject());
			String correlationId = parseCorrelationId(message.getSubject());
			String content = parseContent(message);
			
			EmailDTO email = new EmailDTO()
								.setFrom(from)
								.setSentTime(message.getSentDate())
								.setSubject(subject)
								.setCorrelationId(correlationId)
								.setBody(content);
			
			ret.add(email);
		}
		
		return ret;
	}
	
	private String parseCorrelationId(String subject) {
		if (subject == null)
			return null;
		
		int pos = subject.lastIndexOf("CASE-ID:");
		if (pos < 0)
			return null;
		
		return subject.substring(pos + 8).trim();
	}

	private String parseSubject(String subject) {
		if (subject == null)
			return null;
		
		int pos = subject.lastIndexOf("CASE-ID:");
		if (pos < 0)
			return subject;
		
		return subject.substring(0, pos).trim();
	}

	private String parseFrom(Address[] from) {
		if (from == null || from.length<=0)
			return null;
		
		Set<String> ret = new HashSet<>();
		for (Address addr: from)
			ret.add(addr.toString());
	
		return String.join("; ", ret);
	}

	private static String parseContent(Message message) throws IOException, MessagingException {
    	
    	if (message instanceof MimeMessage == false){
    		log.error("ERROR: message not MimeMessage: "+message);
    		return null;    	
    	}
    	
    	Object contentObject = message.getContent();
        if (contentObject instanceof String)// a simple text message
        	return (String) contentObject;
        
        if(contentObject instanceof Multipart == false){
        	log.error("not a multipart email message, dont know how to parse!"+contentObject);
        	return null;
        }
        
    	Multipart multipart = (Multipart)contentObject;
		Map<String, Part> parts = new HashMap<String, Part>();
    	mapMimetypes(multipart, parts);
    	
    	if(parts.containsKey("text/plain"))
    		return parts.get("text/plain").getContent().toString();
    	
    	if (parts.containsKey("text/html"))
    		return parts.get("text/plain").getContent().toString();
    	
    	log.error("this multipart message did not contain text/plain or text/html!:"+message);
    	
    	return String.join(", ", parts.keySet());
	}

	private static void mapMimetypes(Multipart multipart, Map<String, Part> where) throws MessagingException, IOException {
		int count = multipart.getCount();
		
		for (int i = 0; i < count; i++)
			mapBodyPart(multipart.getBodyPart(i), where);
	}

	private static void mapBodyPart(BodyPart bodyPart, Map<String, Part> where) throws MessagingException, IOException {
		if (bodyPart.isMimeType("text/html")) {
			where.put("text/html", bodyPart);
			return;
		}
		
		if (bodyPart.isMimeType("text/plain")) {
			where.put("text/plain", bodyPart);
			return;
		}
		
		where.put(bodyPart.getContentType(), bodyPart);
		
		if (bodyPart.isMimeType("multipart/*"))
			mapMimetypes((Multipart) bodyPart.getContent(), where);
	}
	
}
