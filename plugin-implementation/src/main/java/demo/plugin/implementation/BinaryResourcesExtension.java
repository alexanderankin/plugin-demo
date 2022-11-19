package demo.plugin.implementation;

import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Exec;

import java.util.concurrent.Callable;

public abstract class BinaryResourcesExtension {
    /**
     * @return type of task to provide the binary resource for
     */
    abstract public Property<Class<? extends Task>> getTaskType();

    /**
     * ...
     *
     * @return exec task to run in the background
     */
    abstract public Property<Exec> getExecTask();

    // /**
    //  * provide path to executable to execute
    //  *
    //  * @return path to the executable
    //  */
    // abstract public Property<String> getExecutable();
    //
    // /**
    //  * provide arguments to pass to the executable
    //  *
    //  * @return path to the executable
    //  */
    // abstract public Property<java.util.List<String>> getArguments();

    /**
     * property which indicates when the {@link #getExecTask()} is ready
     *
     * @return like podspec.readinessProbe
     */
    abstract public Property<Callable<Boolean>> getReadinessProbe();

    /**
     * number of attempts to check if we are ready
     *
     * @return number of times to execute {@link #getReadinessProbe()}
     */
    abstract public Property<Integer> getAttempts();
}
