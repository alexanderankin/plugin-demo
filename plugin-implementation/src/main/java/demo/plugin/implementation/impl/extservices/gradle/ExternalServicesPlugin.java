package demo.plugin.implementation.impl.extservices.gradle;

import lombok.NonNull;
import org.gradle.api.*;

import java.util.stream.Collectors;

@SuppressWarnings("Convert2Lambda")
public class ExternalServicesPlugin implements Plugin<Project> {
    @Override
    public void apply(@NonNull Project project) {
        project.getExtensions().add("externalServices",
                ExternalServicesExtension.class);

        project.afterEvaluate(new Action<>() {
            @Override
            public void execute(@NonNull Project project) {
                ExternalServicesExtension ese = project.getExtensions().getByType(ExternalServicesExtension.class);
            }
        });

        project.getTasks().register("listServices").configure(new Action<>() {
            @Override
            public void execute(@NonNull Task task) {
                task.doLast(new Action<>() {
                    @Override
                    public void execute(@NonNull Task task) {
                        System.out.println(project.getExtensions().getByType(ExternalServicesExtension.class).getServices().stream().map(Object::toString).collect(Collectors.toList()));
                    }
                });
            }
        });
    }
}
