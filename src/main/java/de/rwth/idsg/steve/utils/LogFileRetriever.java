package de.rwth.idsg.steve.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * @author Sevket Goekay <goekay@dbis.rwth-aachen.de>
 * @since 05.11.2015
 */
public final class LogFileRetriever {
	private static final Logger log = LoggerFactory.getLogger(LogFileRetriever.class);

    public static final LogFileRetriever INSTANCE = new LogFileRetriever();

    private List<Path> logPathList;
    private Random random = new Random();

    private LogFileRetriever() {
        logPathList = getActiveLogFilePaths();
    }

    public Optional<Path> getPath() {
        Path p;
        if (logPathList.isEmpty()) {
            p = null;
        } else if (logPathList.size() == 1) {
            p = logPathList.get(0);
        } else {
            p = rollTheDice();
        }
        return Optional.ofNullable(p);
    }

    public String getLogFilePathOrErrorMessage() {
        Optional<Path> p = getPath();
        if (p.isPresent()) {
            return p.get().toAbsolutePath().toString();
        } else {
            return getErrorMessage();
        }
    }

    public String getErrorMessage() {
        return "Not available";
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /**
     * If the user configured multiple file appenders, which log file should we choose?
     * Clearly, the only sane solution is rolling the dice.
     * Easter egg mode: On
     */
    private Path rollTheDice() {
        log.trace("Rolling the dice...");
        int index = random.nextInt(logPathList.size());
        return logPathList.get(index);
    }

    /**
     * We cannot presume that the default file name/location setting won't be changed by the user.
     * Therefore, we should be able to retrieve that info from the underlying logging mechanism
     * by iterating over appenders.
     */
    private List<Path> getActiveLogFilePaths() {
        /*
        ContextSelector selector = ((Log4jContextFactory) factory).getSelector();*/

        List<Path> fileNameList = new ArrayList<>();
        /*for (LoggerContext ctx : selector.getLoggerContexts()) {
            for (Appender appender : ctx.getConfiguration().getAppenders().values()) {
                String fileName = extractFileName(appender);
                if (fileName != null) {
                    fileNameList.add(Paths.get(fileName));
                }
            }
        }*/
        fileNameList.add(Paths.get("/var/log/tomcat8/catalina.out"));
        return fileNameList;
    }

    /**
     * File appender types do not share a "write-to-file" superclass.
     */
   /* private String extractFileName(Appender a) {
        if (a instanceof FileAppender) {
            return ((FileAppender) a).getFileName();

        } else if (a instanceof RollingFileAppender) {
            return ((RollingFileAppender) a).getFileName();

        } else if (a instanceof RollingRandomAccessFileAppender) {
            return ((RollingRandomAccessFileAppender) a).getFileName();

        } else if (a instanceof RandomAccessFileAppender) {
            return ((RandomAccessFileAppender) a).getFileName();

        } else if (a instanceof MemoryMappedFileAppender) {
            return ((MemoryMappedFileAppender) a).getFileName();

        } else {
            return null;
        }
    }*/
}
