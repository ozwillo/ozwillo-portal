import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
        pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

logger("org.oasis_eu.portal.main", DEBUG)
logger("org.oasis_eu", DEBUG)
logger("org.oasis_eu.portal.config.OasisLocaleResolver", DEBUG)
logger("org.oasis_eu.spring.util.KernelLoggingInterceptor", ERROR) // ERROR, WARN (prod), INFO (preprod, dev), DEBUG
logger("kernelLogging.logFullErrorResponses", DEBUG) // TODO WILL CHANGE requires org.oasis_eu.spring.util.KernelLoggingInterceptor at DEBUG
logger("kernelLogging.logRequestTimings", DEBUG)
logger("org.oasis_eu.portal.front.my.network", DEBUG) // ONLY in dev mode



root(WARN, ["CONSOLE"])
