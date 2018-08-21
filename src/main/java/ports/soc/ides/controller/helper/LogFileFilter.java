package ports.soc.ides.controller.helper;

import java.io.File;
import java.io.FileFilter;

import ports.soc.ides.logger.LoggerConfigurationFactory;

public class LogFileFilter implements FileFilter {

	private static final String LOG_FILE_NAME_REGEX = LoggerConfigurationFactory.LOG_FILE_PREFIX + "-\\d{4}-\\d{2}-\\d{2}-\\d{1,}[.]log([.]zip){0,1}";
	
	@Override
	public boolean accept(File f) {
		if (!f.isFile()) {
			return false;
		}
		if (!f.getName().startsWith(LoggerConfigurationFactory.LOG_FILE_PREFIX)) {
			return false;
		}
		if (!f.getName().matches(LOG_FILE_NAME_REGEX) && !f.getName().equals(LoggerConfigurationFactory.ACTIVE_LOG_FILE_NAME)) {
			return false;
		}
		if (!f.canRead()) {
			return false;
		}
		if (f.isHidden()) {
			return false;
		}
		return true;
	}

}
