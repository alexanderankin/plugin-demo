package demo.plugin.implementation.impl.extservices.gradle;

import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.api.services.BuildServiceSpec;

import java.util.Optional;

public abstract class BuildServiceAdapter implements BuildService<BuildServiceAdapter.Params>, AutoCloseable {
    private final Task end;

    public BuildServiceAdapter() {
        ExternalServicesConfiguration.ExternalService externalService = getParameters().getExternalService().get();
        end = externalService.end().map(this::lookup).orElse(null);

        Task startTask = lookup(externalService.start().orElseThrow(() -> new IllegalStateException("no start task specified")));

        execute(startTask);

    }

    Task lookup(String name) {
        return lookup(getParameters().getProject().get(), name);
    }

    Task lookup(Project project, String name) {
        return Optional.ofNullable(project.getTasks().findByName(name))
                .orElseThrow(() -> new IllegalStateException("no such task for given name: " + name));
    }

    /**
     * @see <a href="https://stackoverflow.com/a/70205357/4971476">stack overflow</a>
     */
    private void execute(Task task) {
        task.getTaskDependencies().getDependencies(task).forEach(this::execute);
        task.getActions().forEach(action -> action.execute(task));
    }

    @SneakyThrows
    @Override
    public void close() {
        if (end == null) return;
        execute(end);
    }

    interface Params extends BuildServiceParameters {
        @SuppressWarnings("Convert2Lambda")
        static Action<BuildServiceSpec<Params>> configurer(Project project, ExternalServicesConfiguration.ExternalService service) {
            return new Action<>() {
                @Override
                public void execute(@NonNull BuildServiceSpec<Params> spec) {
                    Params parameters = spec.getParameters();
                    parameters.getExternalService().set(service);
                    parameters.getProject().set(project);
                }
            };
        }

        Property<ExternalServicesConfiguration.ExternalService> getExternalService();

        Property<Project> getProject();
    }
}
