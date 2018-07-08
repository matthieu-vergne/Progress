package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.heterogeneousmap.HeterogeneousMap;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.ProgressTest;

@RunWith(JUnitPlatform.class)
public class ManualProgressTest implements
		ProgressTest<ManualProgress<Integer>> {

	@Override
	public ManualProgress<Integer> createStartingProgress(
			HeterogeneousMap context) {
		return new ManualProgress<Integer>(0, 5);
	}

	@Override
	public boolean canIncrementWithoutFinishing(
			ManualProgress<Integer> progress, HeterogeneousMap context) {
		return progress.getMaxValue() > progress.getCurrentValue() + 1;
	}

	@Override
	public void increment(ManualProgress<Integer> progress,
			HeterogeneousMap context) {
		progress.setCurrentValue(progress.getCurrentValue() + 1);
	}

	@Override
	public void finish(ManualProgress<Integer> progress,
			HeterogeneousMap context) {
		progress.finish();
	}

	@Override
	public void changeMax(ManualProgress<Integer> progress,
			HeterogeneousMap context) {
		progress.setMaxValue(progress.getMaxValue() + 1);
	}

	@Test
	public void testManualProgressCorretlyInitialized() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		assertEquals((Integer) 5, progress.getCurrentValue());
	}

	@Test
	public void testManualProgressCorretlyUpdated() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		progress.setCurrentValue(3);
		assertEquals((Integer) 3, progress.getCurrentValue());

		progress.setMaxValue(5);
		assertEquals((Integer) 5, progress.getMaxValue());
	}

	@Test
	public void testManualProgressCorretlyFinished() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 10);

		for (int i = 0; i < progress.getMaxValue(); i++) {
			progress.setCurrentValue(i);
			assertFalse(progress.isFinished());
		}
		progress.setCurrentValue(progress.getMaxValue());
		assertTrue(progress.isFinished());
	}

	@Test
	public void testManualProgressCorretlyNotifies() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);
		final List<Integer> values = new ArrayList<Integer>(2);
		values.add(null);
		values.add(null);
		ProgressListener<Integer> listener = new ProgressListener<Integer>() {

			@Override
			public void currentUpdate(Integer value) {
				values.set(0, value);
			}

			@Override
			public void maxUpdate(Integer maxValue) {
				values.set(1, maxValue);
			}

		};
		progress.addProgressListener(listener);

		assertEquals(2, values.size());
		assertEquals(null, values.get(0));
		assertEquals(null, values.get(1));

		progress.setCurrentValue(1);
		assertEquals(2, values.size());
		assertEquals((Integer) 1, values.get(0));
		assertEquals(null, values.get(1));

		progress.setMaxValue(20);
		assertEquals(2, values.size());
		assertEquals((Integer) 1, values.get(0));
		assertEquals((Integer) 20, values.get(1));
	}

	@Test
	public void testManualProgressThrowsExceptionOnNullValue() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		try {
			progress.setCurrentValue(null);
			fail("No exception thrown because null value");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testManualProgressThrowsExceptionOnNegativeValue() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		try {
			progress.setCurrentValue(-20);
			fail("No exception thrown because negative value");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testManualProgressThrowsExceptionOnTooHighValue() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		try {
			progress.setCurrentValue(20);
			fail("No exception thrown because higher than max");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testManualProgressThrowsExceptionOnTooLowMaxValue() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		try {
			progress.setMaxValue(1);
			fail("No exception thrown because lower than current");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testManualProgressThrowsExceptionOnNegativeMaxValue() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		try {
			progress.setMaxValue(-1);
			fail("No exception thrown because negative value");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testManualProgressAcceptNoMax() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, null);

		assertEquals(null, progress.getMaxValue());
	}

	@Test
	public void testManualProgressFinishedStateWithMax() {
		int max = 10;
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, max);

		for (int i = 0; i < max; i++) {
			progress.setCurrentValue(i);
			assertFalse(progress.isFinished());
		}
		progress.setCurrentValue(max);
		assertTrue(progress.isFinished());
	}

	@Test
	public void testManualProgressFinishedStateWithoutMax() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, null);

		for (int i = 0; i < 100; i++) {
			progress.setCurrentValue(i);
			assertFalse(progress.isFinished());
		}
		progress.setMaxValue(progress.getCurrentValue());
		assertTrue(progress.isFinished());
	}

	@Test
	public void testManualProgressProperlyFinishedWhenRequested() {
		Collection<ManualProgress<Integer>> progresses = new LinkedList<ManualProgress<Integer>>();
		progresses.add(new ManualProgress<Integer>(0, 10));
		progresses.add(new ManualProgress<Integer>(5, null));

		for (ManualProgress<Integer> progress : progresses) {
			progress.finish();
			assertTrue(progress.isFinished());
		}
	}

	@Test
	public void testManualProgressCorretlyAdd() {
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 10);

		assertEquals((Integer) 0, progress.getCurrentValue());
		progress.add(1);
		assertEquals((Integer) 1, progress.getCurrentValue());
		progress.add(2);
		assertEquals((Integer) 3, progress.getCurrentValue());
		progress.add(5);
		assertEquals((Integer) 8, progress.getCurrentValue());
	}
}
