package demo.plugin.implementation;

import demo.plugin.implementation.gradle.BrePlugin;
import demo.plugin.implementation.impl.extservices.gradle.ExternalServicesPlugin;
import lombok.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DemoPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        new BrePlugin().apply(project);
        new ExternalServicesPlugin().apply(project);
    }

}
