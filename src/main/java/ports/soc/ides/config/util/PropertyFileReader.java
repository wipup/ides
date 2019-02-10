package ports.soc.ides.config.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * A wrapper class for reading property from file
 * @author WIPU
 *
 */
public class PropertyFileReader extends AbstractPropertyReader {

	public static final String PROPERTIES_FILE_EXTENSION = ".properties";
	
	private File propertySourceFile;
	private Properties prop;
	
	public PropertyFileReader(File source) {
		RuntimeException e = isValidPropertyFile(source);
		if (e != null) {
			throw e;
		}
		propertySourceFile = source;
	}
	
	protected IllegalArgumentException isValidPropertyFile(File source) {
		if (!source.getName().endsWith(PROPERTIES_FILE_EXTENSION)) {
			return new IllegalArgumentException("The input property file extension of " + source.getAbsolutePath() + " does not ends with " + PROPERTIES_FILE_EXTENSION);
		}
		return null;
	}
	
	protected Properties initPropertiesFromFile(File source) throws FileNotFoundException, IOException {
		Properties p = null;
		try (FileInputStream fis = new FileInputStream(source)){
			p = new Properties();
			p.load(fis);
		}
		return p;
	}
	
	@Override
	protected String readOneProperty(String name) {
		if (prop == null) {
			try {
				prop = initPropertiesFromFile(propertySourceFile);
			} catch (IOException e) {
				prop = null;
				throw new RuntimeException("Failed to read property of name: " + name, e);
			}
		}
		return prop.getProperty(name);
	}

	public File getPropertySourceFile() {
		return propertySourceFile;
	}
	
	public void setPropertySourceFile(File propertySourceFile) {
		this.propertySourceFile = propertySourceFile;
		prop = null;
	}

	@Override
	public String toString() {
		return "PropertiesFileReader [propertySourceFile=" + propertySourceFile + "]";
	}
}
