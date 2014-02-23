package org.commacq;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;

public class CommaCqServer {
	
	private final GenericXmlApplicationContext context;

	/**
	 * The main method accepts a list of spring resource URLs pointing to .xml
	 * files for spring configuration and .properties files.
	 * 
	 * All of these resources are used to create a spring context.
	 * 
	 * @param args
	 */
    public static void main(String[] args) {
        
	    if(args.length == 0) {
			System.err.println("Error: Specify the spring configuration .xml files and .properties " +
					           "files as resource strings - starting with file: or classpath: or http:");
			System.exit(-1);
		}
		
		new CommaCqServer(Arrays.asList(args));
    }
	
	public CommaCqServer(List<String> configurationXmlAndProperties) {
	    context = new GenericXmlApplicationContext();
	    ConfigurableEnvironment environment = context.getEnvironment();
	    MutablePropertySources propertySources = environment.getPropertySources();
	    List<String> xmlConfiguration = new ArrayList<String>();
	    
	    for(String entry : configurationXmlAndProperties) {
	        if(entry.endsWith(".properties")) {
	            try {
                    propertySources.addLast(new ResourcePropertySource(entry));
                } catch(IOException ex) {
                    throw new IllegalArgumentException("IO error when accessing resource: " + entry, ex); 
                }
	        } else if(entry.endsWith(".xml")) {
	            xmlConfiguration.add(entry);
	        } else {
	            throw new IllegalArgumentException("Resources must be .xml or .properties not: " + entry);
	        }
	    }
	    
	    context.load(xmlConfiguration.toArray(new String[xmlConfiguration.size()]));
	    context.refresh();
	}
	
	public void stop() {
		context.stop();
	}
	
}