package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;

public class ProgressFactoryTest {

	private final ProgressFactory factory = new ProgressFactory();

	@Test
	public void testManualProgressCorretlyInitialized() {
		ManualProgress<Integer> progress = factory.createManualProgress(5, 10);

		assertEquals((Integer) 5, progress.getCurrentValue());
	}

	@Test
	public void testManualProgressCorretlyUpdated() {
		ManualProgress<Integer> progress = factory.createManualProgress(5, 10);

		progress.setCurrentValue(3);
		assertEquals((Integer) 3, progress.getCurrentValue());

		progress.setMaxValue(5);
		assertEquals((Integer) 5, progress.getMaxValue());
	}

	@Test
	public void testManualProgressCorretlyFinished() {
		int max = 10;
		ManualProgress<Integer> progress = factory.createManualProgress(0, max);

		for (int i = 0; i < max; i++) {
			progress.setCurrentValue(i);
			assertFalse(progress.isFinished());
		}
		progress.setCurrentValue(max);
		assertTrue(progress.isFinished());
	}

	@Test
	public void testManualProgressCorretlyNotifies() {
		ManualProgress<Integer> progress = factory.createManualProgress(5, 10);
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
	public void testManualProgressThrowsExceptionOnTooHighValue() {
		ManualProgress<Integer> progress = factory.createManualProgress(5, 10);

		try {
			progress.setCurrentValue(20);
			fail("No exception thrown because higher than max");
		} catch (IllegalArgumentException e) {
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCombinedProgressProvideCorrectCurrentValue() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory.createCombinedProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Integer) 0, progress.getCurrentValue());
		p1.setCurrentValue(3);
		assertEquals((Integer) 3, progress.getCurrentValue());
		p2.setCurrentValue(1);
		assertEquals((Integer) 4, progress.getCurrentValue());
		p3.setCurrentValue(3);
		assertEquals((Integer) 7, progress.getCurrentValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCombinedProgressProvideCorrectMaxValue() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory.createCombinedProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Integer) 18, progress.getMaxValue());
		p1.setMaxValue(null);
		assertEquals(null, progress.getMaxValue());
		p1.setMaxValue(5);
		assertEquals((Integer) 13, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCombinedProgressProvideCorrectFinishedState() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory.createCombinedProgress(Arrays
				.asList(p1, p2, p3));

		assertFalse(progress.isFinished());
		p1.finish();
		assertFalse(progress.isFinished());
		p2.finish();
		assertFalse(progress.isFinished());
		p3.finish();
		assertTrue(progress.isFinished());
		p2.setCurrentValue(0);
		assertFalse(progress.isFinished());
		p2.finish();
		assertTrue(progress.isFinished());
		p1.setCurrentValue(0);
		assertFalse(progress.isFinished());
		p1.finish();
		assertTrue(progress.isFinished());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCombinedProgressProvideCorrectNotifications() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory.createCombinedProgress(Arrays
				.asList(p1, p2, p3));

		final List<Integer> notifiedValues = new ArrayList<Integer>(2);
		notifiedValues.add(null);
		notifiedValues.add(null);
		progress.addProgressListener(new ProgressListener<Integer>() {

			@Override
			public void currentUpdate(Integer value) {
				notifiedValues.set(0, value);
			}

			@Override
			public void maxUpdate(Integer maxValue) {
				notifiedValues.set(1, maxValue);
			}

		});

		assertEquals(null, notifiedValues.get(0));
		assertEquals(null, notifiedValues.get(1));

		p1.setCurrentValue(5);
		assertEquals(progress.getCurrentValue(), notifiedValues.get(0));
		p2.setCurrentValue(1);
		assertEquals(progress.getCurrentValue(), notifiedValues.get(0));
		p3.setCurrentValue(2);
		assertEquals(progress.getCurrentValue(), notifiedValues.get(0));

		p1.setMaxValue(15);
		assertEquals(progress.getMaxValue(), notifiedValues.get(1));
		p2.setMaxValue(20);
		assertEquals(progress.getMaxValue(), notifiedValues.get(1));
		p3.setMaxValue(5);
		assertEquals(progress.getMaxValue(), notifiedValues.get(1));
	}

}
