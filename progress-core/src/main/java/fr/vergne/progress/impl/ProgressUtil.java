package fr.vergne.progress.impl;

import java.awt.GridLayout;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;

/**
 * This utility class provides different services to simplify the management of
 * {@link Progress} instances, especially their display.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class ProgressUtil {

	/**
	 * Create a {@link JDialog} to display a simple progress bar. The
	 * {@link JDialog} created is returned for further interaction, like
	 * changing the title to better explicit the current status of the task.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param closeOnTermination
	 *            <code>true</code> if the dialog should be automatically
	 *            disposed when the progress is over, <code>false</code> if it
	 *            should be closed manually.
	 * @return the {@link JDialog} displaying the {@link Progress}
	 */
	public static <Value extends Number> JDialog displayProgressOnDialog(
			final Progress<Value> progress, final boolean closeOnTermination) {
		final JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Running...");
		dialog.setLayout(new GridLayout(1, 1));

		JProgressBar bar = displayProgressOnBar(progress);
		dialog.add(bar);

		progress.addProgressListener(new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				checkAutoClose();
			}

			@Override
			public void maxUpdate(Value maxValue) {
				checkAutoClose();
			}

			private void checkAutoClose() {
				if (closeOnTermination) {
					Value value = progress.getCurrentValue();
					Value max = progress.getMaxValue();
					if (value != null && value.equals(max)) {
						dialog.dispose();
					} else {
						// still running
					}
				} else {
					// no auto-close
				}
			}
		});

		dialog.pack();
		dialog.setVisible(true);
		return dialog;
	}

	/**
	 * Create a {@link JProgressBar} to display a {@link Progress} instance. The
	 * {@link JProgressBar} returned can be added to a dialog or a frame.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @return the {@link JProgressBar} displaying the {@link Progress}
	 */
	public static <Value extends Number> JProgressBar displayProgressOnBar(
			final Progress<Value> progress) {
		final JProgressBar bar = new JProgressBar();
		bar.setStringPainted(true);
		bar.setString("");
		bar.setValue(0);
		configureBarMaximum(progress, bar);
		updateString(progress, bar);

		progress.addProgressListener(new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				bar.setValue(value.intValue());
				updateString(progress, bar);
			}

			@Override
			public void maxUpdate(Value maxValue) {
				configureBarMaximum(progress, bar);
				updateString(progress, bar);
			}
		});
		return bar;
	}

	private static <Value extends Number> void updateString(
			Progress<Value> progress, JProgressBar bar) {
		Value value = progress.getCurrentValue();
		Value max = progress.getMaxValue();
		if (max == null) {
			bar.setString(value.toString());
		} else {
			int percent = computeIntegerPercentage(value, max);
			bar.setString(value + "/" + max + " (" + percent + "%)");
		}
	}

	private static <Value extends Number> void configureBarMaximum(
			Progress<Value> progress, JProgressBar bar) {
		Value max = progress.getMaxValue();
		if (max == null) {
			bar.setIndeterminate(true);
			bar.setMaximum(Integer.MAX_VALUE);
		} else {
			bar.setIndeterminate(false);
			bar.setMaximum(max.intValue());
		}
	}

	/**
	 * Manage the display of a {@link Progress} instance on an
	 * {@link OutputStream}. The display occurs each time the {@link Progress}
	 * is updated.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param stream
	 *            the {@link OutputStream} on which it should be displayed
	 */
	public static <Value extends Number> void displayProgressOnOutputStream(
			final String prefix, final Progress<Value> progress,
			OutputStream stream) {
		final PrintStream printer = new PrintStream(stream);
		progress.addProgressListener(new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				printer.println(prefix + ProgressUtil.toString(progress));
			}

			@Override
			public void maxUpdate(Value maxValue) {
				printer.println("Progress finished when reaches " + maxValue);
			}
		});
	}

	public static <Value extends Number> void displayProgressOnOutputStream(
			final Progress<Value> progress, OutputStream stream) {
		displayProgressOnOutputStream(null, progress, stream);
	}

	/**
	 * Manage the display of a {@link Progress} instance on an
	 * {@link OutputStream}. The display occurs on a regular basis by specifying
	 * a period.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param stream
	 *            the {@link OutputStream} on which it should be displayed
	 * @param period
	 *            the period of display in milliseconds
	 */
	public static <Value extends Number> void displayProgressOnOutputStream(
			final Progress<Value> progress, OutputStream stream,
			final long period, final boolean displayTermination) {
		if (progress == null) {
			throw new NullPointerException("No progress provided");
		} else if (stream == null) {
			throw new NullPointerException("No stream provided");
		} else if (period <= 0) {
			throw new NullPointerException(
					"The period should be strictly positive");
		} else {
			final PrintStream printer = new PrintStream(stream);
			progress.addProgressListener(new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					// nothing to do
				}

				@Override
				public void maxUpdate(Value maxValue) {
					printer.println("Progress finished when reaches "
							+ maxValue);
				}
			});

			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (printer) {
						printer.println(ProgressUtil.toString(progress));
						boolean isFinished = false;
						do {
							try {
								printer.wait(period);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							Value value = progress.getCurrentValue();
							Value max = progress.getMaxValue();
							if (max != null && value.equals(max)) {
								isFinished = true;
								if (displayTermination) {
									printer.println(ProgressUtil
											.toString(progress));
								} else {
									// do not display termination
								}
							} else {
								printer.println(ProgressUtil.toString(progress));
							}
						} while (!isFinished);
					}
				}
			}).start();
		}
	}

	/**
	 * This method is a facility to obtain the current status of a
	 * {@link Progress} into a {@link String}. It can be used for
	 * {@link Object#toString()} methods or for any other display purpose.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @return a string representation showing the current state of the
	 *         {@link Progress}
	 */
	public static <Value extends Number> String toString(
			Progress<Value> progress) {
		Value value = progress.getCurrentValue();
		Value max = progress.getMaxValue();
		if (max == null) {
			return value + "/?";
		} else {
			int percent = computeIntegerPercentage(value, max);
			return value + "/" + max + " (" + percent + "%)";
		}
	}

	/**
	 * This method is the usual computation of percentage for {@link Progress}
	 * instances, which takes the current and maximum {@link Value} and infer
	 * the integer percentage within [0;100]. The percentage is floored, meaning
	 * that the percentage reach a given value when this value has been actually
	 * reached. For instance, it reaches 100 only when the task is actually
	 * finished (value = max).
	 * 
	 * @param value
	 * @param max
	 * @return the integer percentage in [0;100]
	 */
	public static <Value extends Number> int computeIntegerPercentage(
			Value value, Value max) {
		return (int) Math.floor(100 * value.doubleValue() / max.doubleValue());
	}
}
