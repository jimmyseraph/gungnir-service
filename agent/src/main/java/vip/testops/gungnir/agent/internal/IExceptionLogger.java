package vip.testops.gungnir.agent.internal;

public interface IExceptionLogger {
    IExceptionLogger SYSTEM_ERR = new IExceptionLogger() {
        public void logExeption(final Exception ex) {
            ex.printStackTrace();
        }
    };

    void logExeption(Exception ex);
}
