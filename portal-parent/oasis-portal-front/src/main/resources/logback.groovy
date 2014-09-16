import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
        pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

logger("org.oasis_eu.portal.main", INFO)
logger("org.oasis_eu", INFO)
logger("org.oasis_eu.portal.services", DEBUG)
logger("org.oasis_eu.portal.core.dao", DEBUG)
logger("org.oasis_eu.portal.config.OasisLocaleResolver", INFO)
logger("org.oasis_eu.spring.kernel.security.OpenIdCAuthFilter", INFO)
//logger("org.oasis_eu.spring.util.KernelLoggingInterceptor", DEBUG)
// logger("kernelLogging.logFullErrorResponses", DEBUG)
logger("kernelLogging.logRequestTimings", DEBUG)

root(WARN, ["CONSOLE"])
