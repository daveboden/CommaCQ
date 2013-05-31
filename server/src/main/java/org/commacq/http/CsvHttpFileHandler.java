package org.commacq.http;

import java.io.IOException;

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

public class CsvHttpFileHandler extends AbstractHandler {

	private static final Logger logger = LoggerFactory.getLogger(CsvHttpFileHandler.class);

	private DataManager dataManager;

	public CsvHttpFileHandler(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		if(target.equals("/favicon.ico")) {
			//Ignore requests for the favicon.ico from the browser
			return;
		}
		
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
		
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "attachment; filename=" + entity + ".csv");
		
		//response.setHeader("Cache-Control", "must-revalidate");
		//response.setHeader("Pragma", "must-revalidate");
		//response.setContentType("application/vnd.ms-excel");
		
		//TODO - Keep track of the total content length of the cache at any one time
		//so that we can indicate to the user how many percent of the way through
		//the file download they are.
		csvCache.writeToOutput(response.getWriter());
		
		response.flushBuffer();
	}
}
