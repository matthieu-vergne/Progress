package fr.vergne.progress.sample;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.ManualProgress;
import fr.vergne.progress.impl.ProgressFactory;
import fr.vergne.progress.impl.ProgressUtil;

public class CombinedSample {

	public static void main(String[] args) throws InterruptedException {
		// Use a factory to simplify the creation
		ProgressFactory factory = new ProgressFactory();

		// Create the progresses corresponding to each atomic task to execute
		ManualProgress<Integer> p1 = factory.createManualProgress(0, 5);
		ManualProgress<Integer> p2 = factory.createManualProgress(0, 10);
		ManualProgress<Integer> p3 = factory.createManualProgress(0, 5);
		@SuppressWarnings("unchecked")
		Collection<ManualProgress<Integer>> subProgresses = Arrays.asList(p1,
				p2, p3);

		// Create the global progress
		Progress<Integer> global = factory
				.createCombinedProgress(subProgresses);

		// Display each progress on the standard output for details
		ProgressUtil.displayProgressOnOutputStream("p1: ", p1, System.out);
		ProgressUtil.displayProgressOnOutputStream("p2: ", p2, System.out);
		ProgressUtil.displayProgressOnOutputStream("p3: ", p3, System.out);
		ProgressUtil.displayProgressOnOutputStream("global: ", global,
				System.out);

		// Display the global progress graphically for overview
		ProgressUtil.createJDialog(global, true, false);

		// Run the task until finished
		System.out.println("Start running...");
		Random rand = new Random();
		List<ManualProgress<Integer>> notFinished = new LinkedList<ManualProgress<Integer>>(
				subProgresses);
		while (!global.isFinished()) {
			// Select a task to run
			ManualProgress<Integer> currentProgress = notFinished.get(rand
					.nextInt(notFinished.size()));

			// Simulation of the execution of the task
			Thread.sleep(rand.nextInt(1000));

			// Update of the progress of the task
			currentProgress
					.setCurrentValue(currentProgress.getCurrentValue() + 1);

			// If finished, do not execute it anymore
			if (currentProgress.isFinished()) {
				notFinished.remove(currentProgress);
				System.out.println("Finished sub-task.");
			} else {
				// not finished yet
			}
		}
		System.out.println("Finished global task.");
	}
}
