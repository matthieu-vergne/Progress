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
	 * The {@link Displayer} is the core concept behind the idea of displaying
	 * the status of a {@link Progress}. Several methods of {@link ProgressUtil}
	 * provides facilities to display some {@link Progress} in some places, but
	 * they are all based on the use of one or several {@link Displayer}s.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static interface Displayer {
		public <Value extends Number> void display(Progress<Value> progress);
	}

	/**
	 * This {@link Displayer} is a special one which does not display anything.
	 * It can be used to explicitly say that no {@link Displayer} should be
	 * used, rather than providing <code>null</code> (which is usually
	 * interpreted as a mistake and generates {@link NullPointerException}s).
	 */
	public static final Displayer NO_DISPLAYER = new Displayer() {

		@Override
		public <Value extends Number> void display(Progress<Value> progress) {
			// no display
		}

	};

	/**
	 * This method is the core method for displaying the status of a
	 * {@link Progress} each time it is updated. Use a custom {@link Displayer}
	 * to tell how this {@link Progress} should be displayed depending on the
	 * kind of update occurring.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param currentUpdateDisplayer
	 *            the {@link Displayer} for updates on current {@link Value}
	 * @param maxUpdateDisplayer
	 *            the {@link Displayer} for updates on max {@link Value}
	 */
	public static <Value extends Number> void displayProgress(
			final Progress<Value> progress,
			final Displayer currentUpdateDisplayer,
			final Displayer maxUpdateDisplayer) {
		if (progress == null) {
			throw new NullPointerException("No progress provided");
		} else if (currentUpdateDisplayer == null) {
			throw new NullPointerException("No current displayer provided");
		} else if (maxUpdateDisplayer == null) {
			throw new NullPointerException("No max displayer provided");
		} else {
			progress.addProgressListener(new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					currentUpdateDisplayer.display(progress);
				}

				@Override
				public void maxUpdate(Value maxValue) {
					maxUpdateDisplayer.display(progress);
				}
			});
		}
	}

	/**
	 * This method is the core method for displaying the status of a
	 * {@link Progress} on a regular basis. Additionally, one can specify a
	 * {@link Displayer} to use on launch (called soon after this method is
	 * called) as well as a {@link Displayer} to use on termination (called when
	 * the {@link Progress} is noticed to be finished).
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param period
	 *            the period of display
	 * @param launchDisplayer
	 *            the {@link Displayer} to use on launch
	 * @param regulardisplayer
	 *            the {@link Displayer} to use at each period
	 * @param terminationDisplayer
	 *            the {@link Displayer} to use on termination
	 */
	public static <Value extends Number> void displayProgress(
			final Progress<Value> progress, final long period,
			final Displayer launchDisplayer, final Displayer regulardisplayer,
			final Displayer terminationDisplayer) {
		if (progress == null) {
			throw new NullPointerException("No progress provided");
		} else if (regulardisplayer == null) {
			throw new NullPointerException("No regular displayer provided");
		} else if (launchDisplayer == null) {
			throw new NullPointerException("No launch displayer provided");
		} else if (terminationDisplayer == null) {
			throw new NullPointerException("No termination displayer provided");
		} else if (period <= 0) {
			throw new NullPointerException(
					"The period should be strictly positive");
		} else {
			final ProgressListener<Value> listener = new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					notifyUponTermination();
				}

				@Override
				public void maxUpdate(Value maxValue) {
					notifyUponTermination();
				}

				private void notifyUponTermination() {
					synchronized (progress) {
						if (progress.isFinished()) {
							progress.notifyAll();
						} else {
							// let the waiting run
						}
					}
				}

			};
			progress.addProgressListener(listener);

			new Thread(new Runnable() {

				@Override
				public void run() {
					synchronized (progress) {
						launchDisplayer.display(progress);

						do {
							try {
								progress.wait(period);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							if (!progress.isFinished()) {
								regulardisplayer.display(progress);
							} else {
								terminationDisplayer.display(progress);
							}
						} while (!progress.isFinished());
						progress.removeProgressListener(listener);
					}
				}
			}).start();
		}
	}

	/**
	 * This method is a simplified method for displaying the status of a
	 * {@link Progress} on a regular basis. No display is made on launch nor
	 * termination. To exploit such features, use the extended
	 * {@link #displayProgress(Progress, long, Displayer, Displayer, Displayer)}
	 * and provide the required arguments. This simplified method call the
	 * extended one by providing {@link #NO_DISPLAYER} as a launch and
	 * termination {@link Displayer}.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param period
	 *            the period of display
	 * @param regulardisplayer
	 *            the {@link Displayer} to use at each period
	 */
	public static <Value extends Number> void displayProgress(
			final Progress<Value> progress, final long period,
			final Displayer regulardisplayer) {
		displayProgress(progress, period, NO_DISPLAYER, regulardisplayer,
				NO_DISPLAYER);
	}

	/**
	 * Manage the display of a {@link Progress} instance on an
	 * {@link OutputStream}. The display occurs each time the {@link Progress}
	 * is updated. For more control on the display, you can use
	 * {@link #displayProgress(Progress, Displayer, Displayer)}.
	 * 
	 * @param prefix
	 *            a prefix to add to each display
	 * @param progress
	 *            the {@link Progress} to display
	 * @param stream
	 *            the {@link OutputStream} on which it should be displayed
	 */
	public static <Value extends Number> void displayProgressOnOutputStream(
			final String prefix, final Progress<Value> progress,
			OutputStream stream) {
		final PrintStream printer = new PrintStream(stream);
		displayProgress(progress, new Displayer() {

			@Override
			public <V extends Number> void display(Progress<V> progress) {
				printer.println(prefix + ProgressUtil.toString(progress));
			}
		}, new Displayer() {

			@Override
			public <V extends Number> void display(Progress<V> progress) {
				printer.println("Progress finished when reaches "
						+ progress.getMaxValue());
			}
		});
	}

	/**
	 * Display a {@link Progress} on an {@link OutputStream}. The display occurs
	 * each time the {@link Progress} is updated. For more control on the
	 * display, you can use
	 * {@link #displayProgress(Progress, Displayer, Displayer)}.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param stream
	 *            the {@link OutputStream} on which it should be displayed
	 */
	public static <Value extends Number> void displayProgressOnOutputStream(
			final Progress<Value> progress, OutputStream stream) {
		displayProgressOnOutputStream("", progress, stream);
	}

	/**
	 * Display a {@link Progress} on an {@link OutputStream}. The display occurs
	 * on a regular basis by specifying a period as well as on launch and
	 * termination if requested.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param stream
	 *            the {@link OutputStream} on which it should be displayed
	 * @param period
	 *            the period of display in milliseconds
	 * @param isLaunchDisplayed
	 *            <code>true</code> if the display should occur immediately
	 *            after the call, <code>false</code> otherwise
	 * @param isTerminationDisplayed
	 *            <code>true</code> if the display should occur as soon as we
	 *            notice that the progress is finished, <code>false</code>
	 *            otherwise
	 */
	public static <Value extends Number> void displayProgressOnOutputStream(
			final Progress<Value> progress, OutputStream stream,
			final long period, boolean isLaunchDisplayed,
			boolean isTerminationDisplayed) {
		final PrintStream printer = new PrintStream(stream);
		Displayer statusDisplayer = new Displayer() {

			@Override
			public <V extends Number> void display(Progress<V> progress) {
				printer.println(ProgressUtil.toString(progress));
			}
		};
		displayProgress(progress, period, isLaunchDisplayed ? statusDisplayer
				: NO_DISPLAYER, statusDisplayer,
				isTerminationDisplayed ? statusDisplayer : NO_DISPLAYER);
	}

	/**
	 * Display a {@link Progress} on an {@link OutputStream}. The display occurs
	 * on a regular basis by specifying a period. The display starts after the
	 * first period has been passed (not immediately), and the display is
	 * stopped when the {@link Progress} finishes (no final display occurs).
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param stream
	 *            the {@link OutputStream} on which it should be displayed
	 * @param period
	 *            the period of display in milliseconds
	 * @param isLaunchDisplayed
	 *            <code>true</code> if the display should occur immediately
	 *            after the call, <code>false</code> otherwise
	 * @param isTerminationDisplayed
	 *            <code>true</code> if the display should occur as soon as we
	 *            notice that the progress is finished, <code>false</code>
	 *            otherwise
	 */
	public static <Value extends Number> void displayProgressOnOutputStream(
			final Progress<Value> progress, OutputStream stream,
			final long period) {
		displayProgressOnOutputStream(progress, stream, period, false, false);
	}

	/**
	 * @deprecated Use
	 *             {@link #displayProgressOnOutputStream(Progress, OutputStream, long, boolean, boolean)}
	 *             if you want to keep the control on the termination display,
	 *             otherwise you can simply discard it by using the simplified
	 *             version
	 *             {@link #displayProgressOnOutputStream(Progress, OutputStream, long)}
	 *             .
	 */
	@Deprecated
	public static <Value extends Number> void displayProgressOnOutputStream(
			final Progress<Value> progress, OutputStream stream,
			final long period, boolean isTerminationDisplayed) {
		displayProgressOnOutputStream(progress, stream, period, false,
				isTerminationDisplayed);
	}

	/**
	 * Create a {@link JProgressBar} to display a {@link Progress} instance. The
	 * {@link JProgressBar} returned can be added to a dialog or a frame.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @return the {@link JProgressBar} displaying the {@link Progress}
	 */
	public static <Value extends Number> JProgressBar createJProgressBar(
			final Progress<Value> progress) {
		final JProgressBar bar = new JProgressBar();
		bar.setStringPainted(true);
		bar.setValue(0);
		configureBarMaximum(progress, bar);
		bar.setString(toString(progress));

		progress.addProgressListener(new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				// we multiply by 100 to manage decimals
				bar.setValue((int) (value.doubleValue() * 100));
				bar.setString(ProgressUtil.toString(progress));
			}

			@Override
			public void maxUpdate(Value maxValue) {
				configureBarMaximum(progress, bar);
				bar.setString(ProgressUtil.toString(progress));
			}
		});
		return bar;
	}

	/**
	 * @deprecated Use {@link #createJProgressBar(Progress)}.
	 */
	@Deprecated
	public static <Value extends Number> JProgressBar displayProgressOnBar(
			final Progress<Value> progress) {
		return createJProgressBar(progress);
	}

	/**
	 * Create a {@link JDialog} to display a simple progress bar. The
	 * {@link JDialog} created is returned for further interaction, like
	 * changing the title to better explicit the current status of the monitored
	 * task.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 * @param openOnCreation
	 *            <code>true</code> if the {@link JDialog} should be set visible
	 *            upon creation, <code>false</code> to let the user display it
	 *            manually
	 * @param closeOnTermination
	 *            <code>true</code> if the dialog should be automatically
	 *            disposed upon {@link Progress} termination, <code>false</code>
	 *            if it should be closed manually
	 * @return the {@link JDialog} displaying the {@link Progress}
	 */
	public static <Value extends Number> JDialog createJDialog(
			final Progress<Value> progress, boolean openOnCreation,
			final boolean closeOnTermination) {
		final JDialog dialog = new JDialog();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setTitle("Running...");
		dialog.setLayout(new GridLayout(1, 1));

		JProgressBar bar = createJProgressBar(progress);
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

		if (openOnCreation) {
			dialog.setVisible(true);
		} else {
			// do not display yet
		}
		return dialog;
	}

	/**
	 * Create a {@link JDialog} to display a simple progress bar. The
	 * {@link JDialog} is displayed immediately upon creation, and closed as
	 * soon as the {@link Progress} is finished. If you want to have a better
	 * control on this {@link JDialog}, you can consider
	 * {@link #createJDialog(Progress, boolean, boolean)}.
	 * 
	 * @param progress
	 *            the {@link Progress} to display
	 */
	public static <Value extends Number> void displayProgressOnJDialog(
			final Progress<Value> progress) {
		createJDialog(progress, true, true);
	}

	/**
	 * @deprecated Use {@link #createJDialog(Progress, boolean)}.
	 */
	@Deprecated
	public static <Value extends Number> JDialog displayProgressOnDialog(
			final Progress<Value> progress, final boolean closeOnTermination) {
		return createJDialog(progress, true, closeOnTermination);
	}

	/*******************************************************************/
	/*******************************************************************/
	/*******************************************************************/
	/*******************************************************************/
	/*******************************************************************/
	/*******************************************************************/

	private static <Value extends Number> void configureBarMaximum(
			Progress<Value> progress, JProgressBar bar) {
		Value max = progress.getMaxValue();
		if (max == null) {
			bar.setIndeterminate(true);
			bar.setMaximum(Integer.MAX_VALUE);
		} else {
			bar.setIndeterminate(false);
			// we multiply by 100 to manage decimals
			bar.setMaximum(max.intValue() * 100);
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
			return reduceDecimals(value, 3) + "/?";
		} else {
			int percent = computeIntegerPercentage(value, max);
			return reduceDecimals(value, 3) + "/" + reduceDecimals(max, 3) + " (" + percent + "%)";
		}
	}
	
	private static String reduceDecimals(Number value, int decimals) {
		String string = ""+value;
		string = string.replaceAll("(\\.\\d{"+decimals+"})\\d+", "$1");
		string = string.replaceAll("(\\.\\d*)0+$", "$1");
		string = string.replaceAll("\\.$", "");
		return string;
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

	/**
	 * Shortcut to {@link #computeIntegerPercentage(Number, Number)} applied on
	 * a given {@link Progress}.
	 */
	public static <Value extends Number> int computeIntegerPercentage(
			Progress<Value> progress) {
		return computeIntegerPercentage(progress.getCurrentValue(),
				progress.getMaxValue());
	}
}
