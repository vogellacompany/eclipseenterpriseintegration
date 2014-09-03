package com.example.e4.rcp.todo.restservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jsonp.JsonProcessingFeature;

import com.example.e4.rcp.todo.model.ITodoService;
import com.example.e4.rcp.todo.model.Todo;
import com.example.e4.rcp.todo.model.TodoRestUriConstants;

public class RestTodoServiceImpl implements ITodoService {

	public static final String SERVER_URI = "http://localhost:8080/todo";

	public static void main(String[] args) {
		RestTodoServiceImpl restTodoServiceImpl = new RestTodoServiceImpl();
		List<Todo> todos = restTodoServiceImpl.getTodos();
		System.out.println(todos);
		Todo todo = restTodoServiceImpl.getTodo(1);
		System.out.println(todo);
	}

	@Override
	public List<Todo> getTodos() {
		List<Todo> todos = new ArrayList<Todo>();
		try {

			String response = getJsonResponse(TodoRestUriConstants.GET_ALL_TODOS);

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode readTree = objectMapper.readTree(response);
			for (JsonNode jsonNode : readTree) {
				Todo todoValue = objectMapper.readValue(jsonNode, Todo.class);
				todos.add(todoValue);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return todos;
	}

	@Override
	public boolean saveTodo(Todo todo) {

		return true;
	}

	@Override
	public Todo getTodo(long id) {

		String jsonResponse = getJsonResponse(TodoRestUriConstants.GET_todo
				+ id);

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readValue(jsonResponse, Todo.class);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public boolean deleteTodo(long id) {
		return false;
	}

	private String getJsonResponse(String restPath) {
		Client client = ClientBuilder.newClient(new ClientConfig()
				.register(JsonProcessingFeature.class));

		WebTarget webTarget = client.target(SERVER_URI).path(restPath);

		return webTarget.request(MediaType.APPLICATION_JSON_TYPE).get(
				String.class);
	}

}
