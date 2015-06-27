package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
	public void testGlobalAdditiveProgressManagesInteger() {
		ManualProgress<Integer> p1 = factory.createManualProgress(1, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(2, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(3, 3);
		Progress<Integer> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

		assertEquals((Integer) 6, progress.getCurrentValue());
		assertEquals((Integer) 18, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesLong() {
		ManualProgress<Long> p1 = factory.createManualProgress(1L, 10L);
		ManualProgress<Long> p2 = factory.createManualProgress(2L, 5L);
		ManualProgress<Long> p3 = factory.createManualProgress(3L, 3L);
		Progress<Long> progress = factory.createGlobalAdditiveProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Long) 6L, progress.getCurrentValue());
		assertEquals((Long) 18L, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesShort() {
		ManualProgress<Short> p1 = factory.createManualProgress((short) 1,
				(short) 10);
		ManualProgress<Short> p2 = factory.createManualProgress((short) 2,
				(short) 5);
		ManualProgress<Short> p3 = factory.createManualProgress((short) 3,
				(short) 3);
		Progress<Short> progress = factory.createGlobalAdditiveProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Short) (short) 6, progress.getCurrentValue());
		assertEquals((Short) (short) 18, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesFloat() {
		ManualProgress<Float> p1 = factory.createManualProgress(1.3F, 10.1F);
		ManualProgress<Float> p2 = factory.createManualProgress(2.2F, 5.2F);
		ManualProgress<Float> p3 = factory.createManualProgress(3.1F, 3.3F);
		Progress<Float> progress = factory.createGlobalAdditiveProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Float) 6.6F, progress.getCurrentValue());
		assertEquals((Float) 18.6F, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesDouble() {
		ManualProgress<Double> p1 = factory.createManualProgress(1.3, 10.1);
		ManualProgress<Double> p2 = factory.createManualProgress(2.2, 5.2);
		ManualProgress<Double> p3 = factory.createManualProgress(3.1, 3.3);
		Progress<Double> progress = factory.createGlobalAdditiveProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Double) 6.6, progress.getCurrentValue());
		assertEquals((Double) 18.6, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesByte() {
		ManualProgress<Byte> p1 = factory.createManualProgress((byte) 1,
				(byte) 10);
		ManualProgress<Byte> p2 = factory.createManualProgress((byte) 2,
				(byte) 5);
		ManualProgress<Byte> p3 = factory.createManualProgress((byte) 3,
				(byte) 3);
		Progress<Byte> progress = factory.createGlobalAdditiveProgress(Arrays
				.asList(p1, p2, p3));

		assertEquals((Byte) (byte) 6, progress.getCurrentValue());
		assertEquals((Byte) (byte) 18, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesBigInteger() {
		ManualProgress<BigInteger> p1 = factory.createManualProgress(
				new BigInteger("1"), new BigInteger("10"));
		ManualProgress<BigInteger> p2 = factory.createManualProgress(
				new BigInteger("2"), new BigInteger("5"));
		ManualProgress<BigInteger> p3 = factory.createManualProgress(
				new BigInteger("3"), new BigInteger("3"));
		Progress<BigInteger> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

		assertEquals(new BigInteger("6"), progress.getCurrentValue());
		assertEquals(new BigInteger("18"), progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressManagesBigDecimal() {
		ManualProgress<BigDecimal> p1 = factory.createManualProgress(
				new BigDecimal("1.3"), new BigDecimal("10.1"));
		ManualProgress<BigDecimal> p2 = factory.createManualProgress(
				new BigDecimal("2.2"), new BigDecimal("5.2"));
		ManualProgress<BigDecimal> p3 = factory.createManualProgress(
				new BigDecimal("3.1"), new BigDecimal("3.3"));
		Progress<BigDecimal> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

		assertEquals(new BigDecimal("6.6"), progress.getCurrentValue());
		assertEquals(new BigDecimal("18.6"), progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressProvidesCorrectCurrentValue() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

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
	public void testGlobalAdditiveProgressProvidesCorrectMaxValue() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

		assertEquals((Integer) 18, progress.getMaxValue());
		p1.setMaxValue(null);
		assertEquals(null, progress.getMaxValue());
		p1.setMaxValue(5);
		assertEquals((Integer) 13, progress.getMaxValue());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGlobalAdditiveProgressProvidesCorrectFinishedState() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

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
	public void testGlobalAdditiveProgressProvidesCorrectNotifications() {
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 3);
		Progress<Integer> progress = factory
				.createGlobalAdditiveProgress(Arrays.asList(p1, p2, p3));

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

	@Test
	public void testGlobalAdditiveProgressRobustToSubProgressListModifications() {
		List<ManualProgress<Integer>> subProgresses = new LinkedList<ManualProgress<Integer>>();
		subProgresses.add(factory.createManualProgress(1, 10));
		subProgresses.add(factory.createManualProgress(2, 5));
		subProgresses.add(factory.createManualProgress(3, 3));

		Progress<Integer> progress = factory
				.createGlobalAdditiveProgress(subProgresses);
		Integer currentReference = progress.getCurrentValue();
		Integer maxReference = progress.getMaxValue();

		// Robust through computation
		subProgresses.add(factory.createManualProgress(5, 10));
		assertEquals(currentReference, progress.getCurrentValue());
		assertEquals(maxReference, progress.getMaxValue());

		// Robust through notifications
		for (ManualProgress<Integer> subProgress : subProgresses) {
			subProgress.setMaxValue(subProgress.getMaxValue() + 1);
			subProgress.setCurrentValue(subProgress.getCurrentValue() + 1);

			subProgress.setCurrentValue(subProgress.getCurrentValue() - 1);
			subProgress.setMaxValue(subProgress.getMaxValue() - 1);
		}
		assertEquals(currentReference, progress.getCurrentValue());
		assertEquals(maxReference, progress.getMaxValue());
	}

}
