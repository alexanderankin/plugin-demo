package demo.plugin.implementation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionGraph;
import org.gradle.api.tasks.testing.Test;

import java.util.concurrent.atomic.AtomicInteger;

@Accessors(fluent = true)
@Getter
public class BinaryResources {
    private final BinaryResourcesExtension ext;
    private final AtomicInteger testStartedCounter;
    private final Action<Test> testConfigurer;
    private final Action<Task> startCb;
    private final Action<Task> endCb;

    public BinaryResources(BinaryResourcesExtension ext) {
        this.ext = ext;
        testStartedCounter = new AtomicInteger();
        testConfigurer = new TestConfigurer();
        startCb = new RunnableTaskAction(this::start);
        endCb = new RunnableTaskAction(this::end);
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
    }

    void ensureOff() {
    }

    /**
     * register all the {@link BinaryResources} logic onto the {@link Project},
     * also find all {@link Test} tasks and add {@link Task#doFirst(Action)}
     * and {@link Task#doLast(Action)} callbacks.
     */
    @AllArgsConstructor
    public static class BinaryResourcesRegistrationAction implements Action<TaskExecutionGraph> {
        private final Project project;

        @Override
        public void execute(@NonNull TaskExecutionGraph graph) {
            BinaryResources binaryResources =
                    new BinaryResources(project.getExtensions()
                            .getByType(BinaryResourcesExtension.class));

            project.getTasks().withType(Test.class)
                    .configureEach(binaryResources.testConfigurer());
        }
    }

    /**
     * converts a runnable into an {@code Action<Task>}
     */
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
    private class TestConfigurer implements Action<Test> {
        @Override
        public void execute(@NonNull Test test) {
            test.doFirst(startCb());
            test.doLast(endCb());
        }
    }
}
