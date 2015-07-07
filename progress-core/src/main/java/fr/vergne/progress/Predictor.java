package fr.vergne.progress;

/**
 * A {@link Predictor} aims at providing expected {@link Value}s for instants
 * that we have not reached yet.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Value>
 */
public interface Predictor<Value extends Number> {

	/**
	 * 
	 * @param timestamp
	 *            the timestamp to consider
	 * @return the {@link Value} expected for this timestamp
	 */
	public Value predictValueAt(long timestamp);
}
