package ports.soc.ides.config.util;

/**
 * 
 * @author WIPU
 *
 */
public class JVMPropertyReader extends AbstractPropertyReader {

	@Override
	protected String readOneProperty(String name) {
		return System.getProperty(name);
	}

}
