package fr.vergne.progress;

/**
 * A {@link Progress} instance provides the advancement of a given process. A
 * usual evolution is to have a value ({@link #getCurrentValue()}) starting at
 * zero and increasing as long as the process advances, before to reach a
 * maximum value ({@link #getMaxValue()}). It could be that the progress regress
 * at some points (e.g. if something has to be redone). It could also happen
 * that the maximum value is not provided from the start, leading to not being
 * able to know how far the progress goes, only assessing that it is going
 * further, but once the process is finished the maximum value should be
 * provided.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <T>
 *            The type of {@link Number} to use, typically {@link Integer} when
 *            there is atomic steps to count, {@link Double} when we compute
 *            weights, or others for more specific cases.
 */
public interface Progress<T extends Number> {

	/**
	 * This method provides the current state of the process to be evaluated.
	 * This state correspond to a value which indicates how far the process went
	 * until now. If nothing was done yet, zero should be returned, otherwise it
	 * should return a strictly positive value. If the process is finished, it
	 * should return the same value than {@link #getMaxValue()}.
	 * 
	 * @return the current advancement of the process
	 */
	public T getCurrentValue();

	/**
	 * This method provides the limit which corresponds to the end of the
	 * evaluated process. If This amount is not known, this method should return
	 * <code>null</code>, otherwise it should return a strictly positive value.
	 * Once the process is finished, this method should return a proper value
	 * (not <code>null</code>).
	 * 
	 * @return the value corresponding to the end of the process
	 */
	public T getMaxValue();

	/**
	 * This listener allows to be notified when a property of a {@link Progress}
	 * instance evolve.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <T>
	 */
	public interface UpdateListener<T extends Number> {
		/**
		 * When the process evaluated evolves,
		 * {@link Progress#getCurrentValue()} changes (usually increasing, but
		 * not necessarily). When this happens, this method is called to notify
		 * about the new state of the process.
		 * 
		 * @param oldCurrent
		 *            the value before the update
		 * @param newCurrent
		 *            the value after the update
		 */
		public void currentUpdate(T oldCurrent, T newCurrent);

		/**
		 * Typically, the value returned by {@link Progress#getMaxValue()} if
		 * known before to start the process, but it is not always the case. For
		 * instance, the evaluation needed to know this max can be done in
		 * parallel of the running process, leading to know this maximum only
		 * after some advancement have already happen, or it could be that we
		 * cannot evaluate it at all and know about it only when the process is
		 * actually finished. In all these cases, this method helps to know when
		 * the maximum value is set, so that the advancement can be properly
		 * evaluated in real time.
		 * 
		 * @param oldMax
		 *            the value before the update
		 * @param newMax
		 *            the value after the update
		 */
		public void maxUpdate(T oldMax, T newMax);
	}

	/**
	 * This method allows to register a listener to be notified when a given
	 * property of the {@link Progress} will evolve.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void addUpdateListener(UpdateListener<T> listener);

	/**
	 * This method allows to unregister a listener previously registered with
	 * {@link #addUpdateListener(UpdateListener)}.
	 * 
	 * @param listener
	 *            the listener to unregister
	 */
	public void removeUpdateListener(UpdateListener<T> listener);
}
