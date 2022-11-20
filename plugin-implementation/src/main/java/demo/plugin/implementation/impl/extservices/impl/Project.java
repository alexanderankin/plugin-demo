package demo.plugin.implementation.impl.extservices.impl;

import demo.plugin.implementation.impl.extservices.ExternalServicesConfiguration;

import java.util.Set;

/**
 * abstraction to decouple and clarify contract with Gradle API
 */
public interface Project {
    Set<Task> tasks();

    /**
     * @return runnable which starts the service
     */
    Runnable registerService(ExternalServicesConfiguration.ExternalService service);

    /**
     * gradle hack
     */
    void execute(String taskName);

    interface Task {
        String name();

        Class<?> type();

        void execute();

        /**
         * @param runnable callback to execute before a task starts
         */
        void doFirst(Runnable runnable);

        void doLast(Runnable runnable);

        void requireService(String name);

        /**
         * @param other add other task which as required to run <b>before</b> this task (add pre-condition dependency)
         */
        void needsBefore(String other);

        /**
         * @param other add other task which as required to <b>after</b> before this task (add post-condition dependency)
         */
        void needsAfter(String other);
    }
}
