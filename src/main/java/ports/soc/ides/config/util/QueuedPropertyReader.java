package ports.soc.ides.config.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ports.soc.ides.util.IdesUtils;

/**
 * 
 * @author WIPU
 *
 */
public class QueuedPropertyReader extends AbstractPropertyReader {

	private List<PropertyLoader> propertyReaders;
	
	public QueuedPropertyReader() {
		propertyReaders = new ArrayList<>();
	}
	
	public QueuedPropertyReader(PropertyLoader[] configurators) {
		propertyReaders = Arrays.asList(configurators);
	}
	
	@Override
	protected String readOneProperty(String name) {
		String result = null;
		for(PropertyLoader c : propertyReaders) {
			result = c.getOneStringProperty(name);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public List<PropertyLoader> getPropertyReaders() {
		return propertyReaders;
	}
	
	public void setPropertyReaders(PropertyLoader[] configurators) {
		propertyReaders = Arrays.asList(configurators);
	}

	@Override
	public String toString() {
		return "PrioritisedPropertyReader [propertyReaders=" + IdesUtils.deepPrint(propertyReaders) + "]";
	}
	
	@Override
	public void setAutoTrimValue(boolean autoTrimValue) {
		super.setAutoTrimValue(autoTrimValue);
		for(PropertyLoader p : propertyReaders) {
			if (p instanceof AbstractPropertyReader) {
				((AbstractPropertyReader) p).setAutoTrimValue(autoTrimValue);
			}
		}
	}
	
	@Override
	public void setPropertyDelimiter(String listDelimiter) {
		super.setPropertyDelimiter(listDelimiter);
		for(PropertyLoader p : propertyReaders) {
			p.setPropertyDelimiter(listDelimiter);
		}
	}
}
