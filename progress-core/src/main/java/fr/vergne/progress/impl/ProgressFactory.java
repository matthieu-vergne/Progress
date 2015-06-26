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
	 *            the initial {@link Value} of the {@link Progress}
	 * @param maxValue
	 *            the max {@link Value} of the {@link Progress} (can be
	 *            <code>null</code>)
	 * @return the {@link Progress} instance
	 */
	public <Value extends Number> Progress<Value> createManualProgress(
			final ProgressSetter<Value> setter, Value startValue, Value maxValue) {
		final List<Value> values = new ArrayList<Value>(2);
		values.add(startValue);
		values.add(maxValue);
		final Collection<ProgressListener<Value>> listeners = new HashSet<ProgressListener<Value>>();

		final ProgressListener<Value> listener = new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				Value max = values.get(1);
				if (max != null && value.doubleValue() > max.doubleValue()) {
					throw new IllegalArgumentException("Value higher than max:"
							+ value + " > " + max);
				} else {
					values.set(0, value);
					for (ProgressListener<Value> listener : listeners) {
						listener.currentUpdate(value);
					}
				}
			}

			@Override
			public void maxUpdate(Value maxValue) {
				values.set(1, maxValue);
				for (ProgressListener<Value> listener : listeners) {
					listener.maxUpdate(maxValue);
				}
			}
		};
		setter.addProgressListener(listener);

		return new Progress<Value>() {

			@Override
			public Value getCurrentValue() {
				return values.get(0);
			}

			@Override
			public Value getMaxValue() {
				return values.get(1);
			}

			@Override
			public boolean isFinished() {
				Value value = getCurrentValue();
				Value max = getMaxValue();
				return max != null && max.equals(value);
			}

			@Override
			public void addProgressListener(ProgressListener<Value> listener) {
				listeners.add(listener);
			}

			@Override
			public void removeUpdateListener(ProgressListener<Value> listener) {
				listeners.remove(listener);
			}

			@Override
			protected void finalize() throws Throwable {
				setter.removeProgressListener(listener);
			}
		};
	}

	public static class ProgressSetter<Value extends Number> {
		private final Collection<ProgressListener<Value>> listeners = new HashSet<ProgressListener<Value>>();

		public void setCurrentValue(Value value) {
			for (ProgressListener<Value> listener : listeners) {
				listener.currentUpdate(value);
			}
		}

		public void setMaxValue(Value value) {
			for (ProgressListener<Value> listener : listeners) {
				listener.maxUpdate(value);
			}
		}

		private void addProgressListener(ProgressListener<Value> listener) {
			listeners.add(listener);
		}

		private void removeProgressListener(ProgressListener<Value> listener) {
			listeners.remove(listener);
		}

	}

}
