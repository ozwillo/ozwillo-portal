import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

scan()

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "| %-5level| %d{HH:mm:ss.SSS} | %logger{36} - %msg%n"
    }
}

logger("org.springframework", INFO)
logger("org.oasis_eu", DEBUG)
logger("org.oasis_eu.portal.config.OasisLocaleResolver", INFO)

root(WARN, ["CONSOLE"])
