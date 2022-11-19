package demo.plugin.implementation;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.testing.Test;

import java.util.concurrent.atomic.AtomicInteger;

@Accessors(fluent = true)
@Getter
public class BinaryResources {
    private final BinaryResourcesExtension ext;
    private final AtomicInteger testStartedCounter;
    private final Action<Task> testConfigurer;
    private final Action<Task> startCb;
    private final Action<Task> endCb;

    public BinaryResources(BinaryResourcesExtension ext) {
        this.ext = ext;
        testStartedCounter = new AtomicInteger();
        testConfigurer = new TestConfigurer();
        startCb = new RunnableTaskAction(this::start);
        endCb = new RunnableTaskAction(this::end);
    }

    /**
     * register all the {@link BinaryResources} logic onto the {@link Project},
     * also find all {@link Test} tasks and add {@link Task#doFirst(Action)}
     * and {@link Task#doLast(Action)} callbacks.
     */
    @SuppressWarnings("Convert2Lambda")
    public static void register(Project project) {
        project.afterEvaluate(new Action<>() {
            @Override
            public void execute(@NonNull Project project) {
                // instantiate
                BinaryResources binaryResources =
                        new BinaryResources(project.getExtensions()
                                .getByType(BinaryResourcesExtension.class));

                // attach to tasks
                project.getTasks()
                        .withType(project.getExtensions()
                                .getByType(BinaryResourcesExtension.class)
                                .getTaskType()
                                .get())
                        .configureEach(binaryResources.testConfigurer());
            }
        });
    }

    public void start() {
        int i = testStartedCounter.incrementAndGet();
        if (i > 0) {
            ensureRunning();
        }
    }

    public void end() {
        int tasksRunning = testStartedCounter.decrementAndGet();

        if (tasksRunning == 0) {
            ensureOff();
        }
    }

    void ensureRunning() {
        // fill in
    }

    void ensureOff() {
        // fill in
    }

    /**
     * converts a runnable into an {@code Action<Task>}
     */
    @SuppressWarnings("ClassCanBeRecord")
    private static class RunnableTaskAction implements Action<Task> {
        private final Runnable runnable;

        public RunnableTaskAction(Runnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void execute(@NonNull Task task) {
            runnable.run();
        }
    }

    /**
     * will configure a test with a {@link BinaryResources} instance
     */
    private class TestConfigurer implements Action<Task> {
        @Override
        public void execute(@NonNull Task test) {
            test.doFirst(startCb());
            test.doLast(endCb());
        }
    }
}
