package org.commacq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.commacq.DataManager.UpdateCsvCacheResult;

public class UpdateInboundHandler implements MessageListener {

	private static Logger logger = LoggerFactory.getLogger(UpdateInboundHandler.class);
	
	private final DataManager dataManager;
	
	public UpdateInboundHandler(DataManager dataManager) {
		this.dataManager = dataManager;
	}
	
	@Override
	public void onMessage(Message message) {
		logger.debug("Received update message: {}", message);
		TextMessage textMessage = (TextMessage)message;
		String entityName;
		String body;
		try {
			entityName = message.getStringProperty(MessageFields.entityId);
			body = textMessage.getText();
		} catch (JMSException ex) {
			logger.error("Could not perform update", ex);
			return;
		}
		
		logger.info("Received an update for entity {}", entityName);
		
		List<String> ids = new ArrayList<>();
		
		BufferedReader bodyIn = new BufferedReader(new StringReader(body));
		try {
			@SuppressWarnings("unused") String header = bodyIn.readLine(); //expect this to be just id
			String line;
			while((line = bodyIn.readLine()) != null) {
				ids.add(line);
			}
		} catch (IOException e) {
			logger.error("Could not parse information from update text: {}", body);
			return;
		}
		
		UpdateCsvCacheResult result = dataManager.updateCsvCache(entityName, ids);
		
		//TODO Provide a hook in so that if users really want to read off the csv attibutes from the update manager
		//and stop the database manager going back to the database for the details then they can. Ill advised.
		//It may be better advised, though, to use one or two attributes on the update message to decide whether
		//or not a continuous query needs to hit the database.
		
	}
	
}
