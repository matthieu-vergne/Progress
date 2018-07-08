package fr.vergne.progress;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.vergne.heterogeneousmap.HeterogeneousMap;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.impl.BinaryProgress;

public interface ProgressTest<P extends Progress<?>> {

	/**
	 * This method allows to ignore the tests of intermediate steps. This should
	 * almost never happen, so by default it returns <code>false</code>. It
	 * happens for example with {@link BinaryProgress}, which is either at the
	 * start or at the end, and thus has no intermediate step.
	 * 
	 * @return <code>true</code> if the implementation finishes as soon as it
	 *         increases its current value, <code>false</code> otherwise
	 */
	default boolean canNeverIncrementWithoutFinishing() {
		return false;
	}

	/**
	 * This method allows to ignore the tests of max updates. Some
	 * implementations may have a fixed max while focusing on computing the
	 * current value, like {@link BinaryProgress} which is either at 0 or 1.
	 * Such instance should be unable to change their max value, and thus
	 * override this method to return <code>true</code>.
	 * 
	 * @return <code>true</code> if the implementation cannot change its max
	 *         value, <code>false</code> otherwise
	 */
	default boolean canNeverChangeMax() {
		return false;
	}

	/**
	 * Create an instance of the {@link Progress} to test. The context provided
	 * in argument allows to store information that will be provided in the
	 * other methods for reuse.
	 * 
	 * @param context
	 * @return the new {@link Progress} instance
	 */
	public P createStartingProgress(HeterogeneousMap context);

	/**
	 * @return <code>true</code> if calling {@link #increment(Progress, Map)}
	 *         does not allow (yet) to finish the progress.
	 */
	public boolean canIncrementWithoutFinishing(P progress,
			HeterogeneousMap context);

	/**
	 * Increment the {@link Progress} instance.
	 */
	public void increment(P progress, HeterogeneousMap context);

	/***
	 * Finishes the {@link Progress} instance.
	 */
	public void finish(P progress, HeterogeneousMap context);

	/**
	 * Change the max value of the {@link Progress} instance.
	 */
	public void changeMax(P progress, HeterogeneousMap context);

	@Test
	default void testProgressStartsWithZeroValue() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		assertEquals("The progress should always start at 0", 0, progress
				.getCurrentValue().doubleValue(), 0);
	}

	@Test
	default void testProgressStartsNonFinished() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		assertFalse("The progress should start unfinished",
				progress.isFinished());
	}

	@Test
	default void testProgressStartsWithKnownCurrentValue() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		assertNotNull("The current value should be always known",
				progress.getCurrentValue());
	}

	@Test
	default void testIntermediateIncrementsIncreaseCurrentValue() {
		if (canNeverIncrementWithoutFinishing()) {
			// Skip test
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			P progress = createStartingProgress(context);
			if (!canIncrementWithoutFinishing(progress, context)) {
				fail("The progress should be able to increment at least once without finishing");
			} else {
				double previousValue = progress.getCurrentValue().doubleValue();
				int limit = 100;
				while (limit > 0
						&& canIncrementWithoutFinishing(progress, context)) {
					increment(progress, context);
					assertNotNull("The current value should be always known",
							progress.getCurrentValue());
					double currentValue = progress.getCurrentValue()
							.doubleValue();
					assertTrue(
							"The increment step should always increment the current value ("
									+ previousValue + " became " + currentValue
									+ ")", currentValue > previousValue);
					assertFalse("The increment step was said to not finish",
							progress.isFinished());

					previousValue = currentValue;
					limit--;
				}
			}
		}
	}

	@Test
	default void testProgressFinishesWithNonNullCurrentValue() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		finish(progress, context);
		assertNotNull(progress.getCurrentValue());
	}

	@Test
	default void testProgressFinishesWithSameCurrentAndMaxValue() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		finish(progress, context);
		assertEquals(progress.getCurrentValue(), progress.getMaxValue());
	}

	@Test
	default void testProgressRecognizeFinish() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		finish(progress, context);
		assertTrue(progress.isFinished());
	}

	@Test
	default void testListenersRetrieveCurrentValueUpdates() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);

		List<Number> values = new LinkedList<Number>();
		progress.addProgressListener(new ProgressListener<Number>() {

			@Override
			public void currentUpdate(Number value) {
				values.add(value);
			}

			@Override
			public void maxUpdate(Number maxValue) {
				// Ignore
			}
		});

		int limit = 10;
		int updateCount = 0;
		while (limit > 0 && canIncrementWithoutFinishing(progress, context)) {
			increment(progress, context);
			updateCount++;
			limit--;
		}
		finish(progress, context);
		updateCount++;

		assertEquals(
				"The number of current value update notifications does not correspond",
				updateCount, values.size());
	}

	@Test
	default void testListenersRetrieveNoCurrentValueUpdateAfterRemoval() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);

		List<Number> values = new LinkedList<Number>();
		ProgressListener<Number> listener = new ProgressListener<Number>() {

			@Override
			public void currentUpdate(Number value) {
				values.add(value);
			}

			@Override
			public void maxUpdate(Number maxValue) {
				// Ignore
			}
		};
		progress.addProgressListener(listener);
		progress.removeProgressListener(listener);

		int limit = 10;
		while (limit > 0 && canIncrementWithoutFinishing(progress, context)) {
			increment(progress, context);
			limit--;
		}
		finish(progress, context);

		assertEquals("Notifications are still received: " + values, 0,
				values.size());
	}

	@Test
	default void testListenersRetrieveMaxValueUpdates() {
		if (canNeverChangeMax()) {
			// Ignore
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			P progress = createStartingProgress(context);

			List<Number> values = new LinkedList<Number>();
			progress.addProgressListener(new ProgressListener<Number>() {

				@Override
				public void currentUpdate(Number value) {
					// Ignore
				}

				@Override
				public void maxUpdate(Number maxValue) {
					values.add(maxValue);
				}
			});

			int limit = 10;
			int updateCount = 0;
			while (limit > 0) {
				changeMax(progress, context);
				updateCount++;
				limit--;
			}

			assertEquals(
					"The number of max update notifications does not correspond",
					updateCount, values.size());
		}
	}

	@Test
	default void testListenersRetrieveNoMaxValueUpdatesAfterRemoval() {
		if (canNeverChangeMax()) {
			// Ignore
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			P progress = createStartingProgress(context);

			List<Number> values = new LinkedList<Number>();
			ProgressListener<Number> listener = new ProgressListener<Number>() {

				@Override
				public void currentUpdate(Number value) {
					// Ignore
				}

				@Override
				public void maxUpdate(Number maxValue) {
					values.add(maxValue);
				}
			};
			progress.addProgressListener(listener);
			progress.removeProgressListener(listener);

			int limit = 10;
			while (limit > 0) {
				changeMax(progress, context);
				limit--;
			}

			assertEquals("Notifications are still received: " + values, 0,
					values.size());
		}
	}

	@Test
	default void testNormalizedValueIsZeroWhenStarting() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		assertEquals("The normalized value should be 0 when starting", 0,
				progress.getCurrentNormalizedValue(), 0);
	}

	@Test
	default void testNormalizedValueIsOneWhenFinished() {
		HeterogeneousMap context = new HeterogeneousMap();
		P progress = createStartingProgress(context);
		finish(progress, context);

		assertEquals("The normalized value should be 1 when finished", 1,
				progress.getCurrentNormalizedValue(), 0);
	}

	@Test
	default void testIntermediateIncrementsIncreaseNormalizedValue() {
		if (canNeverIncrementWithoutFinishing()) {
			// Skip test
		} else {
			HeterogeneousMap context = new HeterogeneousMap();
			P progress = createStartingProgress(context);
			if (!canIncrementWithoutFinishing(progress, context)) {
				fail("The progress should be able to increment at least once without finishing");
			} else {
				if (progress.getMaxValue() == null) {
					if (canNeverChangeMax()) {
						fail("The max is said to be unchangeable, but it is currently null, which is inconsistent");
					} else {
						changeMax(progress, context);
						if (progress.getMaxValue() == null) {
							fail("A new max has been requested, but it is still null");
						} else {
							// Request fulfilled
						}
					}
				} else {
					// Max already set
				}

				double previousValue = progress.getCurrentValue().doubleValue();
				int limit = 100;
				while (limit > 0
						&& canIncrementWithoutFinishing(progress, context)) {
					increment(progress, context);
					assertNotNull(
							"The normalized value should be known when a max is set",
							progress.getCurrentNormalizedValue());
					double currentValue = progress.getCurrentNormalizedValue()
							.doubleValue();
					assertTrue(
							"The increment step should always increment the normalized value ("
									+ previousValue + " became " + currentValue
									+ ")", currentValue > previousValue);
					assertFalse("The increment step was said to not finish",
							progress.isFinished());

					previousValue = currentValue;
					limit--;
				}
			}
		}
	}
}
