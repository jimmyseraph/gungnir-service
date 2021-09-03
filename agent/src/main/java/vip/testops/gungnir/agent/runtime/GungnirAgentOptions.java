package vip.testops.gungnir.agent.runtime;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class GungnirAgentOptions {

    public static final String DESTFILE = "destfile";

    public static final String DEFAULT_DESTFILE = "jacoco.exec";

    public static final String APPEND = "append";

    public static final String INCLUDES = "includes";

    public static final String EXCLUDES = "excludes";

    public static final String EXCLCLASSLOADER = "exclclassloader";

    public static final String INCLBOOTSTRAPCLASSES = "inclbootstrapclasses";

    public static final String INCLNOLOCATIONCLASSES = "inclnolocationclasses";

    public static final String SESSIONID = "sessionid";

    public static final String PROJECTNAME = "projectname";

    public static final String DUMPONEXIT = "dumponexit";

    /**
     * Specifies the output mode. Default is {@link OutputMode#file}.
     *
     * @see OutputMode#file
     * @see OutputMode#tcpserver
     * @see OutputMode#tcpclient
     * @see OutputMode#none
     */
    public static final String OUTPUT = "output";

    private static final Pattern OPTION_SPLIT = Pattern
            .compile(",(?=[a-zA-Z0-9_\\-]+=)");

    public static enum OutputMode {
        file,
        tcpserver,
        tcpclient,
        none
    }

    /**
     * The IP address or DNS name the tcpserver binds to or the tcpclient
     * connects to. Default is defined by {@link #DEFAULT_ADDRESS}.
     */
    public static final String ADDRESS = "address";

    /**
     * Default value for the "address" agent option.
     */
    public static final String DEFAULT_ADDRESS = null;

    /**
     * The port the tcpserver binds to or the tcpclient connects to. In
     * tcpserver mode the port must be available, which means that if multiple
     * JaCoCo agents should run on the same machine, different ports have to be
     * specified. Default is defined by {@link #DEFAULT_PORT}.
     */
    public static final String PORT = "port";

    /**
     * Default value for the "port" agent option.
     */
    public static final int DEFAULT_PORT = 6300;

    /**
     * Specifies where the agent dumps all class files it encounters. The
     * location is specified as a relative path to the working directory.
     * Default is <code>null</code> (no dumps).
     */
    public static final String CLASSDUMPDIR = "classdumpdir";

    /**
     * Specifies whether the agent should expose functionality via JMX under the
     * name "org.jacoco:type=Runtime". Default is <code>false</code>.
     */
    public static final String JMX = "jmx";

    private static final Collection<String> VALID_OPTIONS = Arrays.asList(
            DESTFILE, APPEND, INCLUDES, EXCLUDES, EXCLCLASSLOADER, PROJECTNAME,
            INCLBOOTSTRAPCLASSES, INCLNOLOCATIONCLASSES, SESSIONID, DUMPONEXIT,
            OUTPUT, ADDRESS, PORT, CLASSDUMPDIR, JMX);

    private final Map<String, String> options;

    /**
     * New instance with all values set to default.
     */
    public GungnirAgentOptions() {
        this.options = new HashMap<>();
    }

    /**
     * New instance parsed from the given option string.
     *
     * @param optionstr
     *            string to parse or <code>null</code>
     */
    public GungnirAgentOptions(final String optionstr) {
        this();
        if (optionstr != null && optionstr.length() > 0) {
            for (final String entry : OPTION_SPLIT.split(optionstr)) {
                final int pos = entry.indexOf('=');
                if (pos == -1) {
                    throw new IllegalArgumentException(format(
                            "Invalid agent option syntax \"%s\".", optionstr));
                }
                final String key = entry.substring(0, pos);
                if (!VALID_OPTIONS.contains(key)) {
                    throw new IllegalArgumentException(
                            format("Unknown agent option \"%s\".", key));
                }

                final String value = entry.substring(pos + 1);
                setOption(key, value);
            }

            validateAll();
        }
    }

    /**
     * New instance read from the given {@link Properties} object.
     *
     * @param properties
     *            {@link Properties} object to read configuration options from
     */
    public GungnirAgentOptions(final Properties properties) {
        this();
        for (final String key : VALID_OPTIONS) {
            final String value = properties.getProperty(key);
            if (value != null) {
                setOption(key, value);
            }
        }
    }

    private void validateAll() {
        validatePort(getPort());
        getOutput();
    }

    private void validatePort(final int port) {
        if (port < 0) {
            throw new IllegalArgumentException("port must be positive");
        }
    }

    public String getProjectName(){
        return getOption(PROJECTNAME, "NoName");
    }

    public void setProjectName(final String projectName) {
        setOption(PROJECTNAME, projectName);
    }

    public String getDestfile() {
        return getOption(DESTFILE, DEFAULT_DESTFILE);
    }

    public void setDestfile(final String destfile) {
        setOption(DESTFILE, destfile);
    }

    public boolean getAppend() {
        return getOption(APPEND, true);
    }

    public void setAppend(final boolean append) {
        setOption(APPEND, append);
    }

    public String getIncludes() {
        return getOption(INCLUDES, "*");
    }

    public void setIncludes(final String includes) {
        setOption(INCLUDES, includes);
    }

    public String getExcludes() {
        return getOption(EXCLUDES, "");
    }

    public void setExcludes(final String excludes) {
        setOption(EXCLUDES, excludes);
    }

    public String getExclClassloader() {
        return getOption(EXCLCLASSLOADER, "sun.reflect.DelegatingClassLoader");
    }

    public void setExclClassloader(final String expression) {
        setOption(EXCLCLASSLOADER, expression);
    }

    public boolean getInclBootstrapClasses() {
        return getOption(INCLBOOTSTRAPCLASSES, false);
    }

    public void setInclBootstrapClasses(final boolean include) {
        setOption(INCLBOOTSTRAPCLASSES, include);
    }

    public boolean getInclNoLocationClasses() {
        return getOption(INCLNOLOCATIONCLASSES, false);
    }

    public void setInclNoLocationClasses(final boolean include) {
        setOption(INCLNOLOCATIONCLASSES, include);
    }

    public String getSessionId() {
        return getOption(SESSIONID, null);
    }

    public void setSessionId(final String id) {
        setOption(SESSIONID, id);
    }

    public boolean getDumpOnExit() {
        return getOption(DUMPONEXIT, true);
    }

    public void setDumpOnExit(final boolean dumpOnExit) {
        setOption(DUMPONEXIT, dumpOnExit);
    }

    public int getPort() {
        return getOption(PORT, DEFAULT_PORT);
    }

    public void setPort(final int port) {
        validatePort(port);
        setOption(PORT, port);
    }

    public String getAddress() {
        return getOption(ADDRESS, DEFAULT_ADDRESS);
    }

    public void setAddress(final String address) {
        setOption(ADDRESS, address);
    }

    public OutputMode getOutput() {
        final String value = options.get(OUTPUT);
        return value == null ? OutputMode.file : OutputMode.valueOf(value);
    }

    public void setOutput(final String output) {
        setOutput(OutputMode.valueOf(output));
    }

    public void setOutput(final OutputMode output) {
        setOption(OUTPUT, output.name());
    }

    public String getClassDumpDir() {
        return getOption(CLASSDUMPDIR, null);
    }

    public void setClassDumpDir(final String location) {
        setOption(CLASSDUMPDIR, location);
    }

    public boolean getJmx() {
        return getOption(JMX, false);
    }

    public void setJmx(final boolean jmx) {
        setOption(JMX, jmx);
    }

    private void setOption(final String key, final int value) {
        setOption(key, Integer.toString(value));
    }

    private void setOption(final String key, final boolean value) {
        setOption(key, Boolean.toString(value));
    }

    private void setOption(final String key, final String value) {
        options.put(key, value);
    }

    private String getOption(final String key, final String defaultValue) {
        final String value = options.get(key);
        return value == null ? defaultValue : value;
    }

    private boolean getOption(final String key, final boolean defaultValue) {
        final String value = options.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    private int getOption(final String key, final int defaultValue) {
        final String value = options.get(key);
        return value == null ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Generate required JVM argument based on current configuration and
     * supplied agent jar location.
     *
     * @param agentJarFile
     *            location of the JaCoCo Agent Jar
     * @return Argument to pass to create new VM with coverage enabled
     */
    public String getVMArgument(final File agentJarFile) {
        return format("-javaagent:%s=%s", agentJarFile, this);
    }

    /**
     * Generate required quoted JVM argument based on current configuration and
     * supplied agent jar location.
     *
     * @param agentJarFile
     *            location of the JaCoCo Agent Jar
     * @return Quoted argument to pass to create new VM with coverage enabled
     */
    public String getQuotedVMArgument(final File agentJarFile) {
        return CommandLineSupport.quote(getVMArgument(agentJarFile));
    }

    /**
     * Generate required quotes JVM argument based on current configuration and
     * prepends it to the given argument command line. If a agent with the same
     * JAR file is already specified this parameter is removed from the existing
     * command line.
     *
     * @param arguments
     *            existing command line arguments or <code>null</code>
     * @param agentJarFile
     *            location of the JaCoCo Agent Jar
     * @return VM command line arguments prepended with configured JaCoCo agent
     */
    public String prependVMArguments(final String arguments,
                                     final File agentJarFile) {
        final List<String> args = CommandLineSupport.split(arguments);
        final String plainAgent = format("-javaagent:%s", agentJarFile);
        args.removeIf(s -> s.startsWith(plainAgent));
        args.add(0, getVMArgument(agentJarFile));
        return CommandLineSupport.quote(args);
    }

    /**
     * Creates a string representation that can be passed to the agent via the
     * command line. Might be the empty string, if no options are set.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final String key : VALID_OPTIONS) {
            final String value = options.get(key);
            if (value != null) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(key).append('=').append(value);
            }
        }
        return sb.toString();
    }
}
