package com.gigu.engagement.domain.policy;
import com.gigu.engagement.domain.valueobject.ProjectStatus;
import java.util.Map;
import java.util.Set;
public class ProjectStatusPolicy {
    private static final Map<ProjectStatus, Set<ProjectStatus>> ALLOWED = Map.of(
            ProjectStatus.PENDING, Set.of(ProjectStatus.IN_PROGRESS, ProjectStatus.CANCELLED),
            ProjectStatus.IN_PROGRESS, Set.of(ProjectStatus.DELIVERED, ProjectStatus.CANCELLED),
            ProjectStatus.DELIVERED, Set.of(ProjectStatus.FINISHED),
            ProjectStatus.FINISHED, Set.of(),
            ProjectStatus.CANCELLED, Set.of()
    );
    public boolean canTransition(ProjectStatus from, ProjectStatus to){ return ALLOWED.getOrDefault(from, Set.of()).contains(to); }
}
