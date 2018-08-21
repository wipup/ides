package ports.soc.ides.config;

import ports.soc.ides.exception.IdesException;

public class IdesConfigurationException extends IdesException {

	private static final long serialVersionUID = 190166890994963189L;
	

	public IdesConfigurationException() {
		super();
	}

	public IdesConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IdesConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdesConfigurationException(String message) {
		super(message);
	}

	public IdesConfigurationException(Throwable cause) {
		super(cause);
	}


}
