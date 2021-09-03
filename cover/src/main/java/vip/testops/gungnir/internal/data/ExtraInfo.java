package vip.testops.gungnir.internal.data;

public class ExtraInfo {
    private final String projectName;

    public String getProjectName() {
        return projectName;
    }

    public ExtraInfo(final String projectName) {
        this.projectName = projectName;
    }

    @Override
    public String toString() {
        return "ExtraInfo[" + projectName + "]";
    }
}
