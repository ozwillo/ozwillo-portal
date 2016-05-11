import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
        pattern = "| %highlight(%-5level) | %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

//Portal
logger("org.oasis_eu", DEBUG)
logger("org.oasis_eu.portal.main", DEBUG)
logger("org.oasis_eu.portal.config.OasisLocaleResolver", DEBUG)
logger("org.oasis_eu.portal.front.my.network", DEBUG) // ONLY in dev mode

//Integration - kernel
logger("org.oasis_eu.spring.util.KernelLoggingInterceptor", INFO) // ERROR, WARN (prod), INFO (preprod, dev), DEBUG
logger("kernelLogging.logFullErrorResponses", INFO) // DEBUG logs any response, INFO only error ones
logger("kernelLogging.logRequestTimings", DEBUG)
logger("org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter", INFO)

root(WARN, ["CONSOLE"])
