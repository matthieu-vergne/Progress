package fr.vergne.progress.sample.singleProgress;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import fr.vergne.progress.Predictor;
import fr.vergne.progress.Progress;
import fr.vergne.progress.impl.ManualProgress;
import fr.vergne.progress.impl.PredictorFactory;
import fr.vergne.progress.impl.PredictorFactory.PredictedValue;
import fr.vergne.progress.impl.ProgressUtil;
import fr.vergne.progress.impl.ProgressUtil.Displayer;

public class Sample5_TerminationPrediction {

	public static void main(String[] args) throws InterruptedException {
		// Create the progress which manages the advancement of our task
		ManualProgress<Integer> progress = new ManualProgress<Integer>(0, 1000);

		// Create the predictors:
		// - linear for our current value (rather constant change)
		// - constant for our max value (not supposed to change)
		PredictorFactory factory = new PredictorFactory();
		final Predictor<Integer> currentPredictor = factory
				.createLinearPredictor(progress, PredictedValue.CURRENT_VALUE);
		final Predictor<Integer> maxPredictor = factory
				.createConstantPredictor(progress.getMaxValue());

		// Display the progress evolution on the standard output
		// decorated with the estimated termination time
		ProgressUtil.displayProgress(progress, 1000, new Displayer() {

			@Override
			public <Value extends Number> void display(Progress<Value> progress) {
				String status = ProgressUtil.DEFAULT_FORMATTER.format(progress);

				long currentMillis = System.currentTimeMillis();
				long terminationMillis = ProgressUtil.predictTerminationTime(
						currentPredictor, maxPredictor);
				long remainingMillis = terminationMillis - currentMillis;

				SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
				String currentTime = format.format(new Date(currentMillis));
				String terminationTime = format.format(new Date(
						terminationMillis));
				// Use universal time zone to avoid summer time delay for
				// displaying the remaining time.
				format.setTimeZone(TimeZone.getTimeZone("GMT"));
				String remainingTime = format.format(new Date(remainingMillis));

				System.out.println("[" + currentTime + "] " + status
						+ ", finished around " + terminationTime + " in "
						+ remainingTime);
			}
		});

		// Run the task until finished
		System.out.println("Start running...");
		Random rand = new Random();
		while (!progress.isFinished()) {
			// Simulation of the execution of the task
			Thread.sleep(rand.nextInt(100));

			// Update of the progress
			progress.add(1);
		}
		System.out.println("Finished.");
	}
}
