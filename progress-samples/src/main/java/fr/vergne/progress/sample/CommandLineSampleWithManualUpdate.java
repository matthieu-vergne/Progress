package fr.vergne.progress.sample;

import java.util.Random;

import fr.vergne.progress.impl.ManualProgress;
import fr.vergne.progress.impl.ProgressUtil;

public class CommandLineSampleWithManualUpdate {

	public static void main(String[] args) throws InterruptedException {
		// Create the progress which manages the advancement of our task
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 10);

		// Display the progress evolution on the standard output
		ProgressUtil.displayProgressOnOutputStream(progress, System.out);

		// Run the task until finished
		System.out.println("Start running...");
		Random rand = new Random();
		while (!progress.isFinished()) {
			// Simulation of the execution of the task
			Thread.sleep(rand.nextBoolean() ? 200 : 1000);

			// Update of the progress
			progress.setCurrentValue(progress.getCurrentValue() + 1);
		}
		System.out.println("Finished.");
	}
}
