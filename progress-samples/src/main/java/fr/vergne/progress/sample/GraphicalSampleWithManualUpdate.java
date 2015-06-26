package fr.vergne.progress.sample;

import java.util.Random;

import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.ProgressFactory;
import fr.vergne.progress.impl.ProgressFactory.ProgressSetter;
import fr.vergne.progress.impl.ProgressUtil;

public class GraphicalSampleWithManualUpdate {

	public static void main(String[] args) throws InterruptedException {
		// Create the progress which manages the advancement of our task
		ProgressFactory factory = new ProgressFactory();
		ProgressSetter<Integer> setter = new ProgressSetter<Integer>();
		Progress<Integer> progress = factory
				.createManualProgress(setter, 0, 10);

		// Display the progress evolution graphically
		ProgressUtil.displayProgressOnDialog(progress, false);

		// Run the task until finished
		System.out.println("Start running...");
		Random rand = new Random();
		while (!progress.isFinished()) {
			// Simulation of the execution of the task
			Thread.sleep(rand.nextInt(1000));

			// Update of the progress
			setter.setCurrentValue(progress.getCurrentValue() + 1);
		}
		System.out.println("Finished.");
	}
}
