package ports.soc.ides.exception;

public class IdesException extends RuntimeException {

	private static final long serialVersionUID = 9006158428163265933L;

	private String messageContent;
	private String messageHeader;
	
	public IdesException() {
		super();
	}

	public IdesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IdesException(String message, Throwable cause) {
		super(message, cause);
	}

	public IdesException(String message) {
		super(message);
	}

	public IdesException(Throwable cause) {
		super(cause);
	}

	public IdesException(String header, String content) {
		this.messageHeader = header;
		this.messageContent = content;
	}
	
	public String getMessageContent() {
		return messageContent;
	}

	public String getMessageHeader() {
		return messageHeader;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public void setMessageHeader(String messageHeader) {
		this.messageHeader = messageHeader;
	}

	
}
