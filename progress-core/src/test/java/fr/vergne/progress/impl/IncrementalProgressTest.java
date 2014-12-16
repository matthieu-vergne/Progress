package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.progress.Progress.UpdateListener;

public class IncrementalProgressTest {

	@Test
	public void testInitDefault() {
		IncrementalProgress progress = new IncrementalProgress();
		assertEquals(0, progress.getCurrentValue(), 0);
		assertNull(progress.getMaxValue());
	}

	@Test
	public void testInitMax() {
		int max = 5;
		IncrementalProgress progress = new IncrementalProgress(max);
		assertEquals(0, progress.getCurrentValue(), 0);
		assertEquals(max, progress.getMaxValue(), 0);
	}

	@Test
	public void testSetGetCurrentValue() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setCurrentValue(3);
		assertEquals(3, progress.getCurrentValue(), 0);

		progress.setCurrentValue(98);
		assertEquals(98, progress.getCurrentValue(), 0);

		progress.setCurrentValue(0);
		assertEquals(0, progress.getCurrentValue(), 0);
	}

	@Test
	public void testNegativeCurrentValue() {
		IncrementalProgress progress = new IncrementalProgress();

		try {
			progress.setCurrentValue(-3);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testCurrentValueOverMax() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setMaxValue(10);
		progress.setCurrentValue(10);
		try {
			progress.setCurrentValue(11);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
		progress.setMaxValue(11);
		progress.setCurrentValue(11);
		try {
			progress.setCurrentValue(12);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testSetGetMaxValue() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setMaxValue(3);
		assertEquals(3, progress.getMaxValue(), 0);

		progress.setMaxValue(98);
		assertEquals(98, progress.getMaxValue(), 0);

		progress.setMaxValue(null);
		assertNull(progress.getMaxValue());
	}

	@Test
	public void testNegativeMaxValue() {
		IncrementalProgress progress = new IncrementalProgress();

		try {
			progress.setMaxValue(-3);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testNullMaxValue() {
		IncrementalProgress progress = new IncrementalProgress();

		try {
			progress.setMaxValue(0);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testMaxValueUnderCurrent() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setCurrentValue(10);
		try {
			progress.setMaxValue(9);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
		progress.setCurrentValue(9);
		progress.setMaxValue(9);
		try {
			progress.setMaxValue(8);
			fail("No exception thrown.");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testSingleIncrement() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setCurrentValue(10);
		assertEquals(10, progress.getCurrentValue(), 0);
		progress.increment();
		assertEquals(11, progress.getCurrentValue(), 0);
		progress.increment();
		assertEquals(12, progress.getCurrentValue(), 0);
		progress.increment();
		assertEquals(13, progress.getCurrentValue(), 0);

		progress.setCurrentValue(0);
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.increment();
		assertEquals(1, progress.getCurrentValue(), 0);
		progress.increment();
		assertEquals(2, progress.getCurrentValue(), 0);
		progress.increment();
		assertEquals(3, progress.getCurrentValue(), 0);
	}

	@Test
	public void testMultipleIncrements() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setCurrentValue(10);
		assertEquals(10, progress.getCurrentValue(), 0);
		progress.increment(5);
		assertEquals(15, progress.getCurrentValue(), 0);
		progress.increment(3);
		assertEquals(18, progress.getCurrentValue(), 0);
		progress.increment(50);
		assertEquals(68, progress.getCurrentValue(), 0);
	}

	@Test
	public void testMaxDefinition() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setMaxValue(null);
		assertFalse(progress.isMaxDefined());

		progress.setMaxValue(15);
		assertTrue(progress.isMaxDefined());

		progress.setMaxValue(6);
		assertTrue(progress.isMaxDefined());

		progress.setMaxValue(null);
		assertFalse(progress.isMaxDefined());
	}

	@Test
	public void testFinished() {
		IncrementalProgress progress = new IncrementalProgress();

		progress.setCurrentValue(5);
		progress.setMaxValue(10);
		assertFalse(progress.isFinished());

		progress.setCurrentValue(10);
		assertTrue(progress.isFinished());

		progress.setMaxValue(20);
		assertFalse(progress.isFinished());

		progress.setCurrentValue(20);
		assertTrue(progress.isFinished());

		progress.setMaxValue(null);
		assertFalse(progress.isFinished());
	}

	@Test
	public void testUpdateListener() {
		IncrementalProgress progress = new IncrementalProgress();

		final boolean[] triggers = new boolean[] { false, false };
		progress.addUpdateListener(new UpdateListener<Integer>() {

			@Override
			public void currentUpdate(Integer oldCurrent, Integer newCurrent) {
				triggers[0] = true;
			}

			@Override
			public void maxUpdate(Integer oldMax, Integer newMax) {
				triggers[1] = true;
			}
		});

		assertFalse(triggers[0]);
		assertFalse(triggers[1]);

		progress.setCurrentValue(12);
		assertTrue(triggers[0]);
		assertFalse(triggers[1]);
		triggers[0] = false;

		progress.setMaxValue(120);
		assertFalse(triggers[0]);
		assertTrue(triggers[1]);
		triggers[1] = false;

		progress.increment(12);
		assertTrue(triggers[0]);
		assertFalse(triggers[1]);
		triggers[0] = false;

		progress.increment();
		assertTrue(triggers[0]);
		assertFalse(triggers[1]);
		triggers[0] = false;

		progress.getCurrentValue();
		assertFalse(triggers[0]);
		assertFalse(triggers[1]);

		progress.getMaxValue();
		assertFalse(triggers[0]);
		assertFalse(triggers[1]);
	}
}
