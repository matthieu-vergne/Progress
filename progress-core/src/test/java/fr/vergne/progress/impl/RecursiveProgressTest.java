package fr.vergne.progress.impl;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.heterogeneousmap.HeterogeneousMap;
import fr.vergne.heterogeneousmap.HeterogeneousMap.Key;
import fr.vergne.progress.ProgressTest;
import fr.vergne.progress.impl.RecursiveProgress.ID;
import fr.vergne.progress.impl.RecursiveProgress.MaxSubProgressesReachedException;

@RunWith(JUnitPlatform.class)
public class RecursiveProgressTest implements ProgressTest<RecursiveProgress> {

	private final Key<List<BinaryProgress>> remainingKey = new Key<List<BinaryProgress>>();

	@Override
	public RecursiveProgress createStartingProgress(HeterogeneousMap context) {
		List<BinaryProgress> remaining = new LinkedList<>();
		remaining.add(new BinaryProgress());
		remaining.add(new BinaryProgress());
		remaining.add(new BinaryProgress());
		remaining.add(new BinaryProgress());
		context.put(remainingKey, remaining);

		RecursiveProgress progress = new RecursiveProgress();
		for (BinaryProgress subprogress : remaining) {
			progress.registerSubProgress(subprogress);
		}
		progress.setMaxSubProgressesToCurrent();
		return progress;
	}

	@Override
	public boolean canIncrementWithoutFinishing(RecursiveProgress progress,
			HeterogeneousMap context) {
		return context.get(remainingKey).size() > 1;
	}

	@Override
	public void increment(RecursiveProgress progress, HeterogeneousMap context) {
		List<BinaryProgress> remaining = context.get(remainingKey);
		BinaryProgress subprogress = remaining.remove(0);
		subprogress.finish();
	}

	@Override
	public void finish(RecursiveProgress progress, HeterogeneousMap context) {
		for (BinaryProgress subprogress : context.get(remainingKey)) {
			subprogress.finish();
		}
	}

	@Override
	public void changeMax(RecursiveProgress progress, HeterogeneousMap context) {
		progress.setMaxSubProgresses(progress.getMaxSubProgresses() + 1);
		BinaryProgress subprogress = new BinaryProgress();
		progress.registerSubProgress(subprogress);
		context.get(remainingKey).add(subprogress);
	}

	@Test
	public void testAcceptNewSubProgresses() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
	}

	@Test
	public void testRejectsAlreadyRegisteredSubProgress() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		BinaryProgress subprogress = factory.createBinaryProgress();
		progress.registerSubProgress(subprogress);
		try {
			progress.registerSubProgress(subprogress);
			fail("No exception thrown");
		} catch (IllegalArgumentException e) {
			// OK
		}
	}

	@Test
	public void testDoNotTerminateWhenFinishedIfNotAuto() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		BinaryProgress subprogress = factory.createBinaryProgress();
		progress.registerSubProgress(subprogress, false);
		
		assertEquals(0, progress.getCurrentValue(), 0);
		subprogress.finish();
		assertEquals(1, progress.getCurrentValue(), 0);
		subprogress.restart();
		assertEquals(0, progress.getCurrentValue(), 0);
	}

	@Test
	public void testTerminateWhenFinishedIfAuto() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		BinaryProgress subprogress = factory.createBinaryProgress();
		progress.registerSubProgress(subprogress, true);
		
		assertEquals(0, progress.getCurrentValue(), 0);
		subprogress.finish();
		assertEquals(1, progress.getCurrentValue(), 0);
		subprogress.restart();
		assertEquals(1, progress.getCurrentValue(), 0);
	}

	@Test
	public void testTerminateSubProgressMakesSubProgressFinished() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		ID id1 = progress.registerSubProgress(factory.createBinaryProgress());
		ID id2 = progress.registerSubProgress(factory.createBinaryProgress());
		ID id3 = progress.registerSubProgress(factory.createBinaryProgress());

		assertEquals(0, progress.getCurrentValue(), 0);
		progress.terminateSubProgress(id1);
		assertEquals(1, progress.getCurrentValue(), 0);
		progress.terminateSubProgress(id3);
		assertEquals(2, progress.getCurrentValue(), 0);
		progress.terminateSubProgress(id2);
		assertEquals(3, progress.getCurrentValue(), 0);
	}

	@Test
	public void testTerminateSubProgressMakesRecursiveProgressIndependentOfSubProgress() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		BinaryProgress subprogress = factory.createBinaryProgress();
		ID id = progress.registerSubProgress(subprogress);

		assertEquals(0, progress.getCurrentValue(), 0);
		progress.terminateSubProgress(id);
		assertEquals(1, progress.getCurrentValue(), 0);

		subprogress.finish();
		assertEquals(1, progress.getCurrentValue(), 0);
		subprogress.restart();
		assertEquals(1, progress.getCurrentValue(), 0);
	}

	@Test
	public void testMaxValueStartsUndefined() {
		RecursiveProgress progress = new RecursiveProgress();

		assertNull(progress.getMaxValue());
	}

	@Test
	public void testMaxValueAtZeroFinishesTheProgress() {
		RecursiveProgress progress = new RecursiveProgress();
		progress.setMaxSubProgressesToCurrent();

		assertTrue(progress.isFinished());
		assertEquals(1, progress.getCurrentNormalizedValue(), 0);
	}

	@Test
	public void testMaxSubProgressesCorrespondsToMaxValue() {
		RecursiveProgress progress = new RecursiveProgress();

		progress.setMaxSubProgresses(3);
		assertEquals(3.0, progress.getMaxValue(), 3);
		progress.setMaxSubProgresses(5);
		assertEquals(5.0, progress.getMaxValue(), 3);
		progress.setMaxSubProgresses(null);
		assertNull(progress.getMaxValue());
	}

	@Test
	public void testRejectsSubProgressWhenMaxReached() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		progress.setMaxSubProgresses(3);
		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
		try {
			progress.registerSubProgress(factory.createBinaryProgress());
			fail("No exception thrown");
		} catch (MaxSubProgressesReachedException e) {
			// OK
		}
	}

	@Test
	public void testMaxSubProgressesToCurrentSetsMaxValue() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
		progress.setMaxSubProgressesToCurrent();
		assertEquals(2.0, progress.getMaxValue(), 0.0);
	}

	@Test
	public void testMaxSubProgressesToCurrentRejectsAdditionalSubProgress() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		progress.registerSubProgress(factory.createBinaryProgress());
		progress.registerSubProgress(factory.createBinaryProgress());
		progress.setMaxSubProgressesToCurrent();
		try {
			progress.registerSubProgress(factory.createBinaryProgress());
			fail("No exception thrown");
		} catch (MaxSubProgressesReachedException e) {
			// OK
		}
	}

	@Test
	public void testCurrentValueStartsAtZeroIfEmptyWithoutMax() {
		RecursiveProgress progress = new RecursiveProgress();
		assertEquals(0, progress.getCurrentValue(), 0);
	}

	@Test
	public void testCurrentValueStartsAtZeroIfEmptyWithMax() {
		RecursiveProgress progress = new RecursiveProgress();
		progress.setMaxSubProgresses(5);
		assertEquals(0, progress.getCurrentValue(), 0);
	}

	@Test
	public void testCurrentValueIsZeroWithNotStartedProgressesWithoutMax() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
	}

	@Test
	public void testCurrentValueIsZeroWithNotStartedProgressesWithMax() {
		RecursiveProgress progress = new RecursiveProgress();
		progress.setMaxSubProgresses(10);
		ProgressFactory factory = new ProgressFactory();

		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
		progress.registerSubProgress(factory.createBinaryProgress());
		assertEquals(0, progress.getCurrentValue(), 0);
	}

	@Test
	public void testCurrentValueAdvancesWithSubProgresses() {
		RecursiveProgress progress = new RecursiveProgress();
		ProgressFactory factory = new ProgressFactory();

		BinaryProgress subProgress1 = factory.createBinaryProgress();
		progress.registerSubProgress(subProgress1);
		ManualProgress<Integer> subProgress2 = factory.createManualProgress(0,
				4);
		progress.registerSubProgress(subProgress2);
		BinaryProgress subProgress3 = factory.createBinaryProgress();
		progress.registerSubProgress(subProgress3);

		assertEquals(0, progress.getCurrentValue(), 0);
		subProgress1.finish();
		assertEquals(1, progress.getCurrentValue(), 0);
		subProgress2.add(1);
		assertEquals(1.25, progress.getCurrentValue(), 0);
		subProgress2.add(1);
		assertEquals(1.5, progress.getCurrentValue(), 0);
		subProgress3.finish();
		assertEquals(2.5, progress.getCurrentValue(), 0);
		subProgress2.add(1);
		assertEquals(2.75, progress.getCurrentValue(), 0);
		subProgress2.add(1);
		assertEquals(3, progress.getCurrentValue(), 0);
	}

}
