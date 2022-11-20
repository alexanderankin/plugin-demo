package demo.plugin.implementation.gradle;

import lombok.Getter;
import lombok.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.testing.Test;

@Getter
@SuppressWarnings("Convert2Lambda") // gradle doesn't like lambdas, groovy doesn't produce them
public class BrePlugin implements Plugin<Project> {
    private Project project;

    @Override
    public void apply(@NonNull Project project) {
        this.project = project;
        project.getTasks().register("demoPluginGreeting", task -> {
            task.setDescription("greets user from the demo plugin");
            task.setGroup(BasePlugin.BUILD_GROUP);
            task.doLast(t -> System.out.println("hi from demoPluginGreeting task"));
        });

        project.getExtensions().create("binaryResources", BinaryResourcesExtension.class);
        // this seems brittle because if you want to tweak how its registered - ready vs afterEvaluate - need to change
        // project.getGradle().getTaskGraph().whenReady(new BinaryResources.BinaryResourcesRegistrationAction(project));
        // maybe just encapsulate registration entirely:
        BinaryResources.register(this);

        // if you wait after evaluating, you can just register the shared service with the extension data
        project.afterEvaluate(new Action<Project>() {
            BinaryResourcesExtension bre;

            @Override
            public void execute(@NonNull Project project) {
                bre = breWithDefaults();
                if (bre.getExecTask().isPresent()) registerExec();
            }

            void registerExec() {
                Provider<BinaryResourcesService> provider = project.getGradle().getSharedServices()
                        .registerIfAbsent("binaryResourcesService",
                                BinaryResourcesService.class,
                                spec -> {
                                    // todo refactor (hide ugly copying logic)
                                    spec.getParameters().getArguments().set(bre.getExecTask().get().getArgs());
                                });

                project.getTasks().withType(bre.getTaskType().get()).configureEach(new AddProviderToTaskAction(provider));
            }
        });
    }

    public BinaryResourcesExtension breWithDefaults() {
        BinaryResourcesExtension bre = project.getExtensions().getByType(BinaryResourcesExtension.class);
        fixupDefault(bre.getTaskType(), Test.class);
        fixupDefault(bre.getAttempts(), 3);
        return bre;
    }

    private <T> void fixupDefault(Property<T> property, T defaultValue) {
        property.set(property.getOrElse(defaultValue));
    }
}
