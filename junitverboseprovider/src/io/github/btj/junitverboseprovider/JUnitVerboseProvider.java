package io.github.btj.junitverboseprovider;

import java.util.Objects;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

class TestRun {
	final String test;
	int runningTime;
	
	TestRun(String test) {
		this.test = test;
	}
}

public class JUnitVerboseProvider implements TestExecutionListener {
	
	private TestRun run;
	
	private synchronized void setCurrentRun(TestRun run) {
		timerThread.interrupt();
		this.run = run;
	}
	
	private synchronized TestRun getCurrentRun() {
		Thread.interrupted(); // Clear interrupted flag
		return run;
	}
	
	private synchronized void testRanForFiveSeconds(TestRun run) {
		if (this.run != null && this.run == run) {
			run.runningTime += 5;
			System.out.println("Test " + run.test + " has been running for " + run.runningTime + " seconds");
		}
	}
	
	private Thread timerThread = new Thread() {
		public void run() {
			for (;;) {
				try {
					TestRun run = getCurrentRun();
					if (run == null)
						Thread.sleep(Integer.MAX_VALUE);
					else {
						Thread.sleep(5000);
						testRanForFiveSeconds(run);
					}
				} catch (InterruptedException e) {}
			}
		}
	};
	
	{
		timerThread.setDaemon(true);
		timerThread.start();
	}
	
	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isTest()) {
			String test = testIdentifier.getUniqueId();
			setCurrentRun(new TestRun(test));
		}
	}
	
	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		setCurrentRun(null);
	}
}
