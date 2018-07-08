package fr.vergne.progress.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import fr.vergne.progress.Progress;

/**
 * <p>
 * A {@link RecursiveProgress} aims at monitoring the advancement of a set of
 * sub-progresses which can come at any time. In other words, some
 * {@link Progress} instances are registered to this {@link RecursiveProgress}
 * through {@link #registerSubProgress(Progress)} in order to be considered in
 * its computation. Each sub-progress is counted through its
 * {@link #getCurrentNormalizedValue()}, such that when a progress adds 0 when
 * it is not started yet, 1 when it is finished, and something in between
 * otherwise. If the normalized value cannot be computed, then we take the most
 * preservative value, which is 0.
 * </p>
 * 
 * <p>
 * The max value is consequently the total number of sub-progresses considered.
 * {@link #getMaxValue()} starts undefined, because this total cannot be known
 * in advance, but can be set through {@link #setMaxSubProgresses(Integer)}. If
 * the total number of sub-progresses remains unknown, but the overall progress
 * is known to be finished, one can use {@link #setMaxSubProgressesToCurrent()}
 * to set the max value to the current number of sub-progresses registered so
 * far.
 * </p>
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 *
 */
public class RecursiveProgress implements Progress<Double> {

	private Integer max = null;
	private final Map<ID, Progress<?>> subprogresses = new HashMap<>();
	private final Map<ID, ProgressListener<Number>> sublisteners = new HashMap<>();
	private final Map<ID, Boolean> autoTerminate = new HashMap<>();
	private final Collection<ProgressListener<? super Double>> listeners = new HashSet<>();
	private static final Progress<Integer> TERMINATED_PROGRESS = new Progress<Integer>() {

		@Override
		public Integer getCurrentValue() {
			return 1;
		}

		@Override
		public Integer getMaxValue() {
			return 1;
		}

		@Override
		public void addProgressListener(
				ProgressListener<? super Integer> listener) {
			// No listener management
		}

		@Override
		public void removeProgressListener(
				ProgressListener<? super Integer> listener) {
			// No listener management
		}
	};

	@SuppressWarnings("serial")
	public static class MaxSubProgressesReachedException extends
			RuntimeException {
		public MaxSubProgressesReachedException(int max) {
			super(
					"No more subprogress can be added, we already reached the provided max of "
							+ max);
		}
	}

	public class ID {
		private ID() {
			// Private constructor
		}
	}

	/**
	 * Registers a new sub-progress to this {@link RecursiveProgress}. If the
	 * sub-progress has already been registered, an exception is thrown. The
	 * automatic termination parameter tells whether the method
	 * {@link #terminateSubProgress(ID)} should be called automatically when the
	 * sub-progress is finished.
	 * 
	 * @param subprogress
	 *            the sub-progress to register
	 * @param autoTerminate
	 *            <code>true</code> if the sub-progress should automatically
	 *            terminate, <code>false</code> otherwise
	 * @return {@link ID} which identifies the sub-progress
	 * @throws MaxSubProgressesReachedException
	 *             if we already reached the maximum number of sub-progresses to
	 *             register
	 * @throws IllegalArgumentException
	 *             if the sub-progress has already been registered
	 * @see RecursiveProgress#setMaxSubProgresses(Integer)
	 * @see RecursiveProgress#setMaxSubProgressesToCurrent()
	 */
	public ID registerSubProgress(final Progress<?> subprogress,
			boolean autoTerminate) throws MaxSubProgressesReachedException {
		if (subprogress == null) {
			throw new NullPointerException("Null subprogress provided");
		} else if (max != null && subprogresses.size() == max) {
			throw new MaxSubProgressesReachedException(max.intValue());
		} else if (subprogresses.containsValue(subprogress)) {
			throw new IllegalArgumentException("Already registered progress: "
					+ subprogress);
		} else {
			final ID id = new ID();
			ProgressListener<Number> listener = new ProgressListener<Number>() {

				@Override
				public void currentUpdate(Number value) {
					fireCurrentValueNotificationIfRequired(subprogress);
					terminateIfFinished(id);
				}

				@Override
				public void maxUpdate(Number maxValue) {
					fireCurrentValueNotificationIfRequired(subprogress);
					terminateIfFinished(id);
				}
			};
			this.subprogresses.put(id, subprogress);
			this.sublisteners.put(id, listener);
			this.autoTerminate.put(id, autoTerminate);
			subprogress.addProgressListener(listener);
			fireCurrentValueNotificationIfRequired(subprogress);
			return id;
		}
	}

	private void terminateIfFinished(ID id) {
		if (subprogresses.get(id).isFinished() && autoTerminate.get(id)) {
			terminateSubProgress(id);
		} else {
			// Keep it alive
		}
	}

	/**
	 * Register a sub-progress through
	 * {@link #registerSubProgress(Progress, boolean)} by requesting it to be
	 * automatically terminated when finished.
	 */
	public ID registerSubProgress(final Progress<?> subprogress)
			throws MaxSubProgressesReachedException {
		return registerSubProgress(subprogress, true);
	}

	/**
	 * This method terminates a sub-progress. The termination allows to
	 * disconnect from the sub-progress while still consider it as finished. If
	 * a registered sub-progress fails, this method enforces its completion
	 * (from the point of view of the {@link RecursiveProgress}).
	 * 
	 * @param id
	 *            the {@link ID} of the sub-progress to terminate
	 * @see #setAutoTerminate(boolean)
	 */
	public void terminateSubProgress(ID id) {
		if (subprogresses.containsKey(id)) {
			Progress<?> removed = subprogresses.put(id, TERMINATED_PROGRESS);
			removed.removeProgressListener(sublisteners.get(id));
		} else {
			// Not managed ID
		}
	}

	private void fireCurrentValueNotificationIfRequired(
			final Progress<?> subprogress) {
		if (subprogress.getCurrentValue() == null
				|| subprogress.getMaxValue() == null) {
			// Not counted, so current value does not change
		} else {
			Double current = getCurrentValue();
			for (ProgressListener<? super Double> listener : listeners) {
				listener.currentUpdate(current);
			}
		}
	}

	/**
	 * This method establishes a limit to the number of sub-progresses that
	 * {@link #registerSubProgress(Progress)} can accept. This limit can be
	 * removed by providing <code>null</code>.
	 * 
	 * @param max
	 *            the maximum number of sub-progresses to accept
	 */
	public void setMaxSubProgresses(Integer max) {
		this.max = max;
		for (ProgressListener<? super Double> listener : listeners) {
			listener.maxUpdate(getMaxValue());
		}
	}

	/**
	 * @return the maximum number of sub-progresses to accept, <code>null</code>
	 *         if no limit has been set yet
	 */
	public Integer getMaxSubProgresses() {
		return max;
	}

	/**
	 * Call {@link #setMaxSubProgresses(Integer)} with the current number of
	 * sub-progresses registered so far.
	 */
	public void setMaxSubProgressesToCurrent() {
		setMaxSubProgresses(subprogresses.size());
	}

	@Override
	public Double getCurrentValue() {
		Double value = 0.0;
		for (Progress<?> subprogress : subprogresses.values()) {
			Double subValue = subprogress.getCurrentNormalizedValue();
			if (subValue == null) {
				// Don't know what to add, assume zero
			} else {
				value += subValue;
			}
		}
		return value;
	}

	/**
	 * 
	 * @return the total number of sub-progresses that this
	 *         {@link RecursiveProgress} should manage
	 */
	@Override
	public Double getMaxValue() {
		return max == null ? null : max.doubleValue();
	}

	@Override
	public void addProgressListener(ProgressListener<? super Double> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeProgressListener(ProgressListener<? super Double> listener) {
		listeners.remove(listener);
	}

	@Override
	protected void finalize() throws Throwable {
		for (Entry<ID, Progress<?>> entry : subprogresses.entrySet()) {
			ID id = entry.getKey();
			Progress<?> subprogress = entry.getValue();
			subprogress.removeProgressListener(sublisteners.get(id));
		}
		super.finalize();
	}
}
