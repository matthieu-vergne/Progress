package fr.vergne.progress;

/**
 * A {@link Progress} instance provides the advancement of a given process. A
 * usual evolution is to have a {@link Value} ({@link #getCurrentValue()})
 * starting at zero and increasing as long as the process advances, before to
 * reach a maximum {@link Value} ({@link #getMaxValue()}). It could be that the
 * progress regresses at some points (e.g. if something has been cancelled or
 * has to be redone). It could also happen that the maximum {@link Value} is not
 * provided from the start, leading to not being able to know how far the
 * progress goes, only assessing that it is going further, but once the process
 * is finished the maximum {@link Value} should be provided.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 *            The type of {@link Number} to use, typically {@link Integer} when
 *            there is atomic steps to count, {@link Double} when we compute
 *            weights, or others for more specific cases.
 */
public interface Progress<Value extends Number> {

	/**
	 * This method provides the current state of the process to be evaluated.
	 * This state corresponds to a {@link Value} which indicates how far the
	 * process went until now. If nothing was done yet, zero should be returned,
	 * otherwise it should return a strictly positive {@link Value}. If the
	 * process is finished, it should return the same {@link Value} than
	 * {@link #getMaxValue()}.<br/>
	 * <br/>
	 * While it is common to have a status which only increases, it is not
	 * guaranteed. For instance, a {@link Progress} could help to evaluate the
	 * advancement of a given task which could be cancelled, and the regression
	 * in case of cancellation can be shown by decreasing the {@link Value}
	 * returned by {@link #getCurrentValue()}.
	 * 
	 * @return the current advancement of the process
	 */
	public Value getCurrentValue();

	/**
	 * This method provides the limit which corresponds to the end of the
	 * evaluated process. If this amount is not known, this method should return
	 * <code>null</code>, otherwise it should return a strictly positive
	 * {@link Value} which is never below {@link #getCurrentValue()}. Once the
	 * process is finished, this method should return the same {@link Value}
	 * than {@link #getCurrentValue()}.
	 * 
	 * @return the value corresponding to the end of the process
	 */
	public Value getMaxValue();

	/**
	 * This method must return <code>true</code> if and only if the
	 * {@link Value}s returned by {@link #getCurrentValue()} and
	 * {@link #getMaxValue()} are equal and not <code>null</code>. In any other
	 * case, this method must return <code>false</code>.
	 * 
	 * @return <code>true</code> if the {@link Progress} is finished,
	 *         <code>false</code> otherwise
	 */
	public boolean isFinished();

	/**
	 * This listener allows to be notified when a property of a {@link Progress}
	 * instance evolves.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Value>
	 */
	public interface ProgressListener<Value extends Number> {
		/**
		 * When the process evaluated evolves,
		 * {@link Progress#getCurrentValue()} changes (usually increasing, but
		 * not necessarily). When this happens, this method is called to notify
		 * about the new state of the process.
		 * 
		 * @param value
		 *            the current {@link Value} of the {@link Progress}
		 */
		public void currentUpdate(Value value);

		/**
		 * Typically, the {@link Value} returned by
		 * {@link Progress#getMaxValue()} is known before to start the process,
		 * but it is not always the case. For instance, the evaluation needed to
		 * know this max can be done in parallel of the running process, leading
		 * to know this maximum only after some advancement has already
		 * occurred, or it could be that we cannot evaluate it at all and know
		 * about it only when the process is actually finished. In all these
		 * cases, this method helps to know when the maximum {@link Value} is
		 * set, so that the advancement can be properly evaluated in real time.
		 * 
		 * @param maxValue
		 *            the current max {@link Value} of the {@link Progress}
		 */
		public void maxUpdate(Value maxValue);
	}

	/**
	 * This method allows to register a listener to be notified when a given
	 * property of the {@link Progress} will evolve.
	 * 
	 * @param listener
	 *            the listener to register
	 */
	public void addProgressListener(ProgressListener<? super Value> listener);

	/**
	 * This method allows to unregister a listener previously registered with
	 * {@link #addProgressListener(ProgressListener)}.
	 * 
	 * @param listener
	 *            the listener to unregister
	 */
	public void removeProgressListener(ProgressListener<? super Value> listener);
}
