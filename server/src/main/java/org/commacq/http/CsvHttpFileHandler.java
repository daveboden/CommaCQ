package org.commacq.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.commacq.CsvDataSource;
import org.commacq.CsvDataSourceLayer;
import org.commacq.CsvLineCallbackWriter;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

@Slf4j
public class CsvHttpFileHandler extends AbstractHandler {

	private CsvDataSourceLayer layer;

	public CsvHttpFileHandler(CsvDataSourceLayer layer) {
		this.layer = layer;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

		if(target.equals("/favicon.ico")) {
			//Ignore requests for the favicon.ico from the browser
			return;
		}
		
		log.info("Received request for: {}", target);
		
		final String entityId = HttpUtils.getEntityStringFromTarget(target);
		
		CsvDataSource source = layer.getCsvDataSource(entityId);
		
		if(source == null) {
			StringBuilder messageBuilder = new StringBuilder();
			for(String currentEntityId : layer.getEntityIds()) {
				messageBuilder.append("\n");
				messageBuilder.append(currentEntityId);
			}
			
			String message;
			if(entityId == null) {
				message = String.format("Valid types are: %s", messageBuilder.toString());
			} else {
				message = String.format("Unknown entity: %s - valid types are: %s",
						  entityId, messageBuilder.toString());

				//Warn. It's more serious that someone has requested an invalid entity than no entity.
				log.warn(message);
			}
			response.sendError(HttpStatus.NOT_FOUND_404,
					message);
			
			return;
		}
		
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "attachment; filename=" + entityId + ".csv");
		
		//response.setHeader("Cache-Control", "must-revalidate");
		//response.setHeader("Pragma", "must-revalidate");
		//response.setContentType("application/vnd.ms-excel");
		
		//TODO - Keep track of the total content length of the cache at any one time
		//so that we can indicate to the user how many percent of the way through
		//the file download they are.
		CsvLineCallbackWriter writer = new CsvLineCallbackWriter(response.getWriter());
		
		source.getAllCsvLines(writer);
		
		response.flushBuffer();
	}
}
