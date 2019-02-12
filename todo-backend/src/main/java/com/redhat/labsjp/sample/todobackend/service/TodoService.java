package com.redhat.labsjp.sample.todobackend.service;

import java.util.Optional;

import com.redhat.labsjp.sample.todobackend.domain.Todo;
import com.redhat.labsjp.sample.todobackend.repository.TodoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TodoService {

    @Autowired
    TodoRepository todoRepository;

    public Iterable<Todo> getTodos() {
        return todoRepository.findAll();
    }

    public Todo getTodo(long todoId) {
        Optional<Todo> todo = todoRepository.findById(todoId);
        if (todo.isPresent()) {
            return todo.get();
        }
        return null;
    }

    public Todo saveTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public void deleteTodo(long todoId) {
        todoRepository.deleteById(todoId);
    }
}