package ports.soc.ides.controller.helper.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.controller.AdministratorPanelController;
import ports.soc.ides.controller.helper.FileWrapper;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@FacesConverter("logFileWrapperConverter")
public class LogFileWrapperConverter implements Converter<FileWrapper> {

	private static final Logger log = LogManager.getRootLogger();

	@Override
	public FileWrapper getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			if (IdesUtils.isEmpty(value)) {
				return null;
			}
			
			int index = Integer.parseInt(value);
			AdministratorPanelController apc = FacesUtils.getNamedController(AdministratorPanelController.class, context);
			
			return apc.getLogFiles().get(index);
		} catch (Exception e) {
			log.error("LogFileWrapperConverter: error converting value to file: value=" + value, e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, FileWrapper value) {
		if (value == null) {
			return null;
		}
		try {
			AdministratorPanelController apc = FacesUtils.getNamedController(AdministratorPanelController.class, context);
			return String.valueOf(apc.getLogFiles().indexOf(value));			
		} catch (Exception e) {
			log.error("LogFileWrapperConverter. error converting file to string, value=" + value, e);
		}
		return null;
	}

}
