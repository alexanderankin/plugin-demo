package demo.plugin.implementation.impl.extservices.gradle;

import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration;
import demo.plugin.implementation.impl.extservices.impl.ExternalServicesManager;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceRegistration;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("Convert2Lambda")
public class ExternalServicesPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        project.getExtensions().add("externalServices",
                ExternalServicesExtension.class);

        project.afterEvaluate(new Action<>() {
            @Override
            public void execute(@NonNull Project project) {
                ExternalServicesExtension ese = project.getExtensions().getByType(ExternalServicesExtension.class);
                new ExternalServicesManager(new ProjectWrapper(project), ese).init();
            }
        });

        project.getTasks().register("listServices").configure(new Action<>() {
            @Override
            public void execute(@NonNull Task task) {
                task.doLast(new Action<>() {
                    @Override
                    public void execute(@NonNull Task task) {
                        System.out.println(project.getExtensions().getByType(ExternalServicesExtension.class).getServices().stream().map(Object::toString).collect(Collectors.toList()));
                    }
                });
            }
        });
    }

    private static class ProjectWrapper implements demo.plugin.implementation.impl.extservices.impl.Project {
        private final Project project;
        private Set<Task> tasks;

        public ProjectWrapper(@NonNull Project project) {
            this.project = project;
        }

        @Override
        public Set<Task> tasks() {
            if (tasks == null) tasks = project.getTasks().stream().map(this::tw).collect(Collectors.toSet());
            return tasks;
        }

        @Override
        public Runnable registerService(ExternalServicesConfiguration.ExternalService service) {
            Provider<BuildServiceAdapter> provider = project.getGradle().getSharedServices().registerIfAbsent(service.getName(),
                    BuildServiceAdapter.class,
                    BuildServiceAdapter.Params.configurer(project, service));

            return provider::get;
        }

        @Override
        public void execute(String taskName) {
            tasks().stream().filter(t -> t.name().equals(taskName)).findAny().ifPresent(Task::execute);
        }

        private TaskWrapper tw(org.gradle.api.Task task) {
            return new TaskWrapper(project, task);
        }

        @AllArgsConstructor
        private static class TaskWrapper implements demo.plugin.implementation.impl.extservices.impl.Project.Task {
            private final Project project;
            private final org.gradle.api.Task task;

            @Override
            public String name() {
                return task.getName();
            }

            @Override
            public Class<?> type() {
                return task.getClass();
            }

            @Override
            public void execute() {
                execute(task);
            }

            @Override
            public void doFirst(Runnable runnable) {
                task.doFirst(new Action<>() {
                    @Override
                    public void execute(@NonNull org.gradle.api.Task task) {
                        runnable.run();
                    }
                });
            }

            @Override
            public void doLast(Runnable runnable) {
                task.doLast(new Action<>() {
                    @Override
                    public void execute(@NonNull org.gradle.api.Task task) {
                        runnable.run();
                    }
                });
            }

            @SuppressWarnings("UnstableApiUsage")
            @Override
            public void requireService(String name) {
                Set<BuildServiceRegistration<?, ?>> registrations =
                        project.getGradle()
                                .getSharedServices()
                                .getRegistrations()
                                .matching(element -> element.getName().equals(name));

                if (registrations.isEmpty())
                    throw new IllegalArgumentException("no registrations with that name: " + name);
                if (registrations.size() > 1)
                    throw new IllegalStateException("multiple registrations with the same name: " + name);

                Provider<? extends BuildService<?>> service = registrations.iterator().next().getService();
                task.usesService(service);
            }

            /**
             * @see <a href="https://stackoverflow.com/a/70205357/4971476">stack overflow</a>
             */
            private void execute(org.gradle.api.Task task) {
                task.getTaskDependencies().getDependencies(task).forEach(this::execute);
                task.getActions().forEach(a -> a.execute(task));
            }

            @Override
            public void needsBefore(String other) {
                task.getDependsOn().add(other);
            }

            @Override
            public void needsAfter(String other) {
                if (null != getByName(other)) task.getFinalizedBy().getDependencies(task).add(getByName(other));
            }

            @SuppressWarnings("unchecked") // narrowing
            private <T extends org.gradle.api.Task> T getByName(String other) {
                return (T) project.getTasks().findByName(other);
            }
        }
    }
}
