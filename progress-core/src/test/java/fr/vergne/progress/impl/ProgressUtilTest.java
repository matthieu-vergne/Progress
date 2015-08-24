package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import fr.vergne.progress.Predictor;
import fr.vergne.progress.impl.PredictorFactory.PredictedValue;

public class ProgressUtilTest {

	@Test
	public void testIntegerPercentageBetween0And100() {
		for (int i = 0; i <= 1000; i++) {
			int percent = ProgressUtil.computeIntegerPercentage(i, 1000);
			assertTrue("Not in [0;100]: " + percent, percent >= 0
					&& percent <= 100);
		}
	}

	@Test
	public void testIntegerPercentageProperlyFloored() {
		for (int i = 0; i < 10; i++) {
			assertEquals(0, ProgressUtil.computeIntegerPercentage(i, 1000));
		}
		assertEquals(1, ProgressUtil.computeIntegerPercentage(10, 1000));

		for (int i = 50; i < 60; i++) {
			assertEquals(5, ProgressUtil.computeIntegerPercentage(i, 1000));
		}
		assertEquals(6, ProgressUtil.computeIntegerPercentage(60, 1000));

		for (int i = 990; i < 1000; i++) {
			assertEquals(99, ProgressUtil.computeIntegerPercentage(i, 1000));
		}
		assertEquals(100, ProgressUtil.computeIntegerPercentage(1000, 1000));
	}

	@Test
	public void testDefaultFormatterOnSpecificCasesPreviouslyWrong() {
		assertEquals("1644/1800 (91%)",
				ProgressUtil.DEFAULT_FORMATTER
						.format(new ManualProgress<Integer>(1644, 1800)));
	}

	@Test
	public void testTerminationPredictionCorrectOnLinearEvolution() {
		final long start = System.currentTimeMillis();
		final long maxDelta = 1000L;
		ManualProgress<Long> progress = new ManualProgress<Long>(0L, maxDelta);

		PredictorFactory factory = new PredictorFactory();
		Predictor<Long> currentPredictor = factory.createLinearPredictor(
				progress, PredictedValue.CURRENT_VALUE);
		Predictor<Long> maxPredictor = factory.createConstantPredictor(progress
				.getMaxValue());

		Random rand = new Random();
		while (!progress.isFinished()) {
			try {
				Thread.sleep(rand.nextInt((int) (maxDelta / 100)));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			long value = System.currentTimeMillis() - start;
			if (value > progress.getMaxValue()) {
				progress.finish();
			} else {
				progress.setCurrentValue(value);
			}

			/*
			 * We wait only 10% done because it should be really reliable for a
			 * proper linear evolution.
			 */
			long tenPercentProgress = progress.getMaxValue() / 10;
			if (progress.getCurrentValue() < tenPercentProgress) {
				// Let it time to accumulate data
			} else {
				double acceptableError = 0.01;
				long expected = start + maxDelta;
				long min = (long) (expected * (1 - acceptableError));
				long max = (long) (expected * (1 + acceptableError));
				long actual = ProgressUtil.predictTerminationTime(
						currentPredictor, maxPredictor);
				assertTrue(actual + " not in [" + min + ";" + max + "]",
						actual > min && actual < max);
			}
		}
	}

}
