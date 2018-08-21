package ports.soc.ides.controller.helper.converter;

import java.sql.Driver;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.controller.AdministratorPanelController;
import ports.soc.ides.util.FacesUtils;
import ports.soc.ides.util.IdesUtils;

@FacesConverter("adminJdbcDriverConveter")
public class JdbcDriverConverter implements Converter<Driver> {

	private static final Logger log = LogManager.getRootLogger();

	@Override
	public Driver getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			if (IdesUtils.isEmpty(value)) {
				return null;
			}
			int index = Integer.parseInt(value);
			AdministratorPanelController admin = FacesUtils.getNamedController(AdministratorPanelController.class, context);

			return admin.getDriverList().get(index);
		} catch (Exception e) {
			log.error("JdbcDriverConverter Error converting input to driver, value=" + value, e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Driver driver) {
		try {
			if (driver == null) {
				return null;
			}

			AdministratorPanelController admin = FacesUtils.getNamedController(AdministratorPanelController.class, context);
			int index = admin.getDriverList().indexOf(driver);

			return String.valueOf(index);
		} catch (Exception e) {
			log.error("JdbcDriverConverter Error converting driver to string, driver=" + driver, e);
		}
		return null;
	}

}
