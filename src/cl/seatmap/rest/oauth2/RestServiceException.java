package cl.seatmap.rest.oauth2;

/**
 * 
 * @author philiptrannp
 *
 */
public class RestServiceException extends RuntimeException {
	private static final long serialVersionUID = -1L;

	public RestServiceException() {
		super();
	}

	public RestServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public RestServiceException(String detailMessage) {
		super(detailMessage);
	}

	public RestServiceException(Throwable throwable) {
		super(throwable);
	}
	
}
