package fr.vergne.progress.sample;

import java.util.Random;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.ManualProgress;
import fr.vergne.progress.impl.ProgressUtil;
import fr.vergne.progress.impl.ProgressUtil.Displayer;

public class LoggerSampleWithDisplaysBasedOnLogLevel {

	public static void main(String[] args) throws InterruptedException {
		// Level of verbosity
		Level level;
		level = Level.INFO; // display every second
		// level = Level.ALL; // display every update

		// Create the progress which manages the advancement of our task
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 1000);

		// Create the logger on which to display
		final Logger logger = Logger
				.getLogger(LoggerSampleWithDisplaysBasedOnLogLevel.class.getName());

		// Setup the level of verbosity
		logger.setLevel(level);
		for (Handler handler : logger.getParent().getHandlers()) {
			handler.setLevel(level);
		}

		// Display the progress regularly for small verbosity
		ProgressUtil.displayProgress(progress, 1000, new Displayer() {

			@Override
			public <Value extends Number> void display(Progress<Value> progress) {
				logger.info(ProgressUtil.toString(progress));
			}
		});

		// Display the progress at each update for finest verbosity
		ProgressUtil.displayProgress(progress, new Displayer() {

			@Override
			public <Value extends Number> void display(Progress<Value> progress) {
				logger.finest(ProgressUtil.toString(progress));
			}
		}, ProgressUtil.NO_DISPLAYER);

		// Run the task until finished
		logger.info("Start running...");
		Random rand = new Random();
		while (!progress.isFinished()) {
			// Simulation of the execution of the task
			Thread.sleep(rand.nextInt(10));

			// Update of the progress
			progress.setCurrentValue(progress.getCurrentValue() + 1);
		}
		logger.info("Finished.");
	}
}
