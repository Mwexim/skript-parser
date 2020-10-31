package io.github.syst3ms.skriptparser.util;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadUtils {

	/**
	 * Run certain code once on a separate thread. The thread is shut down after the code ran.
	 * @param code the runnable that needs to be executed
	 */
	public static void runAsync(Runnable code) {
		ExecutorService executor = Executors.newCachedThreadPool();
		executor.submit(code);
		executor.shutdown();
	}

	/**
	 * Run certain code once after a certain delay. The thread is shut down after the code ran.
	 * @param code the runnable that needs to be executed
	 * @param duration the delay
	 */
	public static void runAfter(Runnable code, Duration duration) {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.schedule(code, duration.toMillis(), TimeUnit.MILLISECONDS);
		executor.shutdown();
	}

	/**
	 * Runs certain code periodically.
	 * @param code the runnable that needs to be executed
	 * @param duration the delay
	 */
	public static void runPeriodically(Runnable code, Duration duration) {
		runPeriodically(code, duration, duration);
	}

	/**
	 * Runs certain code periodically.
	 * @param code the runnable that needs to be executed
	 * @param initialDelay the initial delay
	 * @param duration the delay
	 */
	public static void runPeriodically(Runnable code, Duration initialDelay, Duration duration) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(code, initialDelay.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Runs certain code periodically but with a final bound.
	 * @param code the runnable that needs to be executed
	 * @param duration the delay
	 * @param maxTime the duration this thread will be opened in milliseconds
	 */
	public static void runPeriodicallyBounded(Runnable code, Duration duration, Duration maxTime) {
		runPeriodicallyBounded(code, duration, duration, maxTime);
	}

	/**
	 * Runs certain code periodically but with a final bound.
	 * @param code the runnable that needs to be executed
	 * @param initialDelay the initial delay
	 * @param duration the delay
	 * @param maxTime the duration this thread will be opened in milliseconds
	 */
	public static void runPeriodicallyBounded(Runnable code, Duration initialDelay, Duration duration, Duration maxTime) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(code, initialDelay.toMillis(), duration.toMillis(), TimeUnit.MILLISECONDS);
		scheduler.schedule(scheduler::shutdownNow, maxTime.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Builds a new thread using an {@link ExecutorService}, allowing various utility methods.
	 * @return the created thread
	 */
	public static ExecutorService buildAsync() {
		return Executors.newCachedThreadPool();
	}

	/**
	 * Builds a new thread using an {@link ScheduledExecutorService}, allowing various utility methods.
	 * @return the created thread
	 */
	public static ScheduledExecutorService buildPeriodic() {
		return Executors.newSingleThreadScheduledExecutor();
	}
}
