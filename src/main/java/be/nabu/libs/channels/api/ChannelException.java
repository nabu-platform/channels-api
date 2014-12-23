package be.nabu.libs.channels.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelException extends Exception {

	private static final long serialVersionUID = 1072452438832380010L;
	private List<Exception> suppressedExceptions = new ArrayList<Exception>();

	public ChannelException() {
		super();
	}

	public ChannelException(String message, Throwable cause) {
		super(message, cause);
	}

	public ChannelException(String message) {
		super(message);
	}

	public ChannelException(Throwable cause) {
		super(cause);
	}
	
	public ChannelException(Exception...suppressed) {
		super("Combined channel exception");
		this.suppressedExceptions.addAll(Arrays.asList(suppressed));
	}

	public List<Exception> getSuppressedExceptions() {
		return suppressedExceptions;
	}

	public void addSuppressedException(Exception e) {
		this.suppressedExceptions.add(e);
	}
}
