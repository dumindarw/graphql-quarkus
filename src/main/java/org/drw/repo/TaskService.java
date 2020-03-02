package org.drw.repo;

import graphql.schema.DataFetchingEnvironment;
import org.drw.model.Task;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class TaskService {

    private Map<String, Task> tasks;

    private Map<String, Task> initData() {
        Stream<Task> stream = Stream.<Task>builder()
                .add(new Task("Task1"))
                .add(new Task("Task2"))
                .add(new Task("Task3"))
                .build();


        return stream.collect(Collectors.toMap(Task::getId, task -> task));
    }

    public TaskService(){
        this.tasks = initData();
    }

    public List<Task> allTasks(DataFetchingEnvironment env) {
        boolean uncompletedOnly = env.getArgument("uncompletedOnly");

        return this.tasks.values().stream()
                .filter(task -> !uncompletedOnly || !task.isCompleted())
                .collect(Collectors.toList());
    }

    public boolean complete(DataFetchingEnvironment env) {
        String id = env.getArgument("id");
        Task task = this.tasks.get(id);

        if (task == null) {
            return false;
        }

        task.setCompleted(true);

        return true;
    }

}
