package fr.vergne.progress.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.vergne.progress.Progress;

/**
 * A {@link BinaryProgress} is a {@link Progress} which can be only in 2 states:
 * finished or not. More precisely, its value can be 0 (nothing done) or 1
 * (finished). The only way to change it is either to {@link #restart()} or
 * {@link #finish()} it.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class BinaryProgress implements Progress<Byte> {

	private byte current = 0;
	private final Collection<ProgressListener<Byte>> listeners = new HashSet<ProgressListener<Byte>>();

	@Override
	public Byte getCurrentValue() {
		return current;
	}

	@Override
	public Byte getMaxValue() {
		return 1;
	}

	@Override
	public boolean isFinished() {
		return current == 1;
	}

	@Override
	public void addProgressListener(ProgressListener<Byte> listener) {
		listeners.add(listener);
	}

	@Override
	public void removeProgressListener(ProgressListener<Byte> listener) {
		listeners.remove(listener);
	}

	/**
	 * Restart this {@link BinaryProgress} by setting the current value to 0.
	 */
	public void restart() {
		current = 0;
		for (ProgressListener<Byte> listener : listeners) {
			listener.currentUpdate(current);
		}
	}

	/**
	 * Finish this {@link BinaryProgress} by setting the current value to 1.
	 */
	public void finish() {
		current = 1;
		for (ProgressListener<Byte> listener : listeners) {
			listener.currentUpdate(current);
		}
	}

}
