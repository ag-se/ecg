package utmj.threaded;

import java.util.*;
import junit.framework.*;

public class ConcurrentTestCaseTest extends ConcurrentTestCase {
	private ConcurrentTestCase concurrentTest;

public ConcurrentTestCaseTest(String name) {
	super(name);
}

/**
 * Invoked by testErrorInThread()
 */
public void errorInThread() throws Exception {
	Runnable runnable = new Runnable() {
		public void run() {
			throw new RuntimeException("Provoked error");
		}
	};
	this.addThread("thread", runnable);
	this.startAndJoinAllThreads(); // Thread with error should return on its own
	assertTrue("No deadlock", !this.deadlockDetected());
	assertTrue("Thread not finished", !this.hasThreadFinished("thread"));
}


/**
 * Invoked by testFailureInThread()
 */
public void failureInThread() throws Exception {
	Runnable runnable = new Runnable() {
		public void run() {
			fail("Must fail");
		}
	};
	this.addThread("thread", runnable);
	this.startAndJoinAllThreads(); // Failing thread should return on its own
	assertTrue("No deadlock", !this.deadlockDetected());
	assertTrue("Thread not finished", !this.hasThreadFinished("thread"));
}


public void testCheckpoints() {
	Runnable runnable = new Runnable() {
		public void run() {
			checkpoint("cp1");
		}
	};
	this.addThread("thread", runnable);
	this.startAndJoinThreads(100);
	assertTrue("checkpoint 1", this.checkpointReached("cp1"));
	assertTrue("checkpoint 2", ! this.checkpointReached("cp2"));
}


public void testCheckpointsInSeveralThreads() {
	Runnable runnable1 = new Runnable() {
		public void run() {
			checkpoint("cp1.1");
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			checkpoint("cp2.1");
			boolean alwaysTrue = true;
			while(alwaysTrue) {}
			checkpoint("cp2.2");
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	this.startAndJoinThreads(100);
	assertTrue("checkpoint 1.1", this.checkpointReached("cp1.1"));
	assertTrue("checkpoint 1.2", ! this.checkpointReached("cp1.2"));
	assertTrue("checkpoint 2.1", this.checkpointReached("cp2.1"));
	assertTrue("checkpoint 2.2", ! this.checkpointReached("cp2.2"));
}


public void testErrorInThread() throws Exception {
	TestResult result = new TestResult();
	ConcurrentTestCaseTest test = new ConcurrentTestCaseTest("errorInThread");
	test.run(result);
	assertEquals(1, result.runCount());
	assertEquals(0, result.failureCount());
	assertEquals(1, result.errorCount());
}


public void testFailureInThread() throws Exception {
	TestResult result = new TestResult();
	ConcurrentTestCaseTest test = new ConcurrentTestCaseTest("failureInTest");
	test.run(result);
	assertEquals(1, result.runCount());
	assertEquals(1, result.failureCount());
	assertEquals(0, result.errorCount());
}


public void testJoinThread() throws InterruptedException {
	Runnable runnable1 = new Runnable() {
		public void run() {
			sleep(10);
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			sleep(10000);
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	this.startThreads();
	this.joinThread("thread1");
	assertTrue(hasThreadFinished("thread1"));
	this.joinThread("thread2", 100); // Will not wait till end
	assertTrue(!hasThreadFinished("thread2"));

	try {
		this.joinThread("xyz");
		fail("Failure Expected");
	} catch (AssertionFailedError expected) {
	}
}


public void testJoinThreadWithJoinAll() throws InterruptedException {
	Runnable runnable1 = new Runnable() {
		public void run() {
			sleep(10);
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			sleep(10000);
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	this.startThreads();
	this.joinThread("thread1");
	assertTrue(hasThreadFinished("thread1"));
	this.joinAllThreads(100); // Will not wait till end
	assertTrue(!hasThreadFinished("thread2"));
}


public void testRunningThreads() {
	final Vector signals = new Vector();
	Runnable runnable1 = new Runnable() {
		public void run() {
			signals.addElement("thread1");
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			signals.addElement("thread2");
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	assertTrue("Thread 1 not finished", ! this.hasThreadFinished("thread1"));
	assertTrue("Thread 1 not started", ! this.hasThreadStarted("thread1"));
	assertTrue("Thread 2 not finished", ! this.hasThreadFinished("thread2"));
	assertTrue("Thread 2 not started", ! this.hasThreadStarted("thread2"));
	this.startAndJoinAllThreads();
	assertTrue("Thread 1 has really run", signals.contains("thread1"));
	assertTrue("Thread 2 has really run", signals.contains("thread2"));
	assertTrue("Thread 1 finished", this.hasThreadFinished("thread1"));
	assertTrue("Thread 1 started", this.hasThreadStarted("thread1"));
	assertTrue("Thread 2 finished", this.hasThreadFinished("thread2"));
	assertTrue("Thread 2 started", this.hasThreadStarted("thread2"));
	assertTrue("No deadlock", !this.deadlockDetected());
}


public void testRunningThreadsWithDeadlock() {
	Runnable runnable1 = new Runnable() {
		public void run() {
			sleep(100);
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			try {
				synchronized (this) {
					this.wait();
				}
			} catch (InterruptedException ignore) {}
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	this.startAndJoinThreads(200);
	assertTrue("Deadlock occurred", this.deadlockDetected());
	assertTrue("Thread 1 started", this.hasThreadStarted("thread1"));
	assertTrue("Thread 1 finished", this.hasThreadFinished("thread1"));
	assertTrue("Thread 2 started", this.hasThreadStarted("thread2"));
	assertTrue("Thread 2 not finished", !this.hasThreadFinished("thread2"));
}


public void testTimeout() {
	Runnable runnable = new Runnable() {
		public void run() {
			sleep(10000);
		}
	};
	this.addThread("thread", runnable);
	this.startAndJoinThreads(100);
	assertTrue("Deadlock occurred", this.deadlockDetected());
	assertTrue("Thread started", this.hasThreadStarted("thread"));
	assertTrue("Thread not finished", ! this.hasThreadFinished("thread"));
}


public void testWaitForCheckpoint() throws Exception {
	Runnable runnable1 = new Runnable() {
		public void run() {
			assertTrue(!checkpointReached("cp2"));
			checkpoint("cp1.1");
			waitForCheckpoint("cp2");
			assertTrue(checkpointReached("cp2"));
			checkpoint("cp1.2");
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			waitForCheckpoint("cp1.1");
			assertTrue(checkpointReached("cp1.1"));
			checkpoint("cp2");
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	this.startAndJoinThreads(500);
	assertTrue("No deadlock", !this.deadlockDetected());
	assertTrue("Thread 1 finished", this.hasThreadFinished("thread1"));
	assertTrue("Thread 2 finished", this.hasThreadFinished("thread2"));
}


public void testWaitForCheckpointInMainThread() throws Exception {
	Runnable runnable = new Runnable() {
		public void run() {
			assertTrue("go not reached", !checkpointReached("go"));
			waitForCheckpoint("go");
			assertTrue("go reached", checkpointReached("go"));
		}
	};
	this.addThread("thread1", runnable);
	this.addThread("thread2", runnable);
	this.startThreads();
	this.checkpoint("go");
	this.joinAllThreads(1000);
	assertTrue("No deadlock", !this.deadlockDetected());
	assertTrue("Thread 1 finished", this.hasThreadFinished("thread1"));
	assertTrue("Thread 2 finished", this.hasThreadFinished("thread2"));
}


public void testWaitUntilFinished() throws Exception {
	Runnable runnable1 = new Runnable() {
		public void run() {
			waitForCheckpoint("cp2");
			sleep(100);
		}
	};
	Runnable runnable2 = new Runnable() {
		public void run() {
			sleep(100);
			assertTrue(!hasThreadFinished("thread1"));
			checkpoint("cp2");
			waitUntilFinished("thread1");
			assertTrue(hasThreadFinished("thread1"));
		}
	};
	this.addThread("thread1", runnable1);
	this.addThread("thread2", runnable2);
	this.startAndJoinThreads(1500);
	assertTrue("No deadlock", !this.deadlockDetected());
	assertTrue("Thread 1 finished", this.hasThreadFinished("thread1"));
	assertTrue("Thread 2 finished", this.hasThreadFinished("thread2"));
}
}