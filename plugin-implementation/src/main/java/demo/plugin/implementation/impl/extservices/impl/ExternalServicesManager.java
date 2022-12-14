package demo.plugin.implementation.impl.extservices.impl;

import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration;
import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration.ExternalService;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.tasks.testing.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ExternalServicesManager {
    private static final BackgroundType DEFAULT_BG_TYPE = BackgroundType.TASK_GRAPH;
    private final Project project;
    private final ExternalServicesConfiguration ese;
    private final Map<BackgroundType, Consumer<ExternalService>> bgTypes;

    public ExternalServicesManager(Project project,
                                   ExternalServicesConfiguration ese) {
        this.project = project;
        this.ese = ese;
        bgTypes = Map.ofEntries(
                Map.entry(BackgroundType.GRADLE_SERVICE, this::bgWithGradleService),
                Map.entry(BackgroundType.TASK_GRAPH, this::bgWithTaskGraph),
                Map.entry(BackgroundType.FIRST_LAST, this::bgWithFirstLast)
        );
    }

    public void init() {
        for (ExternalService service : ese.services()) {
            BackgroundType backgroundType = service.backgroundType().orElse(ese.defaultBackgroundType().orElse(DEFAULT_BG_TYPE));
            log.warn("configuring {} with {}", service.getName(), backgroundType);
            bgTypes.get(backgroundType).accept(service);
        }
    }

    void bgWithGradleService(ExternalService service) {
        List<Project.Task> tasks = filter(service).collect(Collectors.toList());
        tasks = defaultTestTask(tasks);

        Runnable startService = project.registerService(service);

        tasks.forEach(task -> task.requireService(service.getName()));
        tasks.forEach(task -> task.doFirst(startService));
    }

    void bgWithTaskGraph(ExternalService service) {
        List<Project.Task> tasks = filter(service).collect(Collectors.toList());

        List<Project.Task> taskList = defaultTestTask(tasks);
        log.warn("tasks are: {} (without filtering: {})",
                taskList,
                project.tasks().stream().map(Project.Task::name).collect(Collectors.toList()));
        taskList.forEach(task -> {
            log.warn("configuring task {} to need {} before", task.name(), service.start());
            task.needsBefore(service.start().orElseThrow(() -> new IllegalArgumentException("missing start task")));
        });
        service.end().ifPresent(end -> taskList.forEach(task -> task.needsAfter(end)));
    }

    private List<Project.Task> defaultTestTask(List<Project.Task> tasks) {
        return Optional.of(tasks)
                .filter(Predicate.not(List::isEmpty))
                .orElseGet(() -> List.of(project.tasks().stream()
                        .filter(e -> e.name().equalsIgnoreCase("test"))
                        .findAny().orElseThrow()));
    }

    void bgWithFirstLast(ExternalService service) {
        Stream<Project.Task> tasks = filter(service);

        tasks.forEach(task -> task.doFirst(() -> project.execute(service.start().orElseThrow(() -> new IllegalArgumentException("missing start task")))));
        service.end().ifPresent(end -> tasks.forEach(task -> task.doLast(() -> project.execute(end))));
    }

    private Stream<Project.Task> filter(ExternalService service) {
        Stream<Project.Task> tasks = project.tasks().stream();

        List<String> taskNames = service.taskNames().orElseGet(List::of);
        if (!taskNames.isEmpty()) {
            log.warn("task names are not empty, filtering by: {}", taskNames);
            tasks = tasks.filter(t -> taskNames.contains(t.name()));
        }

        // use reasonable default here otherwise we will track in all tasks
        List<Class<?>> taskTypes = service.taskTypes().filter(Predicate.not(List::isEmpty)).orElse(List.of(Test.class));
        log.warn("task types: filtering by: {}", taskTypes.stream().map(Class::getName).collect(Collectors.toList()));
        tasks = tasks.filter(t -> taskTypes.stream().anyMatch(tt -> tt.isInstance(t)));

        return tasks;
    }
}
