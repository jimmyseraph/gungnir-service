package vip.testops.gungnir.services;

import lombok.extern.slf4j.Slf4j;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.IExecutionDataVisitor;
import org.jacoco.core.data.ISessionInfoVisitor;
import org.jacoco.core.data.SessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vip.testops.gungnir.dao.ProjectRepository;
import vip.testops.gungnir.dao.RuntimeRepository;
import vip.testops.gungnir.internal.data.ExtraInfo;
import vip.testops.gungnir.internal.data.GungnirExecutionDataPersist;
import vip.testops.gungnir.internal.data.GungnirExecutionDataWriter;
import vip.testops.gungnir.internal.data.IExtraInfoVisitor;
import vip.testops.gungnir.internal.runtime.GungnirRemoteControlReader;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

@Service
@Slf4j
public class ExecutionDataService {

    private ProjectRepository projectRepository;
    private RuntimeRepository runtimeRepository;

    @Value("${socket.port}")
    private int socketPort;

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Autowired
    public void setRuntimeRepository(RuntimeRepository runtimeRepository) {
        this.runtimeRepository = runtimeRepository;
    }

    public void receiver() throws IOException {
//        String execFilename = DEST_FILE_PREFIX + "-" + new Date().getTime() + ".exec";
//        final ExecutionDataWriter fileWriter = new ExecutionDataWriter(new FileOutputStream(execFilename));
        final ServerSocket server = new ServerSocket(socketPort);
        while (true) {
            Socket client = server.accept();
            final  Handler handler = new Handler(client, runtimeRepository, projectRepository);
            new Thread(handler).start();
        }
    }

    private static class Handler implements Runnable, ISessionInfoVisitor, IExecutionDataVisitor, IExtraInfoVisitor {

        private final Socket socket;

        private final GungnirRemoteControlReader reader;

        private final GungnirExecutionDataPersist writer;

        Handler(final Socket socket, RuntimeRepository runtimeRepository, ProjectRepository projectRepository) throws  IOException {
            this.socket = socket;
            this.writer = new GungnirExecutionDataPersist(runtimeRepository, projectRepository, this.socket.getInetAddress().getHostAddress());
            new GungnirExecutionDataWriter(socket.getOutputStream());

            reader = new GungnirRemoteControlReader(socket.getInputStream());
            reader.setExtraInfoVisitor(this);
            reader.setSessionInfoVisitor(this);
            reader.setExecutionDataVisitor(this);
        }

        @Override
        public void run() {
            try {
                while(reader.read()){
                    log.info("read data: {}", reader);
                }
                socket.close();
                synchronized (writer) {
                    writer.stop();
                }
            } catch (IOException e) {
                log.error("read data error.", e);
            }
        }

        @Override
        public void visitClassExecution(ExecutionData executionData) {
            synchronized (writer) {
                writer.visitClassExecution(executionData);
            }
        }

        @Override
        public void visitSessionInfo(SessionInfo sessionInfo) {
            log.info("Retrieving execution Data for session: {}", sessionInfo.getId());
            synchronized (writer) {
                writer.visitSessionInfo(sessionInfo);
            }
        }

        @Override
        public void visitExtraInfo(ExtraInfo info) {
            log.info("Retrieving project name: {}", info.getProjectName());
            synchronized (writer) {
                writer.visitExtraInfo(info);
            }
        }
    }

}
