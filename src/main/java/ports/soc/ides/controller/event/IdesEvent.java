package ports.soc.ides.controller.event;

import java.util.HashMap;
import java.util.Map;

import ports.soc.ides.controller.AbstractIdesController;
import ports.soc.ides.util.IdesUtils;

/**
 * For CDI Bean event bus
 * @author WIPU
 *
 */
public class IdesEvent {

	protected Class<? extends AbstractIdesController> eventSource;
	protected boolean eventSuccess = true;
	protected Map<Object, Object> values;
	protected String description;
	
	public Object put(Object key, Object value) {
		if (values == null) {
			values = new HashMap<>();
		}
		return values.put(key, value);
	}
	
	public Object get(Object key) {
		if (values == null) {
			return null;
		}
		return values.get(key);
	}

	public boolean isEventSuccess() {
		return eventSuccess;
	}

	public void setEventSuccess(boolean eventResult) {
		this.eventSuccess = eventResult;
	}

	public Class<? extends AbstractIdesController> getSource() {
		return eventSource;
	}

	public void setEventSource(AbstractIdesController eventSource) {
		setEventSource(eventSource.getClass());
	}
	
	public void setEventSource(Class<? extends AbstractIdesController> eventSource) {
		this.eventSource = eventSource;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("IdesEvent[eventSource=").append( eventSource == null ? "null" : eventSource.getClass().getSimpleName() )
		.append(", eventSuccess=").append( eventSuccess )
		.append(", values=").append( IdesUtils.deepPrint(values) )
		.append(", description=").append( description )
		.append(", eventType=").append( this.getClass().getSimpleName() ).append("]");
		return sb.toString();
	}
	
	

}
