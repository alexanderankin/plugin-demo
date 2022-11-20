package demo.plugin.implementation.impl.extservices.impl;

import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;

/**
 * How should the service be run in the background
 */
public enum BackgroundType {
    /**
     * using Gradle {@link BuildService}s.
     * <p></p>
     * This strategy is experimental because
     * it uses {@link Incubating} Gradle APIs,
     * such as {@link Task#usesService(Provider)}
     */
    @SuppressWarnings("UnstableApiUsage")
    @Incubating
    GRADLE_SERVICE,

    /**
     * using {@link Task#dependsOn}/{@link Task#finalizedBy} as start/stop.
     * <p>
     * this strategy is the recommended one.
     */
    TASK_GRAPH,

    /**
     * using {@link Task#doFirst}/{@link Task#doLast} as start/stop.
     * <p>
     * this strategy is not recommended because
     * it might cause a new external service
     * to be created for each affected task.
     */
    // todo fix stack overflow error (in project.execute)
    FIRST_LAST,
}
