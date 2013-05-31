package org.commacq.http;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.commacq.CsvCache;
import org.commacq.DataManager;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Serves up the html file that then connects to the asyncronous web socket
 * interface from the user's browser.
 */
public class CsvGridHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(CsvGridHandler.class);

	private DataManager dataManager;
	private final Template gridViewTemplate;

	public CsvGridHandler(DataManager dataManager) {
	    Configuration cfg = new Configuration();
	    cfg.setClassForTemplateLoading(getClass(), "/");
	    try {
			this.gridViewTemplate = cfg.getTemplate("/grid-view/websocket.ftl");
		} catch (IOException ex) {
			throw new RuntimeException("Could not load grid view template", ex);
		}
		this.dataManager = dataManager;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		logger.info("Received request for: {}", target);
		
  		String entity = HttpUtils.getEntityStringFromTarget(target);
		
		CsvCache csvCache = dataManager.getCsvCache(entity);
		
		if(csvCache == null) {
			String message;
			if(entity == null) {
				message = String.format("Valid types are: %s", dataManager.getEntityIds());
			} else {
				message = String.format("Unknown entity: %s - valid types are: %s",
						  entity, dataManager.getEntityIds());

				//Warn. It's more serious that someone has requested an invalid entity than no entity.
				logger.warn(message);
			}
			response.sendError(HttpStatus.NOT_FOUND_404,
					message);
			
			return;
		}
		
		//TODO remove - development only
	    Configuration cfg = new Configuration();
	    cfg.setClassForTemplateLoading(getClass(), "/");
		Template gridViewTemplate = cfg.getTemplate("/grid-view/websocket.ftl");
		
		try {
			gridViewTemplate.process(Collections.singletonMap("entityId", entity), response.getWriter());
		} catch (TemplateException ex) {
			throw new ServletException("Could not create output html", ex);
		}
	}
	
}
