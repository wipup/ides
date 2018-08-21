package ports.soc.ides.controller.helper.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ports.soc.ides.controller.CreateIdeaWizardController;
import ports.soc.ides.model.Organisation;
import ports.soc.util.FacesUtils;
import ports.soc.util.IdesUtils;

@FacesConverter("orgConverter")
public class OrganisationForCreateIdeaWizardConverter implements Converter<Organisation> {

	private static final Logger log = LogManager.getRootLogger();

	@Override
	public Organisation getAsObject(FacesContext context, UIComponent component, String value) {
		try {
			if (IdesUtils.isEmpty(value)) {
				return null;
			}
			
			int index = Integer.parseInt(value);
			CreateIdeaWizardController ciw = FacesUtils.getNamedController(CreateIdeaWizardController.class, context);
			
			return ciw.getOrgs().get(index);
		} catch (Exception e) {
			log.error("OrganisationForCreateIdeaWizardConverter getAsObject value=" + value, e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Organisation value) {
		if (value == null) {
			return null;
		}
		try {
			CreateIdeaWizardController ciw = FacesUtils.getNamedController(CreateIdeaWizardController.class, context);
			return String.valueOf(ciw.getOrgs().indexOf(value));
			
		} catch (Exception e) {
			log.error("OrganisationForCreateIdeaWizardConverter getAsString value=" + value, e);
		}
		return null;
	}

}
