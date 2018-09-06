package ports.soc.ides.logger;

import java.net.URI;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.util.Strings;

import ports.soc.ides.config.InitialConfigurationPropertyName;

/**
 * 
 * Reference: https://logging.apache.org/log4j/2.x/manual/customconfig.html
 *
 */
@Plugin(name = "LoggerConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(Integer.MAX_VALUE)
public class LoggerConfigurationFactory extends ConfigurationFactory {

	public static final Level DEFAULT_LOG_LEVEL;
	
	public static final String LOG_SESSION_ATTRIBUTE_KEY = "sessionId";
	public static final String LOG_IP_ADDRESS_ATTRIBUTE_KEY = "ipAddress";
	
	public static final String LOG_FILE_PREFIX = "ides-log";
	public static final String ACTIVE_LOG_FILE_NAME = LOG_FILE_PREFIX + ".log";
	
	public static final String LOG_CHARSET = "UTF-8";
	
	public static final Level HUNT = Level.forName("HUNT", 700);
	
	public static boolean LOG_IP_ADDRESS = false;
	
	static {
		String level = System.getProperty(InitialConfigurationPropertyName.LOGGER_DEFAULT_LEVEL);
		Level defaultLevel = Level.INFO;
		if (level != null) {
			level = level.trim().toUpperCase();
			switch (level) {
			case "ERROR":
				defaultLevel = Level.ERROR;
				break;
			case "WARN":
				defaultLevel = Level.WARN;
				break;
			case "INFO":
				defaultLevel = Level.INFO;
				break;
			case "DEBUG":
				defaultLevel = Level.DEBUG;
				break;
			case "TRACE":
				defaultLevel = Level.TRACE;
				break;
			case "ALL":
				defaultLevel = Level.ALL;
				break;
			}
		}
		
		DEFAULT_LOG_LEVEL = defaultLevel;
	}
	
	public static Configuration createConfiguration(String name, ConfigurationBuilder<BuiltConfiguration> builder) {
		return createConfiguration(name, builder, DEFAULT_LOG_LEVEL);
	}

	@SuppressWarnings("rawtypes")
	public static Configuration createConfiguration(String name, ConfigurationBuilder<BuiltConfiguration> builder, Level logLevel) {
		System.out.println("Creating configuration for IDES Logger");
		System.out.println("Log file name = " + ACTIVE_LOG_FILE_NAME);
		System.out.println("Default Log level = " + DEFAULT_LOG_LEVEL.name());
		System.out.println("Desired Log level = " + logLevel.name());
		System.out.println("Log IP Address = " + LOG_IP_ADDRESS);
		
		builder.setConfigurationName(name);
		builder.setStatusLevel(logLevel);

		String logPattern = "%p [%X{" + LOG_SESSION_ATTRIBUTE_KEY + "}] %C{1.} - %m";
		if (LOG_IP_ADDRESS) {
			logPattern = "%p [%X{" + LOG_SESSION_ATTRIBUTE_KEY + "}][%X{" + LOG_IP_ADDRESS_ATTRIBUTE_KEY + "}] %C{1.} - %m";
		}
		
		AppenderComponentBuilder consoleAppenderBuilder = builder
				.newAppender("ConsoleAppender", "CONSOLE").addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
		consoleAppenderBuilder.add(builder.newLayout("PatternLayout").addAttribute("pattern", logPattern).addAttribute("charset", LOG_CHARSET));
		builder.add(consoleAppenderBuilder);

		LayoutComponentBuilder fileAppenderLayoutBuilder = builder
				.newLayout("PatternLayout").addAttribute("pattern", "%d{DEFAULT} " + logPattern + "%n").addAttribute("charset", LOG_CHARSET);
		ComponentBuilder fileAppenderTriggeringPolicy = builder.newComponent("Policies").addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "100 MB"))
				.addComponent(builder.newComponent("TimeBasedTriggeringPolicy").addAttribute("interval", "1").addAttribute("modulate", "true"));

		AppenderComponentBuilder fileAppenderBuilder = builder.newAppender("RollingFileAppender", "RollingFile")
				.add(fileAppenderLayoutBuilder)
				.addComponent(fileAppenderTriggeringPolicy);
		
		final String logOutputLocation = System.getProperty(InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION);
		if (Strings.isNotBlank(logOutputLocation)) {
			fileAppenderBuilder.addAttribute("fileName", "${sys:"  + InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION + "}/" + ACTIVE_LOG_FILE_NAME)
				.addAttribute("filePattern", "${sys:" + InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION + "}/" + LOG_FILE_PREFIX + "-%d{yyyy-MM-dd}-%i.log.zip");
		} else {
			System.out.println("Attribute " + InitialConfigurationPropertyName.LOGGER_OUTPUT_LOCATION + " is not set. Use default IDES logger");
			fileAppenderBuilder.addAttribute("fileName", ACTIVE_LOG_FILE_NAME)
				.addAttribute("filePattern", LOG_FILE_PREFIX + "-%d{yyyy-MM-dd}-%i.log.zip");
		}
		
		builder.add(fileAppenderBuilder)
				.add(builder.newRootLogger(logLevel)
				.add(builder.newAppenderRef("ConsoleAppender"))
				.add(builder.newAppenderRef("RollingFileAppender")));

		return builder.build();
	}

	@Override
	protected String[] getSupportedTypes() {
		return new String[] { "*" };
	}

	@Override
	public Configuration getConfiguration(LoggerContext loggerContext, String name, URI configLocation) {
		ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
		return createConfiguration(name, builder);
	}

	@Override
	public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
		return getConfiguration(loggerContext, String.valueOf(source), null);
	}
}
