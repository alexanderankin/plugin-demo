package demo.plugin.implementation;

import lombok.NonNull;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;

public class DemoPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        project.getTasks().register("demoPluginGreeting", task -> {
            task.setDescription("greets user from the demo plugin");
            task.setGroup(BasePlugin.BUILD_GROUP);
            task.doLast(t -> System.out.println("hi from demoPluginGreeting task"));
        });

        project.getExtensions().create("binaryResources", BinaryResourcesExtension.class);
        project.getGradle().getTaskGraph().whenReady(new BinaryResources.BinaryResourcesRegistrationAction(project));
        new BinaryResources.BinaryResourcesRegistrationAction(project);
    }
}
