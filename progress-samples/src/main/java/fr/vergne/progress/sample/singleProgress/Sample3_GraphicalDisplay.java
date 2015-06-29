package fr.vergne.progress.sample.singleProgress;

import java.util.Random;

import fr.vergne.progress.impl.ManualProgress;
import fr.vergne.progress.impl.ProgressUtil;

public class Sample3_GraphicalDisplay {

	public static void main(String[] args) throws InterruptedException {
		// Create the progress which manages the advancement of our task
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 10);

		// Display the progress evolution graphically
		ProgressUtil.createJDialog(progress, true, false);

		// Run the task until finished
		System.out.println("Start running...");
		Random rand = new Random();
		while (!progress.isFinished()) {
			// Simulation of the execution of the task
			Thread.sleep(rand.nextInt(1000));

			// Update of the progress
			progress.setCurrentValue(progress.getCurrentValue() + 1);
		}
		System.out.println("Finished.");
	}
}
