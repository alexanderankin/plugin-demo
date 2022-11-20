package demo.plugin.implementation.impl.extservices.impl;

import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration;
import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration.ExternalService;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.tasks.testing.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
            bgTypes.get(backgroundType).accept(service);
        }
    }

    void bgWithGradleService(ExternalService service) {
        Stream<Project.Task> tasks = filter(service);

        Runnable startService = project.registerService(service);

        tasks.forEach(task -> task.requireService(service.getName()));
        tasks.forEach(task -> task.doFirst(startService));
    }

    void bgWithTaskGraph(ExternalService service) {
        Stream<Project.Task> tasks = filter(service);

        tasks.forEach(task -> task.needsBefore(service.start().orElseThrow(() -> new IllegalArgumentException("missing start task"))));
        service.end().ifPresent(end -> tasks.forEach(task -> task.needsAfter(end)));
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
            tasks = tasks.filter(t -> taskNames.contains(t.name()));
        }

        // use reasonable default here otherwise we will track in all tasks
        List<Class<?>> taskTypes = service.taskTypes().filter(Predicate.not(List::isEmpty)).orElse(List.of(Test.class));
        tasks = tasks.filter(t -> taskTypes.stream().anyMatch(tt -> tt.isInstance(t)));

        return tasks;
    }
}
