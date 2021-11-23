package lt.javahub.objectflows.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import lt.visma.javahub.flows.Context;
import lt.visma.javahub.flows.Flow;
import lt.visma.javahub.flows.engine.IFlowsRepository;
import lt.visma.javahub.flows.impl.SequentialFlow;
import lt.visma.javahub.flows.json.JsonSerializer;

/***
 * naive implementation of a file-based repository for the flows
 * @author mantas.urbonas
 *
 */
@Repository
public class FileRepo implements IFlowsRepository{

	private static final String FLOW_FILE_PREFIX = "flow_";
	private static final String CONTEXT_FILE_PREFIX = "context_";
	
	private static final String FLOW_FILE_EXTENSION = ".json";
	private static final String CONTEXT_FILE_EXTENSION = FLOW_FILE_EXTENSION;
	
	private String flowFilePrefix;
	private String contextFilePrefix;

	public FileRepo() {
		this("default-repo");
	}
	
	public FileRepo(String repoPrefix) {
		this.flowFilePrefix    = repoPrefix+"_"+FLOW_FILE_PREFIX;
		this.contextFilePrefix = repoPrefix+"_"+CONTEXT_FILE_PREFIX;
	}
	
	@Value("${tempdir}")
	private String FOLDER = "c:/deleteme/";
	
	@Override
	public Set<String> listAll() {
		File folder = new File(FOLDER);
		File[] files = folder.listFiles(
								pathname -> pathname.isFile() 
										 && pathname.getName().endsWith(FLOW_FILE_EXTENSION)
										 && pathname.getName().startsWith(flowFilePrefix));
		
		return Arrays.asList(files).stream()
					.map(file -> parseCorrelationId(file))
					.filter(corrId -> corrId != null)
					.collect(Collectors.toSet());
	}
	
	@Override
	public String persist(Flow flow) {
		return persist(flow, flow.getContext());
	}

	public String persist(Flow flow, Context context) {	
		String contextJson = JsonSerializer.serialize(context);

		flow = getRootFlow(flow);
		
		String correlationId = context.getCorrelationID();
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(contextFilePath(correlationId)))){
			writer.write(contextJson);
		}catch(IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
		
		String flowJson = JsonSerializer.serialize(flow);
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(flowFilePath(correlationId)))){
			writer.write(flowJson);
		}catch(IOException ioe) {
			throw new RuntimeException(ioe.getMessage());
		}
		
		return correlationId;
	}

	@Override
	public Flow restore(String correlationId) {
		Context context = loadFromFile(contextFilePath(correlationId), Context.class);
		SequentialFlow flow = loadFromFile(flowFilePath(correlationId), SequentialFlow.class);
		
		if (flow != null)
			flow.setContext(context);
		
		return flow;
	}
	
	@Override
	public void delete(String correlationId) {
		new File(contextFilePath(correlationId)).delete();
		new File(flowFilePath(correlationId)).delete();
	}
	
	private String parseCorrelationId(File file) {
		String fname = file.getName();
		if (!fname.startsWith(flowFilePrefix))
			return null;
		
		return fname.substring(flowFilePrefix.length(), fname.length() - FLOW_FILE_EXTENSION.length());
	}
	
	private <T> T loadFromFile(String file, Class<T> clazz){
		Path path = Paths.get(file);
		if (!Files.exists(path))
			return null;
		
		try {
			byte[] allBytes = Files.readAllBytes(path);
			String json = new String(allBytes, StandardCharsets.UTF_8);
			
			return JsonSerializer.deserialize(json, clazz);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String flowFilePath(String correlationId) {
		return FOLDER + flowFilePrefix + correlationId + FLOW_FILE_EXTENSION;
	}

	private String contextFilePath(String correlationId) {
		return FOLDER + contextFilePrefix + correlationId + CONTEXT_FILE_EXTENSION;
	}
	
	private static Flow getRootFlow(Flow flow) {
		if (flow == null)
			return null;
		
		Flow result = flow;
		while (result.getParent() !=null)
			result = result.getParent();
		
		return result;
	}
}
