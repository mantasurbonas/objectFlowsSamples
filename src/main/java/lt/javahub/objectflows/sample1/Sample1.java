package lt.javahub.objectflows.sample1;

import static lt.visma.javahub.flows.Flows.first;
import static lt.visma.javahub.flows.Flows.print;
import static lt.visma.javahub.flows.Flows.printVar;
import static lt.visma.javahub.flows.Flows.println;
import static lt.visma.javahub.flows.Flows.waitFor;

import lt.javahub.objectflows.utils.CLIUserInput;
import lt.javahub.objectflows.utils.FileRepo;
import lt.javahub.objectflows.utils.Log4JLoggerImpl;
import lt.visma.javahub.flows.Flow;
import lt.visma.javahub.flows.engine.FlowsEngine;
import lt.visma.javahub.flows.engine.impl.SimplePersistStrategy;

/***
 * a primitive console app:
 * 
 * user either
 * A) user passes a custom event to some existing workflow
 * or 
 * B) starts a new flow:
 * 		which will 
 * 				wait for up to 1 minute for a 'HI' event 
 * 				or timeout waiting);
 * 
 * Usage:
 *  either 
 * 		"Do you want to start a new one?" -> Y
 *  	(creates a new workflow and prints its UUID)
 *  
 *  or
 *  	"Do you want to start a new one?" -> N
 *      "Did any of the events happen to any of the existing flows?" -> Y
 *      "What was the workflow ID?" -> (enter from a step A before)
 *      "What was the event ID?" -> HI
 *      "What was the event parameter?" -> whatever parameter here
 *      
 *      "Yes! The 'Hi' event happened, value is [whatever parameter here]"
 *      or
 *      "Nope, no 'Hi' event recevied in 60 seconds - timeouted!"
 *  
 * 
 * @author mantas.urbonas
 *
 */
public class Sample1 {

	private static Flow createNewFlow() {
		return first( println("hello from a freshly started flow!") )
					.then( println("will wait for a minute for a 'HI' event") )
					.then( waitFor()
							.onEvent("HI", first( print("Yes! The 'HI' event happened, value is [") )
											.then(printVar("HI"))
											.then(println("]"))
									)
							.onTimeout(1000 * 60, println("Nope, no 'HI' event received in 60 seconds - timeouted!")))
					.then( println("flow end"));
	}
	
	public static void main(String... args) throws Exception {	
		FileRepo fileRepo = new FileRepo("sample1-repo");
		System.out.println("Known workflows: "+fileRepo.listAll());
		
		FlowsEngine engine = new FlowsEngine()
									.setRepository(fileRepo)
									.setLogger(new Log4JLoggerImpl())
									.setPersistanceStrategy(new SimplePersistStrategy());
		
		CLIUserInput userInput = new CLIUserInput();
		
		if (userInput.askBoolean("Do you want to start a new one?") == true) {
			engine.startNewFlow( createNewFlow() );
			return;
		}
		
		if (userInput.askBoolean("Did any event happen to any of the existing flows?") == true) {
			engine.onEvent( userInput.askString("What was workflow ID?"), 
					        userInput.askString("What was event ID?"), 
					        userInput.askString("What was event parameter?") 
					      );
			
			return;
		}
		
		System.out.println("OK, just processing any of existing flows:");
		
		engine.onTimer();
		
		System.out.println("...done");
	}

}
