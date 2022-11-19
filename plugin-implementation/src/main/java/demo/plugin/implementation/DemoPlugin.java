package demo.plugin.implementation;

import demo.plugin.implementation.gradle.DemoPluginSetup;
import lombok.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class DemoPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        DemoPluginSetup.setup(project);
    }

}
