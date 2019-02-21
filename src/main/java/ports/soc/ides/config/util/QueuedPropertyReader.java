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
	private boolean reverse = false;
	
	public QueuedPropertyReader() {
		propertyReaders = new ArrayList<>();
	}
	
	public QueuedPropertyReader(PropertyLoader[] configurators) {
		propertyReaders = Arrays.asList(configurators);
	}
	
	@Override
	protected String readOneProperty(String name) {
		if (reverse) {
			reverse = false;
			return readOnePropertyFromTail(name);
		}
		return readOnePropertyFromHead(name);
	}
	
	private String readOnePropertyFromHead(String name) {
		String result = null;
		for(PropertyLoader p : propertyReaders) {
			result = p.getOneStringProperty(name);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	private String readOnePropertyFromTail(String name) {
		String result = null;
		for(int i = propertyReaders.size() - 1; i >= 0; i--) {
			PropertyLoader p = propertyReaders.get(i);
			result = p.getOneStringProperty(name);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	/**
	 * Reverse queue
	 * @return
	 */
	public QueuedPropertyReader fromLast() {
		reverse = true;
		return this;
	}

	public List<PropertyLoader> getPropertyReaders() {
		return propertyReaders;
	}
	
	public void addToQueue(PropertyLoader prop) {
		propertyReaders.add(prop);
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
