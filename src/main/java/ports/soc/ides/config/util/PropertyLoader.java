package ports.soc.ides.config.util;

import java.util.List;

/**
 * 
 * @author WIPU
 *
 */
public interface PropertyLoader {
	
	public String getOneStringProperty(String name);
	
	public String getOneStringProperty(String name, String defaultValue);
	
	public int getOneIntProperty(String name, int defaultValue);

	public long getOneLongProperty(String name, long defaultValue); 
	
	public boolean getOneBooleanProperty(String name, boolean defaultValue);

	public List<String> getManyStringProperties(String name, String delimiter);

	public List<String> getManyStringProperties(String name);
	
	public void setPropertyDelimiter(String listDelimiter);

}
