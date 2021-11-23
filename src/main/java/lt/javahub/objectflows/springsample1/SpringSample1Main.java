package lt.javahub.objectflows.springsample1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import lt.javahub.objectflows.utils.CLIUserInput;

/***
 * implements command line runner infrastructure for running SpringSample1
 * @author mantas.urbonas
 *
 */

@SpringBootApplication
@EnableAsync
public class SpringSample1Main implements CommandLineRunner{

	@Autowired
	private SampleConversationService sampleConversationService;
	
	/***
	 * entry point
	 */
	public static void main(String s[]) {
		SpringApplication
			.run(SpringSample1Main.class, s)
			.close();
	}
	
	@Override
	public void run(String... args) throws Exception {	
		sampleConversationService.continueConversations();
		
		CLIUserInput userInput = new CLIUserInput();
		
		if (userInput.askBoolean("Do you want to start a new flow?") == false)
			return;
		
		String recipientEmail = userInput.askString("Specify email of recipient");
		if (recipientEmail == null || recipientEmail.trim().isEmpty())
			return;
		
		sampleConversationService.startConversationWith(recipientEmail);
	}
}
