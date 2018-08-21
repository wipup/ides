package ports.soc.ides.controller.helper.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.util.IdesUtils;

@FacesConverter("logLevelConveter")
public class LogLevelConverter implements Converter<Level> {

	private static final Logger log = LogManager.getRootLogger();

	@Override
	public Level getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			if (IdesUtils.isEmpty(value)) {
				return null;
			}
			return Level.valueOf(value);
		} catch (Exception e) {
			log.error("LogLevelConverter Error getAsObject", e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Level value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}

}
