package ports.soc.ides.config.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author WIPU
 *
 */
public abstract class AbstractPropertyReader implements PropertyLoader {

	protected String listDelimiter = ",";
	protected boolean autoTrimValue = true;

	abstract protected String readOneProperty(String name);

	public String getOneStringProperty(String name) {
		String result = readOneProperty(name);
		if (autoTrimValue && result != null) {
			result = result.trim();
		}
		return result;
	}

	@Override
	public List<String> getManyStringProperties(String name, String delimiter) {
		List<String> result = new ArrayList<String>();
		String rawResult = getOneStringProperty(name);

		if (rawResult == null) {
			return result;
		}
		
		String[] frags = rawResult.split(delimiter);
		for (int i = 0; i < frags.length; i++) {
			String f = frags[i];
			if (autoTrimValue) {
				f = f.trim();
			}
			if (f.isEmpty()) {
				continue;
			}
			result.add(f);
		}
		
		return result;
	}

	@Override
	public List<String> getManyStringProperties(String name) {
		return getManyStringProperties(name, listDelimiter);
	}

	@Override
	public String getOneStringProperty(String name, String defaultValue) {
		String result = getOneStringProperty(name);
		return result != null ? result : defaultValue;
	}

	@Override
	public int getOneIntProperty(String name, int defaultValue) {
		String result = getOneStringProperty(name);
		try {
			if (result != null) {
				return Integer.parseInt(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}

	@Override
	public long getOneLongProperty(String name, long defaultValue) {
		String result = getOneStringProperty(name);
		try {
			if (result != null) {
				return Long.parseLong(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defaultValue;
	}
	
	@Override
	public boolean getOneBooleanProperty(String name, boolean defaultValue) {
		String result = getOneStringProperty(name);
		if (result == null) {
			return defaultValue;
		}
		result = result.toUpperCase();
		if (result.equals("TRUE") || result.equals("1") || result.equals("YES")) {
			return true;
		} else if (result.equals("FALSE") || result.equals("0") || result.equals("NO")) {
			return false;
		}
		return false;
	}

	public String getListDelimiter() {
		return listDelimiter;
	}
	
	@Override
	public void setPropertyDelimiter(String listDelimiter) {
		this.listDelimiter = listDelimiter;		
	}

	public boolean isAutoTrimValue() {
		return autoTrimValue;
	}

	public void setAutoTrimValue(boolean autoTrimValue) {
		this.autoTrimValue = autoTrimValue;
	}

}
