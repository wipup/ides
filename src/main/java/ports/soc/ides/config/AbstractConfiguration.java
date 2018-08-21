package ports.soc.ides.config;

import java.io.File;
import java.io.Serializable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.config.util.PropertyFileReader;
import ports.soc.ides.config.util.PropertyLoader;

public abstract class AbstractConfiguration implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;

	protected static final Logger log = LogManager.getRootLogger();
	
	@Override
	abstract public AbstractConfiguration clone();
	
	abstract public void load(PropertyLoader loader);
	
	public void load(File f) {
		PropertyFileReader loader = new PropertyFileReader(f);
		load(loader);
	}
}
