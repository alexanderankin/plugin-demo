package demo.plugin.implementation;

import lombok.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.provider.Provider;

@SuppressWarnings("Convert2Lambda") // gradle doesn't like lambdas, groovy doesn't produce them
public class DemoPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        project.getTasks().register("demoPluginGreeting", task -> {
            task.setDescription("greets user from the demo plugin");
            task.setGroup(BasePlugin.BUILD_GROUP);
            task.doLast(t -> System.out.println("hi from demoPluginGreeting task"));
        });

        project.getExtensions().create("binaryResources", BinaryResourcesExtension.class);
        // this seems brittle because if you want to tweak how its registered - ready vs afterEvaluate - need to change
        // project.getGradle().getTaskGraph().whenReady(new BinaryResources.BinaryResourcesRegistrationAction(project));
        // maybe just encapsulate registration entirely:
        BinaryResources.register(project);

        // if you wait after evaluating, you can just register the shared service with the extension data
        project.afterEvaluate(new Action<>() {
            @Override
            public void execute(@NonNull Project project) {
                BinaryResourcesExtension bre = project.getExtensions().getByType(BinaryResourcesExtension.class);
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

    @SuppressWarnings("ClassCanBeRecord")
    private static class AddProviderToTaskAction implements Action<Task> {
        private final Provider<BinaryResourcesService> provider;

        public AddProviderToTaskAction(Provider<BinaryResourcesService> provider) {
            this.provider = provider;
        }

        /**
         * needs to both use the service and {@link Provider#get()} it, see linked explanation
         *
         * @param test The object to perform the action on.
         * @see <a href="https://discuss.gradle.org/t/clean-way-to-start-up-shut-down-some-resource-around-test-tasks/43932/6">service usage explanation</a>
         */
        @Override
        @SuppressWarnings("UnstableApiUsage")
        public void execute(@NonNull Task test) {
            // register the service
            test.usesService(provider);
            test.doFirst(new Action<>() {
                @Override
                public void execute(@NonNull Task task) {
                    // actually trigger its creation
                    provider.get();
                }
            });
        }
    }
}
