package com.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import java.util.Collections;
import java.util.Map;

import com.serverless.dal.User;

public class CreateUserHandler implements RequestHandler<Map<String, Object>, ApiGatewayResponse> {

	private final Logger logger = Logger.getLogger(this.getClass());

	@Override
	public ApiGatewayResponse handleRequest(Map<String, Object> input, Context context) {

		try {
			// get the 'body' from input
			JsonNode body = new ObjectMapper().readTree((String) input.get("body"));

			// create the User object for post
			User user = new User();
			// user.setId(body.get("id").asText());
			user.setName(body.get("name").asText());
			user.setPrice((float) body.get("price").asDouble());
			user.save(user);

			// send the response back
			return ApiGatewayResponse.builder().setStatusCode(200).setObjectBody(user)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless"))
					.setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
					.setHeaders(Collections.singletonMap("Access-Control-Allow-Credentials", "true")).build();

		} catch (Exception ex) {
			logger.error("Error in saving user: " + ex);

			// send the error response back
			Response responseBody = new Response("Error in saving user: ", input);
			return ApiGatewayResponse.builder().setStatusCode(500).setObjectBody(responseBody)
					.setHeaders(Collections.singletonMap("X-Powered-By", "AWS Lambda & Serverless")).build();
		}
	}
}
