import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender


appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
        pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

logger("org.oasis_eu", DEBUG)
//logger("org.oasis_eu.spring.datacore",INFO)
logger("org.oasis_eu.spring.util.KernelLoggingInterceptor", DEBUG) // ERROR, WARN (prod), INFO (preprod, dev), DEBUG
logger("kernelLogging.logFullErrorResponses", DEBUG) // DEBUG logs any response, INFO only error ones

root(WARN, ["CONSOLE"])
