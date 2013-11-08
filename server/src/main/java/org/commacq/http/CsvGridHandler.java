package org.commacq.http;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Serves up the html file that then connects to the asyncronous web socket
 * interface from the user's browser.
 */
@Slf4j
public class CsvGridHandler extends AbstractHandler {

	private CsvDataSourceLayer layer;
	private final Template gridViewTemplate;

	public CsvGridHandler(CsvDataSourceLayer layer) {
	    Configuration cfg = new Configuration();
	    cfg.setClassForTemplateLoading(getClass(), "/");
	    try {
			this.gridViewTemplate = cfg.getTemplate("/grid-view/websocket.ftl");
		} catch (IOException ex) {
			throw new RuntimeException("Could not load grid view template", ex);
		}
		this.layer = layer;
	}
	
	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		
		log.info("Received request for: {}", target);
		
  		String entityId = HttpUtils.getEntityStringFromTarget(target);
		
  		CsvDataSource source = layer.getCsvDataSource(entityId);
		
		if(source == null) {
			HttpUtils.respondWithErrorMessage(layer, entityId, response, log);
			
			return;
		}
		
		//TODO remove - development only
	    Configuration cfg = new Configuration();
	    cfg.setClassForTemplateLoading(getClass(), "/");
		Template gridViewTemplate = cfg.getTemplate("/grid-view/websocket.ftl");
		
		try {
			gridViewTemplate.process(Collections.singletonMap("entityId", entityId), response.getWriter());
		} catch (TemplateException ex) {
			throw new ServletException("Could not create output html", ex);
		}
	}
	
}
