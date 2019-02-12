package com.redhat.labsjp.sample.todobackend.controller;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.redhat.labsjp.sample.todobackend.controller.exceptionhandler.ErrorResponse;
import com.redhat.labsjp.sample.todobackend.controller.exceptionhandler.RestControllerExceptionHandler;
import com.redhat.labsjp.sample.todobackend.domain.Todo;
import com.redhat.labsjp.sample.todobackend.service.TodoService;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@RunWith(SpringRunner.class)
public class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private ObjectMapper mapper;

    private MockMvc mockMvc;

    @Before
    public void before() throws Exception {
        mapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(todoController)
                .setControllerAdvice(new RestControllerExceptionHandler()).build();
    }

    @Test
    public void getTodos() throws Exception {
        Todo todo1 = new Todo(1L, "title1", "status1", "desc1");
        Todo todo2 = new Todo(2L, "title2", "status2", "desc2");
        List<Todo> list = Lists.newArrayList(todo1, todo2);

        when(todoService.getTodos()).thenReturn(list);

        mockMvc.perform(get("/todos")).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(list)));
    }

    @Test
    public void getTodosIfNotPresent() throws Exception {
        List<Todo> list = Collections.emptyList();

        when(todoService.getTodos()).thenReturn(list);

        mockMvc.perform(get("/todos")).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(list)));
    }

    @Test
    public void getTodosInternalServerError() throws Exception {
        long id = 1;
        doThrow(RuntimeException.class).when(todoService).getTodos();

        mockMvc.perform(get("/todos")).andExpect(status().isInternalServerError())
                .andExpect(content().json(mapper.writeValueAsString(new ErrorResponse("E999", "Exception occured!!"))));
    }

    @Test
    public void getTodo() throws Exception {
        long id = 1;
        Todo todo = new Todo(id, "title1", "status1", "desc1");

        when(todoService.getTodo(id)).thenReturn(todo);

        mockMvc.perform(get("/todos/{todoId}", id)).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(todo)));
    }

    @Test
    public void getTodoIfNotPresent() throws Exception {
        long id = 1;
        when(todoService.getTodo(id)).thenReturn(null);

        mockMvc.perform(get("/todos/{todoId}", id)).andExpect(status().isOk())
                .andExpect(content().string(Matchers.isEmptyString()));
    }

    @Test
    public void getTodoInternalServerError() throws Exception {
        long id = 1;
        doThrow(RuntimeException.class).when(todoService).getTodo(id);

        mockMvc.perform(get("/todos/{todoId}", id)).andExpect(status().isInternalServerError())
                .andExpect(content().json(mapper.writeValueAsString(new ErrorResponse("E999", "Exception occured!!"))));
    }

    @Test
    public void postTodo() throws Exception {
        Todo todo = new Todo("title", "status", null);

        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        when(todoService.saveTodo(captor.capture())).thenReturn(todo);

        mockMvc.perform(
                post("/todos").contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isCreated()).andExpect(content().json(mapper.writeValueAsString(todo)));

        assertThat(captor.getValue(), Matchers.is(todo));
    }

    @Test
    public void postTodoIfTitleIsMissing() throws Exception {
        Todo todo = new Todo(1l, null, "status", null);

        when(todoService.saveTodo(Mockito.any())).thenReturn(null);

        mockMvc.perform(
                post("/todos").contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .json(mapper.writeValueAsString(new ErrorResponse("E001", "input arguments not valid!"))));

        verify(todoService, times(0)).saveTodo(Mockito.any());
    }

    @Test
    public void postTodoIfStatusIsMissing() throws Exception {
        Todo todo = new Todo(1l, "title", null, null);

        when(todoService.saveTodo(Mockito.any())).thenReturn(null);

        mockMvc.perform(
                post("/todos").contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .json(mapper.writeValueAsString(new ErrorResponse("E001", "input arguments not valid!"))));

        verify(todoService, times(0)).saveTodo(Mockito.any());
    }

    @Test
    public void postTodoInternalServerError() throws Exception {
        Todo todo = new Todo("title", "status", null);
        doThrow(RuntimeException.class).when(todoService).saveTodo(todo);

        mockMvc.perform(
                post("/todos").contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(mapper.writeValueAsString(new ErrorResponse("E999", "Exception occured!!"))));
    }

    @Test
    public void putTodo() throws Exception {
        long id = 1;
        Todo todo = new Todo(1l, "title", "status", null);

        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        when(todoService.saveTodo(captor.capture())).thenReturn(todo);

        mockMvc.perform(put("/todos/{todoId}", id).contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(todo))).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(todo)));

        assertThat(captor.getValue(), Matchers.is(todo));
    }

    @Test
    public void putTodoIfIdisDifferent() throws Exception {
        long id = 1;
        Todo todo = new Todo(2l, "title", "status", null);

        when(todoService.saveTodo(todo)).thenReturn(todo);

        mockMvc.perform(put("/todos/{todoId}", id).contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(todo))).andExpect(status().isBadRequest())
                .andExpect(content()
                        .json(mapper.writeValueAsString(new ErrorResponse("E001", "input arguments not valid!"))));

        verify(todoService, times(0)).saveTodo(todo);
    }

    @Test
    public void putTodoIfBodyIdNotSet() throws Exception {
        long id = 1;
        Todo todo = new Todo("title", "status", null);
        Todo result = new Todo(id, "title", "status", null);

        ArgumentCaptor<Todo> captor = ArgumentCaptor.forClass(Todo.class);
        when(todoService.saveTodo(captor.capture())).thenReturn(result);

        mockMvc.perform(put("/todos/{todoId}", id).contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(todo))).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(result)));

        assertThat(captor.getValue(), Matchers.is(result));
    }

    @Test
    public void putTodoIfTitleIsMissing() throws Exception {
        Todo todo = new Todo(1l, null, "status", null);

        when(todoService.saveTodo(Mockito.any())).thenReturn(null);

        mockMvc.perform(
                put("/todos/{todoId}", 1l).contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .json(mapper.writeValueAsString(new ErrorResponse("E001", "input arguments not valid!"))));

        verify(todoService, times(0)).saveTodo(Mockito.any());
    }

    @Test
    public void putTodoIfStatusIsMissing() throws Exception {
        Todo todo = new Todo(1l, "title", null, null);

        when(todoService.saveTodo(Mockito.any())).thenReturn(null);

        mockMvc.perform(
                put("/todos/{todoId}", 1l).contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isBadRequest()).andExpect(content()
                        .json(mapper.writeValueAsString(new ErrorResponse("E001", "input arguments not valid!"))));

        verify(todoService, times(0)).saveTodo(Mockito.any());
    }

    @Test
    public void putTodoInternalServerError() throws Exception {
        Todo todo = new Todo(1l, "title", "status", null);
        doThrow(RuntimeException.class).when(todoService).saveTodo(todo);

        mockMvc.perform(
                put("/todos/{todoId}", 1l).contentType(MediaType.APPLICATION_JSON_UTF8).content(mapper.writeValueAsString(todo)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().json(mapper.writeValueAsString(new ErrorResponse("E999", "Exception occured!!"))));
    }

    @Test
    public void deleteTodo() throws Exception {
        long id = 1;

        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);
        doNothing().when(todoService).deleteTodo(captor.capture());

        mockMvc.perform(delete("/todos/{todoId}", id)).andExpect(status().isOk()).andExpect(content().string(""));

        assertThat(captor.getValue(), Matchers.is(id));
    }

    @Test
    public void deleteTodoInternalServerError() throws Exception {
        long id = 1;

        doThrow(RuntimeException.class).when(todoService).deleteTodo(id);

        mockMvc.perform(delete("/todos/{todoId}", id)).andExpect(status().isInternalServerError())
                .andExpect(content().json(mapper.writeValueAsString(new ErrorResponse("E999", "Exception occured!!"))));
    }
}