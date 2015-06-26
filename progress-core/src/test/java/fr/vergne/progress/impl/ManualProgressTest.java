package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.vergne.progress.Progress.ProgressListener;

public class ManualProgressTest {

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
		int max = 10;
		ManualProgress<Integer> progress = new ManualProgress<Integer>(5, 10);

		for (int i = 0; i < max; i++) {
			progress.setCurrentValue(i);
			assertFalse(progress.isFinished());
		}
		progress.setCurrentValue(max);
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
}
