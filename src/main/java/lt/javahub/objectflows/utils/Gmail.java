package lt.javahub.objectflows.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.Base64Utils;

public class Gmail {

	private String imapUrl;
	private String login;
	private String pwd;
	
	private Store  imapStore;
	private Session smtpSession;
	private Transport smtpTransport;
	
	private Map<String, Folder> folders = new HashMap<String, Folder>();


	private static Logger log = LogManager.getLogger(Gmail.class);
	
	public Gmail(){
	}
	
	public void send(String to, String subject, String content) throws MessagingException {
		MimeMessage msg = new MimeMessage(getSMTPSession());
		msg.addRecipient(RecipientType.TO, new InternetAddress(to));
		msg.setSubject(subject, "UTF-8");
		msg.setText(content, "UTF-8");
		//msg.setContent(content, "text/plain");
		
		Transport transport = getTransport();
		transport.sendMessage(msg, msg.getAllRecipients());		
		
		log.debug("sent message to "+to);
	}
	
	public Folder openFolder(String folderName) throws MessagingException {
		if (folders.containsKey(folderName))
			return folders.get(folderName);
		
		Folder folder = getIMAPStore().getFolder(folderName);
		folder.open(Folder.READ_WRITE);
		folders.put(folderName, folder);
		
		return folder;
	}
	
	public void moveMessage(Message email, String newFolder){		
		try{
			Folder fromFolder = email.getFolder();
			Folder toFolder = openFolder(newFolder);
			
			fromFolder.copyMessages(new Message[]{email}, toFolder);
			if (!email.isExpunged())
				email.setFlag(Flags.Flag.DELETED, true);
			
		}catch(MessagingException me){
			log.error("error when moving messages between folders", me);
			throw new RuntimeException(me);
		}
	}
	
	public String getImapUrl() {
		return imapUrl;
	}

	public Gmail setImapUrl(String imapUrl) {
		this.imapUrl = imapUrl;
		return this;
	}

	public String getLogin() {
		return login;
	}

	public Gmail setLogin(String login) {
		this.login = login;
		return this;
	}

	public String getPwd() {
		return pwd;
	}

	public Gmail setPwd(String pwd) {
		this.pwd = pwd;
		return this;
	}
	
	
	///////////////////////////////////////
	// private impl down here
	
	private Store getIMAPStore() throws MessagingException {
		if (imapStore == null)
			imapStore = openIMAPStore();
		
		return imapStore;
	}
	
	private Session getSMTPSession(){
		if (smtpSession == null)
			smtpSession = openSMTPSession();
		
		return smtpSession;
	}
	
	private Store openIMAPStore() throws MessagingException {
		Properties imapProps = System.getProperties();
        imapProps.setProperty("mail.store.protocol", "imaps");
        
		Session imapSession = Session.getDefaultInstance(imapProps, null);
		Store result = imapSession.getStore("imaps");
		String password = new String(Base64Utils.decodeFromString(pwd));
		result.connect(imapUrl, login, password);
		
		log.debug("IMAP session opened");
		return result;
	}
		
	private Session openSMTPSession(){
		Properties smtpProps = new Properties();
		smtpProps.put("mail.smtp.auth", "true");
		smtpProps.put("mail.smtp.starttls.enable", "true");
		smtpProps.put("mail.smtp.host", "smtp.gmail.com");
		smtpProps.put("mail.smtp.port", "587");
 
		Session result = Session.getInstance(smtpProps,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				String password = new String(Base64Utils.decodeFromString(pwd));
				return new PasswordAuthentication(login, password);
			}
		  });
		
		log.debug("SMTP session ready");
		
		return result;
	}
	
	private Transport getTransport() throws MessagingException  {
		if (smtpTransport == null){
			smtpTransport = getSMTPSession().getTransport("smtp");
			smtpTransport.connect();
			log.debug("SMTP transport ready");
		}
		return smtpTransport;
	}

}
