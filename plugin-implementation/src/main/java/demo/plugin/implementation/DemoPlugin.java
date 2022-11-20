package demo.plugin.implementation;

import demo.plugin.implementation.gradle.BrePlugin;
import demo.plugin.implementation.impl.extservices.gradle.ExternalServicesPlugin;
import lombok.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import java.util.Optional;

public class DemoPlugin implements Plugin<Project> {
    @SuppressWarnings("ConstantConditions") // analysis is wrong, getOrElse takes null fine
    public static <T> Optional<T> of(Provider<T> tProvider) {
        return Optional.ofNullable(tProvider.getOrElse(null));
    }

    @Override
    public void apply(@NonNull Project project) {
        // todo verify using this gradle api works
        // project.getPluginManager().apply(BrePlugin.class);
        // project.getPluginManager().apply(ExternalServicesPlugin.class);
        new BrePlugin().apply(project);
        new ExternalServicesPlugin().apply(project);
    }

}
