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
	public static <T extends Number> JDialog displayProgressOnDialog(
			final Progress<T> progress, final boolean closeOnTermination) {
		final JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Running...");
		dialog.setLayout(new GridLayout(1, 1));

		JProgressBar bar = displayProgressOnBar(progress);
		dialog.add(bar);

		progress.addProgressListener(new ProgressListener<T>() {

			@Override
			public void currentUpdate(T value) {
				checkAutoClose();
			}

			@Override
			public void maxUpdate(T maxValue) {
				checkAutoClose();
			}

			private void checkAutoClose() {
				if (closeOnTermination) {
					T value = progress.getCurrentValue();
					T max = progress.getMaxValue();
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
	public static <T extends Number> JProgressBar displayProgressOnBar(
			final Progress<T> progress) {
		final JProgressBar bar = new JProgressBar();
		bar.setStringPainted(true);
		bar.setString("");
		bar.setValue(0);
		configureBarMaximum(progress, bar);
		updateString(progress, bar);

		progress.addProgressListener(new ProgressListener<T>() {

			@Override
			public void currentUpdate(T value) {
				bar.setValue(value.intValue());
				updateString(progress, bar);
			}

			@Override
			public void maxUpdate(T maxValue) {
				configureBarMaximum(progress, bar);
				updateString(progress, bar);
			}
		});
		return bar;
	}

	private static <T extends Number> void updateString(Progress<T> progress,
			JProgressBar bar) {
		T value = progress.getCurrentValue();
		T max = progress.getMaxValue();
		if (max == null) {
			bar.setString(value.toString());
		} else {
			int percent = computePercent(value, max);
			bar.setString(value + "/" + max + " (" + percent + "%)");
		}
	}

	private static <T extends Number> void configureBarMaximum(
			Progress<T> progress, JProgressBar bar) {
		T max = progress.getMaxValue();
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
	public static <T extends Number> void displayProgressOnOutputStream(
			final Progress<T> progress, OutputStream stream) {
		final PrintStream printer = new PrintStream(stream);
		progress.addProgressListener(new ProgressListener<T>() {

			@Override
			public void currentUpdate(T value) {
				writeProgress(progress, printer);
			}

			@Override
			public void maxUpdate(T maxValue) {
				printer.println("Progress finished when reaches " + maxValue);
			}
		});
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
	public static <T extends Number> void displayProgressOnOutputStream(
			final Progress<T> progress, OutputStream stream, final long period,
			final boolean displayTermination) {
		if (progress == null) {
			throw new NullPointerException("No progress provided");
		} else if (stream == null) {
			throw new NullPointerException("No stream provided");
		} else if (period <= 0) {
			throw new NullPointerException(
					"The period should be strictly positive");
		} else {
			final PrintStream printer = new PrintStream(stream);
			progress.addProgressListener(new ProgressListener<T>() {

				@Override
				public void currentUpdate(T value) {
					// nothing to do
				}

				@Override
				public void maxUpdate(T maxValue) {
					printer.println("Progress finished when reaches "
							+ maxValue);
				}
			});

			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (printer) {
						writeProgress(progress, printer);
						boolean isFinished = false;
						do {
							try {
								printer.wait(period);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							T value = progress.getCurrentValue();
							T max = progress.getMaxValue();
							if (max != null && value.equals(max)) {
								isFinished = true;
								if (displayTermination) {
									writeProgress(progress, printer);
								} else {
									// do not display termination
								}
							} else {
								writeProgress(progress, printer);
							}
						} while (!isFinished);
					}
				}
			}).start();
		}
	}

	private static <T extends Number> void writeProgress(Progress<T> progress,
			PrintStream printer) {
		T value = progress.getCurrentValue();
		T max = progress.getMaxValue();
		if (max == null) {
			printer.println(value);
		} else {
			int percent = computePercent(value, max);
			printer.println(value + "/" + max + " (" + percent + "%)");
		}
	}

	private static <T extends Number> int computePercent(T value, T max) {
		return (int) Math.floor(100 * value.doubleValue() / max.doubleValue());
	}
}
