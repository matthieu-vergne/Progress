package fr.vergne.progress.impl;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;

import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.ProgressUtil.ValueTranslator;

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
	private final Collection<ProgressListener<? super Value>> listeners = new HashSet<>();
	private final ValueTranslator<Value> translator;

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
		translator = ProgressUtil.createValueTranslator(startValue);
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
			synchronized (this) {
				this.currentValue = value;
				for (ProgressListener<? super Value> listener : listeners) {
					listener.currentUpdate(value);
				}
			}
		}
	}

	@Override
	public Value getCurrentValue() {
		return currentValue;
	}

	/**
	 * Because a {@link Progress} is often about incrementing {@link Value}s,
	 * this method provides a shortcut to do such operation.
	 * 
	 * @param addedValue
	 *            the {@link Value} to add to the current value
	 */
	public void add(Value addedValue) {
		BigDecimal v1 = translator.toDecimal(currentValue);
		BigDecimal v2 = translator.toDecimal(addedValue);
		BigDecimal sum = v1.add(v2);

		setCurrentValue(translator.toValue(sum));
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
			synchronized (this) {
				this.maxValue = value;
				for (ProgressListener<? super Value> listener : listeners) {
					listener.maxUpdate(value);
				}
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
	public void addProgressListener(ProgressListener<? super Value> listener) {
		synchronized (this) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeProgressListener(ProgressListener<? super Value> listener) {
		synchronized (this) {
			listeners.remove(listener);
		}
	}

	@Override
	protected void finalize() throws Throwable {
		synchronized (this) {
			listeners.clear();
		}
	}

	@Override
	public String toString() {
		return ProgressUtil.DEFAULT_FORMATTER.format(this);
	}
}
