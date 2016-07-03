package net.shadowfacts.baymax.command.exception

/**
 * @author shadowfacts
 */
class CommandException extends RuntimeException {

	CommandException() {
	}

	CommandException(String msg) {
		super(msg)
	}

	CommandException(String msg, Throwable t) {
		super(msg, t)
	}

	CommandException(Throwable t) {
		super(t)
	}

}
