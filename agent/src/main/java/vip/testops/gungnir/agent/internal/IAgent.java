package vip.testops.gungnir.agent.internal;

import java.io.IOException;

public interface IAgent {
    /**
     * Returns version of JaCoCo.
     *
     * @return version of JaCoCo
     */
    String getVersion();

    /**
     * Returns current a session identifier.
     *
     * @return current session identifier
     */
    String getSessionId();

    /**
     * Sets a session identifier.
     *
     * @param id
     *            new session identifier
     */
    void setSessionId(String id);

    String getProjectName();

    void setProjectName(String projectName);

    /**
     * Resets all coverage information.
     */
    void reset();

    /**
     * Returns current execution data.
     *
     * @param reset
     *            if <code>true</code> the current execution data is cleared
     *            afterwards
     * @return dump of current execution data in JaCoCo binary format
     */
    byte[] getExecutionData(boolean reset);

    /**
     * Triggers a dump of the current execution data through the configured
     * output.
     *
     * @param reset
     *            if <code>true</code> the current execution data is cleared
     *            afterwards
     * @throws IOException
     *             if the output can't write execution data
     */
    void dump(boolean reset) throws IOException;
}
