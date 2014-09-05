package com.example.e4.rcp.todo.restservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;

import com.example.e4.rcp.todo.events.MyEventConstants;
import com.example.e4.rcp.todo.model.ITodoService;
import com.example.e4.rcp.todo.model.Todo;
import com.example.e4.rcp.todo.model.TodoRestUriConstants;

public class RestTodoServiceImpl implements ITodoService {

	public static final String NODEPATH = "com.example.e4.rcp.todo";
	public static final String SERVER_URI_PREF_KEY = "server";

	public static final String SERVER_URI_DEFAULT = "http://localhost:8080/todo";

	private String serverUri = SERVER_URI_DEFAULT;

	@Inject
	private IEventBroker broker;

	@Inject
	public void setServerUri(
			@Preference(nodePath = NODEPATH, value = SERVER_URI_PREF_KEY) String serverUri) {
		if(serverUri != null) {
			this.serverUri = serverUri;
		}
	}

	@Override
	public List<Todo> getTodos() {
		List<Todo> todos = new ArrayList<Todo>();
		HttpClient client = new DefaultHttpClient();
		try {
			
			HttpGet get = new HttpGet(serverUri
					+ TodoRestUriConstants.GET_ALL_TODOS);
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if (null == entity) {
				return Collections.emptyList();
			}
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");

			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode readTree = objectMapper.readTree(result);
			for (JsonNode jsonNode : readTree) {
				Todo todoValue = objectMapper.readValue(jsonNode, Todo.class);
				todos.add(todoValue);
			}


		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return todos;
	}

	@Override
	public boolean saveTodo(Todo todo) {

		ObjectMapper objectMapper = new ObjectMapper();
		HttpClient client = new DefaultHttpClient();
		try {
			String todoJson = objectMapper.writeValueAsString(todo);
			HttpPost httpPost = new HttpPost(serverUri
					+ TodoRestUriConstants.SAVE_todo);
			httpPost.setHeader("Content-Type", "application/json");
			httpPost.setEntity(new StringEntity(todoJson));
			HttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == 200) {
				broker.post(MyEventConstants.TOPIC_TODO_NEW,
						getTodo(todo.getId()));
				return true;
			} else {
				broker.post(MyEventConstants.TOPIC_TODO_UPDATE,
						getTodo(todo.getId()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}

		return false;
	}

	@Override
	public Todo getTodo(long id) {

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(serverUri + TodoRestUriConstants.GET_todo
				+ id);
		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			if (null == entity) {
				return null;
			}
			String result = EntityUtils.toString(response.getEntity(), "UTF-8");

			if (!result.isEmpty()) {
				ObjectMapper objectMapper = new ObjectMapper();
				return objectMapper.readValue(result, Todo.class);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return null;
	}

	@Override
	public boolean deleteTodo(long id) {
		HttpClient client = new DefaultHttpClient();
		HttpDelete httpDelete = new HttpDelete(serverUri
				+ TodoRestUriConstants.DELETE_todo + id);
		HttpResponse response;
		try {
			response = client.execute(httpDelete);
			if (response.getStatusLine().getStatusCode() == 200) {
				broker.post(MyEventConstants.TOPIC_TODO_DELETE, getTodo(id));
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}

		return false;
	}
}
