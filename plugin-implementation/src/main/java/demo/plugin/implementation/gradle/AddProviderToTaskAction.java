package demo.plugin.implementation.gradle;

import lombok.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

@SuppressWarnings("ClassCanBeRecord")
public class AddProviderToTaskAction implements Action<Task> {
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
