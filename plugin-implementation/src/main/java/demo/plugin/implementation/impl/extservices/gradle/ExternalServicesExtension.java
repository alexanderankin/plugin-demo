package demo.plugin.implementation.impl.extservices.gradle;

import demo.plugin.implementation.DemoPlugin;
import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration;
import demo.plugin.implementation.impl.extservices.impl.BackgroundType;
import lombok.ToString;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * DSL interface for configuring external services provided during a build
 */
public abstract class ExternalServicesExtension implements ExternalServicesConfiguration {
    // private NamedDomainObjectContainer<ExternalService> services;

    @Inject
    public abstract ObjectFactory getObjectFactory();

    /**
     * @return type of task to provide the binary resource for
     */
    abstract public Property<BackgroundType> getDefaultBackgroundType();

    /**
     * <pre>
     *     externalServices {
     *         services {
     *             abc {
     *                 start 'startAbc'
     *                 end 'endAbc'
     *                 ...
     *             }
     *             create('def') {
     *                 start tasks.create('startDef', Exec) { ... }
     *                 ...
     *             }
     *         }
     *     }
     * </pre>
     *
     * @return services which are made available to the tasks
     */
    public abstract NamedDomainObjectContainer<ExternalService> getServices()
    ;
    // {
    //     if (services == null) services = ndoc(getObjectFactory(), ExternalService.class);
    //     return services;
    // }

    @Override
    public Optional<BackgroundType> defaultBackgroundType() {
        return DemoPlugin.of(getDefaultBackgroundType());
    }

    @Override
    public Set<ExternalService> services() {
        return getServices();
    }

    /**
     * abstracts away gradle boilerplate
     */
    @SuppressWarnings("SameParameterValue")
    private <T> NamedDomainObjectContainer<T> ndoc(ObjectFactory objects, Class<T> tClass) {
        return objects.domainObjectContainer(tClass, name -> objects.newInstance(tClass, name));
    }

    /**
     * model per-service configuration
     */
    @ToString
    static abstract public class ExternalService implements ExternalServicesConfiguration.ExternalService {
        private final String name;

        /**
         * @see <a href="https://docs.gradle.org/7.5.1/userguide/implementing_gradle_plugins.html#declaring_a_dsl_configuration_container">docs</a>
         */
        @Inject
        public ExternalService(String name) {
            this.name = name;
        }

        /**
         * item label from gradle {@link NamedDomainObjectContainer} syntax
         */
        public String getName() {
            return name;
        }

        /**
         * let the user customize each tasks background type
         */
        abstract public Property<BackgroundType> getBackgroundType();

        /**
         * if not empty, scan for these <b>types of tasks</b> for adding services
         */
        abstract public ListProperty<Class<? extends Task>> getTaskTypes();

        /**
         * if not empty, scan for these tasks <b>by name</b> for adding services
         */
        abstract public ListProperty<String> getTaskNames();

        /**
         * @return name of task which starts the external service
         */
        abstract public Property<String> getStart();

        /**
         * @return name of the task that turns off the external service (optional)
         */
        abstract public Property<String> getEnd();

        public Optional<BackgroundType> backgroundType() {
            return DemoPlugin.of(getBackgroundType());
        }

        @SuppressWarnings("unchecked") // narrowing
        public Optional<List<Class<?>>> taskTypes() {
            return DemoPlugin.of(getTaskTypes()).map(List.class::cast);
        }

        public Optional<List<String>> taskNames() {
            return DemoPlugin.of(getTaskNames());
        }

        public Optional<String> start() {
            return DemoPlugin.of(getStart());
        }

        public Optional<String> end() {
            return DemoPlugin.of(getEnd());
        }
    }
}
