package fr.vergne.progress.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;

/**
 * This {@link ProgressFactory} provides facilities to create different kinds of
 * {@link Progress} instances.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class ProgressFactory {

	/**
	 * Creates a {@link Progress} instance which should be manually updated via
	 * a {@link ProgressSetter} provided in argument.
	 * 
	 * @param setter
	 *            the setter used to update the {@link Progress}
	 * @param startValue
	 *            the initial value of the {@link Progress}
	 * @param maxValue
	 *            the max value of the {@link Progress} (can be
	 *            <code>null</code>)
	 * @return the {@link Progress} instance
	 */
	public <T extends Number> Progress<T> createManualProgress(
			final ProgressSetter<T> setter, T startValue, T maxValue) {
		final List<T> values = new ArrayList<T>(2);
		values.add(startValue);
		values.add(maxValue);
		final Collection<ProgressListener<T>> listeners = new HashSet<ProgressListener<T>>();

		final ProgressListener<T> listener = new ProgressListener<T>() {

			@Override
			public void currentUpdate(T value) {
				T max = values.get(1);
				if (max != null && value.doubleValue() > max.doubleValue()) {
					throw new IllegalArgumentException("Value higher than max:"
							+ value + " > " + max);
				} else {
					values.set(0, value);
					for (ProgressListener<T> listener : listeners) {
						listener.currentUpdate(value);
					}
				}
			}

			@Override
			public void maxUpdate(T maxValue) {
				values.set(1, maxValue);
				for (ProgressListener<T> listener : listeners) {
					listener.maxUpdate(maxValue);
				}
			}
		};
		setter.addProgressListener(listener);

		return new Progress<T>() {

			@Override
			public T getCurrentValue() {
				return values.get(0);
			}

			@Override
			public T getMaxValue() {
				return values.get(1);
			}

			@Override
			public boolean isFinished() {
				T value = getCurrentValue();
				T max = getMaxValue();
				return max != null && max.equals(value);
			}

			@Override
			public void addProgressListener(ProgressListener<T> listener) {
				listeners.add(listener);
			}

			@Override
			public void removeUpdateListener(ProgressListener<T> listener) {
				listeners.remove(listener);
			}

			@Override
			protected void finalize() throws Throwable {
				setter.removeProgressListener(listener);
			}
		};
	}

	public static class ProgressSetter<T extends Number> {
		private final Collection<ProgressListener<T>> listeners = new HashSet<ProgressListener<T>>();

		public void setCurrentValue(T value) {
			for (ProgressListener<T> listener : listeners) {
				listener.currentUpdate(value);
			}
		}

		public void setMaxValue(T value) {
			for (ProgressListener<T> listener : listeners) {
				listener.maxUpdate(value);
			}
		}

		private void addProgressListener(ProgressListener<T> listener) {
			listeners.add(listener);
		}

		private void removeProgressListener(ProgressListener<T> listener) {
			listeners.remove(listener);
		}

	}

}
