package lt.javahub.objectflows.springsample1;

import org.springframework.stereotype.Service;

@Service
public class SampleConversationClient {

	public void onStarted(String recipientEmail) {
		System.out.println("Starting a conversation with " + recipientEmail);
	}

	public void onSuccess(String recipientEmail) {
		System.out.println("Success! Done talking to " + recipientEmail);
	}

	public void onTimeout(String recipientEmail) {
		System.out.println("Timeouted talking to " + recipientEmail);
	}

	public void onFailure(String recipientEmail) {
		System.out.println("Failed talking to " + recipientEmail);
	}

}
