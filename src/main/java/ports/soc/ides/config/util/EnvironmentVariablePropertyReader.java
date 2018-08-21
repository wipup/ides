package ports.soc.ides.config.util;

/**
 * 
 * @author WIPU
 *
 */
public class EnvironmentVariablePropertyReader extends AbstractPropertyReader {

	@Override
	protected String readOneProperty(String name) {
		return System.getenv(name);
	}

}
