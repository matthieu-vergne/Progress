package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.heterogeneousmap.HeterogeneousMap;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.ProgressTest;

@RunWith(JUnitPlatform.class)
public class BinaryProgressTest implements ProgressTest<BinaryProgress> {

	@Override
	public BinaryProgress createStartingProgress(HeterogeneousMap context) {
		return new BinaryProgress();
	}

	@Override
	public boolean canIncrementWithoutFinishing(BinaryProgress progress,
			HeterogeneousMap context) {
		return false;
	}

	@Override
	public boolean canNeverIncrementWithoutFinishing() {
		return true;
	}
	
	@Override
	public void increment(BinaryProgress progress, HeterogeneousMap context) {
		progress.finish();
	}

	@Override
	public void finish(BinaryProgress progress, HeterogeneousMap context) {
		progress.finish();
	}

	@Override
	public void changeMax(BinaryProgress progress, HeterogeneousMap context) {
		// Cannot change max
	}
	
	@Override
	public boolean canNeverChangeMax() {
		return true;
	}

	@Test
	public void testBinaryProgressCorretlyInitialized() {
		BinaryProgress progress = new BinaryProgress();

		assertEquals((Byte) (byte) 0, progress.getCurrentValue());
	}

	@Test
	public void testBinaryProgressCorretlyFinishedAndRestarted() {
		BinaryProgress progress = new BinaryProgress();

		assertFalse(progress.isFinished());
		progress.finish();
		assertTrue(progress.isFinished());
		progress.restart();
		assertFalse(progress.isFinished());
	}

	@Test
	public void testBinaryProgressCorretlyNotifies() {
		BinaryProgress progress = new BinaryProgress();
		final List<Byte> values = new ArrayList<Byte>(2);
		values.add(null);
		values.add(null);
		ProgressListener<Byte> listener = new ProgressListener<Byte>() {

			@Override
			public void currentUpdate(Byte value) {
				values.set(0, value);
			}

			@Override
			public void maxUpdate(Byte maxValue) {
				values.set(1, maxValue);
			}

		};
		progress.addProgressListener(listener);

		assertEquals(2, values.size());
		assertEquals(null, values.get(0));
		assertEquals(null, values.get(1));

		progress.restart();
		assertEquals(2, values.size());
		assertEquals((Byte) (byte) 0, values.get(0));
		assertEquals(null, values.get(1));

		progress.finish();
		assertEquals(2, values.size());
		assertEquals((Byte) (byte) 1, values.get(0));
		assertEquals(null, values.get(1));
	}
}
