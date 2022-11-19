package demo.plugin.implementation.impl.extservices.gradle;

import lombok.NonNull;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

@SuppressWarnings("Convert2Lambda")
public class ExternalServicePlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        project.getExtensions().add("externalServices", ExternalServiceExtension.class);
        project.afterEvaluate(new Action<>() {
            @Override
            public void execute(@NonNull Project project) {
                ExternalServiceExtension ese = project.getExtensions().getByType(ExternalServiceExtension.class);
            }
        });
    }
}
