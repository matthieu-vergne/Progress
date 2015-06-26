package fr.vergne.progress.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.vergne.progress.Progress;

/**
 * A {@link ManualProgress} aims at providing a simple {@link Progress} that one
 * can manually update.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 */
public class ManualProgress<Value extends Number> implements Progress<Value> {

	private Value currentValue;
	private Value maxValue;
	private final Collection<ProgressListener<Value>> listeners = new HashSet<ProgressListener<Value>>();

	/**
	 * 
	 * @param startValue
	 *            the initial {@link Value} of this {@link ManualProgress}
	 * @param maxValue
	 *            the max {@link Value} of this {@link Progress}, possibly
	 *            <code>null</code>
	 */
	public ManualProgress(Value startValue, Value maxValue) {
		setCurrentValue(startValue);
		setMaxValue(maxValue);
	}

	public void setCurrentValue(Value value) {
		if (value == null) {
			throw new NullPointerException("The current value cannot be null: "
					+ value);
		} else if (value.doubleValue() < 0) {
			throw new IllegalArgumentException(
					"The current value cannot be negative: " + value);
		} else if (maxValue != null
				&& value.doubleValue() > maxValue.doubleValue()) {
			throw new IllegalArgumentException(
					"The current value cannot be higher than the max value ("
							+ maxValue + "): " + value);
		} else {
			this.currentValue = value;
			for (ProgressListener<Value> listener : listeners) {
				listener.currentUpdate(value);
			}
		}
	}

	@Override
	public Value getCurrentValue() {
		return currentValue;
	}

	public void setMaxValue(Value value) {
		if (value != null && value.doubleValue() < 0) {
			throw new IllegalArgumentException(
					"The max value cannot be negative: " + value);
		} else if (value != null
				&& currentValue.doubleValue() > value.doubleValue()) {
			throw new IllegalArgumentException(
					"The max value cannot be lower than the current value ("
							+ currentValue + "): " + value);
		} else {
			this.maxValue = value;
			for (ProgressListener<Value> listener : listeners) {
				listener.maxUpdate(value);
			}
		}
	}

	@Override
	public Value getMaxValue() {
		return maxValue;
	}

	public void finish() {
		if (maxValue == null) {
			setMaxValue(currentValue);
		} else {
			setCurrentValue(maxValue);
		}
	}

	@Override
	public boolean isFinished() {
		return maxValue != null && maxValue.equals(currentValue);
	}

	@Override
	public void addProgressListener(ProgressListener<Value> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeProgressListener(ProgressListener<Value> listener) {
		listeners.remove(listener);
	}

	@Override
	protected void finalize() throws Throwable {
		listeners.clear();
	}

	@Override
	public String toString() {
		return ProgressUtil.toString(this);
	}
}
