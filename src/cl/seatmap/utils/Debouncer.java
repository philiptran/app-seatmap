package cl.seatmap.utils;

import android.os.Handler;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class Debouncer<T> {

	private Runnable runnable;
	private int interval;
	private Handler handler;
	private boolean scheduled;
	private T arg;

	public Debouncer(final Function<T> func, final int interval) {
		this.scheduled = false;
		this.runnable = new Runnable() {
			@Override
			public void run() {
				scheduled = false;
				func.call(arg);
			}
		};
		this.interval = interval;
		this.handler = new Handler();
	}

	private void beforeReschedule(T arg) {
		if (scheduled) {
			handler.removeCallbacks(runnable);
		} else {
			scheduled = true;
		}
		this.arg = arg;
	}

	public void call(T arg) {
		beforeReschedule(arg);
		handler.postDelayed(runnable, interval);
	}

	public void callImmediate(T arg) {
		beforeReschedule(arg);
		runnable.run();
	}

	public void destroy(boolean call) {
		if (scheduled) {
			handler.removeCallbacks(runnable);
			if (call) {
				runnable.run();
			}
		}
	}

	public void destroy() {
		destroy(false);
	}

	public interface Function<T> {
		void call(T arg);
	}
}