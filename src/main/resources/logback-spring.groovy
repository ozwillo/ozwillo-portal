scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        //pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
        pattern = "| %highlight(%-5level) | %d{HH:mm:ss.SSS} | %logger - %msg%n"
    }
}

// Portal
logger("org.oasis_eu", INFO)
logger("org.oasis_eu.portal.config.OasisLocaleResolver", DEBUG)
logger("org.oasis_eu.portal.main", INFO)
logger("org.oasis_eu.portal.services", DEBUG)

// Integration - kernel
logger("org.oasis_eu.spring", INFO)
logger("org.oasis_eu.spring.util.KernelLoggingInterceptor", DEBUG) // ERROR, WARN (prod), INFO (preprod, dev), DEBUG
logger("kernelLogging.logFullErrorResponses", DEBUG) // DEBUG logs any response, INFO only error ones
logger("kernelLogging.logRequestTimings", INFO)
logger("org.oasis_eu.spring.kernel.security.OasisAuthenticationFilter", INFO)

root(WARN, ["CONSOLE"])
