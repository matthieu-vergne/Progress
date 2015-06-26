package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.impl.ProgressFactory.ProgressSetter;

public class ProgressFactoryTest {

	private final ProgressFactory factory = new ProgressFactory();

	@Test
	public void testManualProgressCorretlyInitialized() {
		ProgressSetter<Integer> setter = new ProgressSetter<Integer>();
		Progress<Integer> progress = factory
				.createManualProgress(setter, 5, 10);

		assertEquals((Integer) 5, progress.getCurrentValue());
	}

	@Test
	public void testManualProgressCorretlyUpdated() {
		ProgressSetter<Integer> setter = new ProgressSetter<Integer>();
		Progress<Integer> progress = factory
				.createManualProgress(setter, 5, 10);

		setter.setCurrentValue(3);
		assertEquals((Integer) 3, progress.getCurrentValue());

		setter.setMaxValue(5);
		assertEquals((Integer) 5, progress.getMaxValue());
	}

	@Test
	public void testManualProgressCorretlyFinished() {
		ProgressSetter<Integer> setter = new ProgressSetter<Integer>();
		int max = 10;
		Progress<Integer> progress = factory.createManualProgress(setter, 0,
				max);

		for (int i = 0; i < max; i++) {
			setter.setCurrentValue(i);
			assertFalse(progress.isFinished());
		}
		setter.setCurrentValue(max);
		assertTrue(progress.isFinished());
	}

	@Test
	public void testManualProgressCorretlyNotifies() {
		ProgressSetter<Integer> setter = new ProgressSetter<Integer>();
		Progress<Integer> progress = factory
				.createManualProgress(setter, 5, 10);
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

		setter.setCurrentValue(1);
		assertEquals(2, values.size());
		assertEquals((Integer) 1, values.get(0));
		assertEquals(null, values.get(1));

		setter.setMaxValue(20);
		assertEquals(2, values.size());
		assertEquals((Integer) 1, values.get(0));
		assertEquals((Integer) 20, values.get(1));
	}

	@Test
	public void testManualProgressThrowsExceptionOnTooHighValue() {
		ProgressSetter<Integer> setter = new ProgressSetter<Integer>();
		factory.createManualProgress(setter, 5, 10);

		try {
			setter.setCurrentValue(20);
			fail("No exception thrown because higher than max");
		} catch (IllegalArgumentException e) {
		}
	}

}
