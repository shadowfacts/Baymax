package net.shadowfacts.baymax.command.exception

/**
 * @author shadowfacts
 */
class IllegalUsageException extends CommandException {

	IllegalUsageException() {
	}

	IllegalUsageException(String msg) {
		super(msg)
	}

	IllegalUsageException(String msg, Throwable t) {
		super(msg, t)
	}

	IllegalUsageException(Throwable t) {
		super(t)
	}

}
