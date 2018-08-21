package ports.soc.ides.model;

import java.io.Serializable;

/**
 * Nothing but Serializable for now
 * @author WIPU
 *
 */
public abstract class DataModel implements Serializable {

	private static final long serialVersionUID = 2675991266716800727L;

	abstract public String printDetail();
	
	public static final String printDetail(DataModel model) {
		if (model == null) {
			return "null";
		}
		return model.printDetail();
	}
}
