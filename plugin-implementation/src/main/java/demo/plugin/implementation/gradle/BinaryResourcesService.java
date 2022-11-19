package demo.plugin.implementation.gradle;

import demo.plugin.implementation.DemoPlugin;
import lombok.SneakyThrows;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import javax.inject.Inject;

public abstract class BinaryResourcesService implements BuildService<BinaryResourcesService.Params>, AutoCloseable {
    /**
     * not possible here is the list of what is injectable - <a href="https://docs.gradle.org/current/userguide/custom_gradle_types.html#service_injection">Service Injection in gradle user guide</a>
     */
    @Inject
    Project project;

    @SneakyThrows
    public BinaryResourcesService() {
        ListProperty<String> resources = getParameters().getArguments();

        // ok, so here we are, how do we get our project to get our BinaryResourcesExtension?
        // answer = that hook that we have to pass to register the service
    }

    // Some parameters for the web server
    interface Params extends BuildServiceParameters {
        Property<Integer> getPort();

        /**
         * assigned in {@link DemoPlugin#apply(Project)}
         *
         * @return cli arguments to pass
         */
        ListProperty<String> getArguments();
    }

    @Override
    public void close() throws Exception {
        // todo
    }
}
