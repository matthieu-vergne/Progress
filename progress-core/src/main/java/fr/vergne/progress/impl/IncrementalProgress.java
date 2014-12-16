package fr.vergne.progress.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.vergne.progress.Progress;

/**
 * An {@link IncrementalProgress} aims at providing the basic features to manage
 * a process which advances through atomic steps. Each step achieved would lead
 * to an {@link #increment()} call. the {@link #increment(int)} method allow to
 * advance several steps at once, which can be useful for instance if some
 * optional steps are refused from the user.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class IncrementalProgress implements Progress<Integer> {

	/**
	 * The current value.
	 */
	private Integer current = 0;
	/**
	 * The maximum value to reach. It can be <code>null</code> if it is not
	 * known yet.
	 */
	private Integer max = null;
	/**
	 * All the {@link UpdateListener}s registered.
	 */
	private final Collection<UpdateListener<Integer>> listeners = new HashSet<UpdateListener<Integer>>();

	/**
	 * Create an {@link IncrementalProgress} with its default values: the
	 * current value is at zero and the maximum value is undefined (
	 * <code>null</code>). You will have to set the max through
	 * {@link #setMaxValue(Integer)} before the process can be considered as
	 * finished.
	 */
	public IncrementalProgress() {
		// use default value
	}

	/**
	 * Create an {@link IncrementalProgress} which starts at zero and for which
	 * the maximum is set at the value given in argument.
	 * 
	 * @param max
	 *            the maximum value
	 */
	public IncrementalProgress(int max) {
		setMaxValue(max);
	}

	@Override
	public Integer getCurrentValue() {
		return current;
	}

	/**
	 * Change the current value to a specific one. If you want to increase the
	 * value to show that the process is going ahead, prefer to use
	 * {@link #increment()} or {@link #increment(int)} which do not need to know
	 * about the current value.
	 * 
	 * @param newValue
	 *            the current value to consider from now on
	 * @see #increment()
	 * @see #increment(int)
	 * @throws IllegalArgumentException
	 *             if the value provided is inconsistent
	 */
	public void setCurrentValue(int newValue) {
		if (newValue < 0) {
			throw new IllegalArgumentException(
					"The current value should be positive or null: " + newValue);
		} else if (isMaxDefined() && newValue > getMaxValue()) {
			throw new IllegalArgumentException("The current value (" + newValue
					+ ") cannot be higher than the maximum value ("
					+ getMaxValue() + ").");
		} else {
			int oldValue = this.current;
			this.current = newValue;
			for (UpdateListener<Integer> listener : listeners) {
				listener.currentUpdate(oldValue, newValue);
			}
		}
	}

	/**
	 * Basically, each step correspond to an increment of 1 of
	 * {@link #getCurrentValue()}. This method allows to reproduce this behavior
	 * by adding one each time it is called. If you want to increment by several
	 * steps, you should prefer {@link #increment(int)}.
	 * 
	 * @see #increment(int)
	 */
	public void increment() {
		increment(1);
	}

	/**
	 * Basically, each step correspond to an increment of 1, but if several
	 * steps are made at once, you can use this method to do it in a single
	 * call. Otherwise, you could prefer the more simple {@link #increment()}.
	 * 
	 * @param steps
	 *            the number of steps to pass
	 * @see #increment()
	 */
	public void increment(int steps) {
		setCurrentValue(current + steps);
	}

	@Override
	public Integer getMaxValue() {
		return max;
	}

	/**
	 * If the maximum value has not been set yet (or if you want to change it),
	 * you can use this method to set it. You can also set it to
	 * <code>null</code> to make it undefined.
	 * 
	 * @param newValue
	 *            the maximum value to consider from now on
	 * @throws IllegalArgumentException
	 *             if the value provided is inconsistent
	 */
	public void setMaxValue(Integer newValue) {
		if (newValue != null && newValue <= 0) {
			throw new IllegalArgumentException(
					"The maximum value should be strictly positive: "
							+ newValue);
		} else if (newValue != null && newValue < getCurrentValue()) {
			throw new IllegalArgumentException("The maximum value (" + newValue
					+ ") cannot be lower than the current value ("
					+ getCurrentValue() + ").");
		} else {
			Integer oldValue = this.max;
			this.max = newValue;
			for (UpdateListener<Integer> listener : listeners) {
				listener.maxUpdate(oldValue, newValue);
			}
		}
	}

	/**
	 * 
	 * @return <code>true</code> if the maximum value is not <code>null</code>,
	 *         <code>false</code> otherwise
	 */
	public boolean isMaxDefined() {
		return max != null;
	}

	/**
	 * 
	 * @return <code>true</code> if the maximum value has been defined and
	 *         reached, <code>false</code> otherwise
	 */
	public boolean isFinished() {
		return isMaxDefined() && getMaxValue().equals(getCurrentValue());
	}

	@Override
	public void addUpdateListener(UpdateListener<Integer> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeUpdateListener(UpdateListener<Integer> listener) {
		listeners.remove(listener);
	}
}
