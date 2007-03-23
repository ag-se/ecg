package utmj.threaded;

import junit.framework.*;

/** 
 * Copyright (c) 2001, <a href="mailto:john.link@gmx.net">Johannes Link</a>
 *
 * Assert that an uncaught exception is being thrown in main thread or sub threads.
 */
public abstract class ExceptionAssert {
	private Class exceptionType;
	private long millisecondsToTimeout;
	private Throwable caughtException = null;

public ExceptionAssert(Class exceptionType, long millisecondsToTimeout) {
	this.exceptionType = exceptionType;
	this.millisecondsToTimeout = millisecondsToTimeout;
}


public Throwable getCaughtException() {
	return caughtException;
}


private void handleCaughtException(Throwable ex) {
	if (caughtException == null) {
		setCaughtException(ex);
	}
}


public abstract void run();


private synchronized void setCaughtException(Throwable ex) {
	caughtException = ex;
	this.notify();
}


public void start() throws AssertionFailedError {
	ThreadGroup group = new ThreadGroup("exception assert") {
		public void uncaughtException(Thread t, Throwable ex) {
			handleCaughtException(ex);
		}
	};
	new Thread(group, "test thread") {
		public void run() {
			ExceptionAssert.this.run();
		}
	}
	.start();
	this.waitForException();
	if (caughtException == null) {
		Assert.fail("No Exception thrown");
	}
	if (!exceptionType.isAssignableFrom(caughtException.getClass())) {
		Assert.fail(
			"Expected "
				+ exceptionType.getName()
				+ " but caught "
				+ caughtException.toString());
	}

}


private synchronized void waitForException() {
	try {
		if (caughtException == null) {
			this.wait(millisecondsToTimeout);
		}
	} catch (InterruptedException ignore) {
	}
}
}