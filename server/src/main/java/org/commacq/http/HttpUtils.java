package org.commacq.http;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.commacq.layer.Layer;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;

class HttpUtils {
	
	/**
	 * Returns a string that we hope will represent an entity
	 * or null if the URL could not be parsed. 
	 * @param target
	 * @return
	 */
	static String getEntityStringFromTarget(String target) {
		int finalSlash = target.lastIndexOf("/");
		if(finalSlash == -1 || finalSlash >= target.length() - 1) {
			return null;
		}
		
		return target.substring(finalSlash + 1);
	}
	
	static void respondWithErrorMessage(Layer layer, String entityId, HttpServletResponse response, Logger log) throws IOException {

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
	}
	
}
