package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

import fr.vergne.progress.Predictor;
import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.PredictorFactory.PredictedValue;

public class PredictorFactoryTest {

	@Test
	public void testLinearPredictorOnCurrentValueReturnsCorrectPredictionWithProperLinearEvolution() {
		final long start = System.currentTimeMillis();
		final long maxDelta = 1000L;
		final long artificialDeviation = 5000L;
		ManualProgress<Long> progress = new ManualProgress<Long>(
				artificialDeviation + 0L, artificialDeviation + maxDelta);
		Predictor<Long> predictor = new PredictorFactory()
				.createLinearPredictor(progress, PredictedValue.CURRENT_VALUE);

		Random rand = new Random();
		while (!progress.isFinished()) {
			try {
				Thread.sleep(rand.nextInt((int) (maxDelta / 100)));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			long value = System.currentTimeMillis() - start
					+ artificialDeviation;
			if (value > progress.getMaxValue()) {
				progress.finish();
			} else {
				progress.setCurrentValue(value);
			}

			/*
			 * We wait only 10% done because it should be really reliable for a
			 * proper Linear evolution.
			 */
			long tenPercentProgress = (9 * artificialDeviation + progress
					.getMaxValue()) / 10;
			if (progress.getCurrentValue() < tenPercentProgress) {
				// Let it time to accumulate data
			} else {
				double acceptableError = 0.1;
				long expected = progress.getMaxValue();
				long min = (long) (expected - (expected - artificialDeviation)
						* acceptableError);
				long max = (long) (expected + (expected - artificialDeviation)
						* acceptableError);
				Long actual = predictor.predictValueAt(start + maxDelta);
				assertTrue(actual + " not in [" + min + ";" + max + "]",
						actual > min && actual < max);
			}
		}
	}

	@Test
	public void testLinearPredictorOnCurrentValueReturnsCorrectPredictionWithApproximativeLinearEvolution() {
		final long start = System.currentTimeMillis();
		final long maxDelta = 1000L;
		final long artificialDeviation = 5000L;
		ManualProgress<Long> progress = new ManualProgress<Long>(
				artificialDeviation + 0L, artificialDeviation + maxDelta);
		Predictor<Long> predictor = new PredictorFactory()
				.createLinearPredictor(progress, PredictedValue.CURRENT_VALUE);

		Random rand = new Random();
		while (!progress.isFinished()) {
			try {
				Thread.sleep(rand.nextInt((int) (maxDelta / 100)));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			long value = System.currentTimeMillis() - start
					+ artificialDeviation;
			if (value > progress.getMaxValue()) {
				progress.finish();
			} else {
				progress.add(maxDelta / 100 / 2);
			}

			/*
			 * We wait until half is done because it is not as reliable as a
			 * proper Linear evolution.
			 */
			long halfProgress = (artificialDeviation + progress.getMaxValue()) / 2;
			if (progress.getCurrentValue() < halfProgress) {
				// Let it time to accumulate data
			} else {
				double acceptableError = 0.1;
				long expected = progress.getMaxValue();
				long min = (long) (expected - (expected - artificialDeviation)
						* acceptableError);
				long max = (long) (expected + (expected - artificialDeviation)
						* acceptableError);
				Long actual = predictor.predictValueAt(start + maxDelta);
				assertTrue(actual + " not in [" + min + ";" + max + "]",
						actual > min && actual < max);
			}
		}
	}

	@Test
	public void testLinearPredictorOnCurrentValueEquivalentToLinearPredictorOnMaxValueForReversedProgress() {
		final long start = System.currentTimeMillis();
		final long maxDelta = 1000L;
		final long artificialDeviation = 5000L;
		final ManualProgress<Long> progress = new ManualProgress<Long>(
				artificialDeviation + 0L, artificialDeviation + maxDelta);
		/*
		 * The reversed progress reverses the current and max values, but also
		 * the signs to keep current <= max.
		 */
		Progress<Long> reversedProgress = new Progress<Long>() {

			@Override
			public Long getCurrentValue() {
				return -progress.getMaxValue();
			}

			@Override
			public Long getMaxValue() {
				return -progress.getCurrentValue();
			}

			@Override
			public boolean isFinished() {
				return progress.isFinished();
			}

			@Override
			public void addProgressListener(
					final ProgressListener<Long> listener) {
				progress.addProgressListener(new ProgressListener<Long>() {

					@Override
					public void maxUpdate(Long maxValue) {
						listener.currentUpdate(-maxValue);
					}

					@Override
					public void currentUpdate(Long value) {
						listener.maxUpdate(-value);
					}
				});
			}

			@Override
			public void removeProgressListener(ProgressListener<Long> listener) {
				throw new RuntimeException("Not implemented");
			}
		};

		Predictor<Long> predictor = new PredictorFactory()
				.createLinearPredictor(progress, PredictedValue.CURRENT_VALUE);
		Predictor<Long> reversedPredictor = new PredictorFactory()
				.createLinearPredictor(reversedProgress,
						PredictedValue.MAX_VALUE);

		Random rand = new Random();
		while (!progress.isFinished()) {
			try {
				Thread.sleep(rand.nextInt((int) (maxDelta / 100)));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			// only update the progress, the reversed one is automatic
			long value = System.currentTimeMillis() - start
					+ artificialDeviation;
			if (value > progress.getMaxValue()) {
				progress.finish();
			} else {
				progress.setCurrentValue(value);
			}

			long tenPercentProgress = (9 * artificialDeviation + progress
					.getMaxValue()) / 10;
			if (progress.getCurrentValue() < tenPercentProgress) {
				/*
				 * Let it time to accumulate data, because although the reversed
				 * progress receives the same values, it receives them with a
				 * slightly different timestamp.
				 */
			} else {
				// consider a strict error margin to be almost equal
				double acceptableError = 0.02;
				long timestamp = start + maxDelta;
				long expected = predictor.predictValueAt(timestamp);
				long min = (long) (expected - (expected - artificialDeviation)
						* acceptableError);
				long max = (long) (expected + (expected - artificialDeviation)
						* acceptableError);
				long actual = -reversedPredictor.predictValueAt(timestamp);
				assertTrue(actual + " not in [" + min + ";" + max + "]",
						actual > min && actual < max);
			}
		}
	}

}
