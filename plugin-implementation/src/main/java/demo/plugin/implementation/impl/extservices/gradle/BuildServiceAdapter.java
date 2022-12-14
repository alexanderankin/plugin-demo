package demo.plugin.implementation.impl.extservices.gradle;

import com.sun.net.httpserver.HttpServer;
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

import java.net.InetSocketAddress;
import java.util.Optional;

public abstract class BuildServiceAdapter implements BuildService<BuildServiceAdapter.Params>, AutoCloseable {
    private final Task end;
    HttpServer httpServer;

    @SneakyThrows
    public BuildServiceAdapter() {
        System.out.println("hi from shared service!!");
        httpServer = HttpServer.create();
        httpServer.createContext("/", c -> {
            c.sendResponseHeaders(200, 0); c.close();
        });
        httpServer.bind(new InetSocketAddress("localhost", 3000), 0);
        httpServer.start();
        end = null;
        // ExternalServicesConfiguration.ExternalService externalService = getParameters().getExternalService().get();
        // end = externalService.end().map(this::lookup).orElse(null);
        //
        // Task startTask = lookup(externalService.start().orElseThrow(() -> new IllegalStateException("no start task specified")));
        //
        // execute(startTask);

    }

    // Task lookup(String name) {
    //     return lookup(getParameters().getProject().get(), name);
    // }

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
        httpServer.stop(0);
        if (end == null) return;
        execute(end);
    }

    /**
     * this will have to contain only serializable things,
     * which means we cant have fun task interactions here
     * <p>
     * the means that if we wanted to use the
     * {@link org.gradle.api.tasks.Exec} signature, for instance,
     * to let the user specify the exec params (for convenience and familiarity)
     * we have to recreate it here with just the data parts.
     * <p>
     * This means that there is even less code shared between different approaches.
     */
    interface Params extends BuildServiceParameters {
        @SuppressWarnings("Convert2Lambda")
        static Action<BuildServiceSpec<Params>> configurer(Project project, ExternalServicesConfiguration.ExternalService service) {
            return new Action<>() {
                @Override
                public void execute(@NonNull BuildServiceSpec<Params> spec) {
                    Params parameters = spec.getParameters();
                    // parameters.getExternalService().set(service);
                    // parameters.getProject().set(project);
                }
            };
        }

        // Property<ExternalServicesConfiguration.ExternalService> getExternalService();

        // Property<Project> getProject();
    }
}
