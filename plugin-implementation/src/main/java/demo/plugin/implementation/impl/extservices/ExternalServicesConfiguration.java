package demo.plugin.implementation.impl.extservices;

import demo.plugin.implementation.impl.extservices.impl.BackgroundType;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExternalServicesConfiguration {
    Optional<BackgroundType> defaultBackgroundType();

    Set<? extends ExternalService> services();

    interface ExternalService {
        String getName();

        Optional<BackgroundType> backgroundType();

        Optional<List<Class<?>>> taskTypes();

        Optional<List<String>> taskNames();

        Optional<String> start();

        Optional<String> end();
    }
}
