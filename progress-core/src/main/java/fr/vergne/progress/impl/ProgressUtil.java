package fr.vergne.progress.impl;

import java.awt.GridLayout;
import java.io.OutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import javax.swing.JDialog;
import javax.swing.JProgressBar;

import fr.vergne.progress.Predictor;
import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.impl.PredictorFactory.UnableToPredictException;

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
			return reduceDecimals(value, 3) + "/" + reduceDecimals(max, 3)
					+ " (" + percent + "%)";
		}
	}

	private static String reduceDecimals(Number value, int decimals) {
		String string = "" + value;
		string = string.replaceAll("(\\.\\d{" + decimals + "})\\d+", "$1");
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

	/**
	 * This method is a facility to build a {@link ValueTranslator} depending on
	 * the type of {@link Value} to manage. In particular, all {@link Number}s
	 * are not added by simply using "v1 + v2" and some requires more specific
	 * uses, like {@link BigInteger} which needs to use "v1.add(v2)". This
	 * method allows to abstract from these details by providing the right
	 * {@link ValueTranslator} at runtime.
	 * 
	 * @param value
	 *            an example of {@link Value} to compute
	 * @return a {@link ValueTranslator} able to do various operations on
	 *         {@link Value}s of the same {@link Class}
	 */
	@SuppressWarnings("unchecked")
	public static <Value extends Number> ValueTranslator<Value> createValueTranslator(
			Value value) {
		if (value == null) {
			throw new NullPointerException(
					"Cannot choose the right value computer with a null value");
		} else if (value instanceof Integer) {
			return (ValueTranslator<Value>) new ValueTranslator<Integer>() {
				@Override
				public BigDecimal toDecimal(Integer value) {
					return new BigDecimal(value.toString());
				}

				@Override
				public Integer toValue(BigDecimal decimal) {
					return decimal.intValue();
				}
			};
		} else if (value instanceof Long) {
			return (ValueTranslator<Value>) new ValueTranslator<Long>() {
				@Override
				public BigDecimal toDecimal(Long value) {
					return new BigDecimal(value.toString());
				}

				@Override
				public Long toValue(BigDecimal decimal) {
					return decimal.longValue();
				}
			};
		} else if (value instanceof Short) {
			return (ValueTranslator<Value>) new ValueTranslator<Short>() {
				@Override
				public BigDecimal toDecimal(Short value) {
					return new BigDecimal(value.toString());
				}

				@Override
				public Short toValue(BigDecimal decimal) {
					return decimal.shortValue();
				}
			};
		} else if (value instanceof Byte) {
			return (ValueTranslator<Value>) new ValueTranslator<Byte>() {
				@Override
				public BigDecimal toDecimal(Byte value) {
					return new BigDecimal(value.toString());
				}

				@Override
				public Byte toValue(BigDecimal decimal) {
					return decimal.byteValue();
				}
			};
		} else if (value instanceof Float) {
			return (ValueTranslator<Value>) new ValueTranslator<Float>() {
				@Override
				public BigDecimal toDecimal(Float value) {
					return new BigDecimal(value.toString());
				}

				@Override
				public Float toValue(BigDecimal decimal) {
					return decimal.floatValue();
				}
			};
		} else if (value instanceof Double) {
			return (ValueTranslator<Value>) new ValueTranslator<Double>() {
				@Override
				public BigDecimal toDecimal(Double value) {
					return new BigDecimal(value.toString());
				}

				@Override
				public Double toValue(BigDecimal decimal) {
					return decimal.doubleValue();
				}
			};
		} else if (value instanceof BigInteger) {
			return (ValueTranslator<Value>) new ValueTranslator<BigInteger>() {
				@Override
				public BigDecimal toDecimal(BigInteger value) {
					return new BigDecimal(value);
				}

				@Override
				public BigInteger toValue(BigDecimal decimal) {
					return decimal.toBigInteger();
				}
			};
		} else if (value instanceof BigDecimal) {
			return (ValueTranslator<Value>) new ValueTranslator<BigDecimal>() {
				@Override
				public BigDecimal toDecimal(BigDecimal value) {
					return value;
				}

				@Override
				public BigDecimal toValue(BigDecimal decimal) {
					return decimal;
				}
			};
		} else {
			throw new RuntimeException("Unmanaged type: " + value.getClass());
		}
	}

	/**
	 * A {@link Progress} is often based on computing {@link Value}s, whether we
	 * simply add them to make a {@link Progress} advance, or for a
	 * {@link Predictor} to be able to compute new {@link Value}s. A
	 * {@link ValueTranslator} allows to switch between a {@link Value} and a
	 * {@link BigDecimal}, which is the most extended {@link Number}
	 * implementation (able to deal with any arbitrary precision), thus the most
	 * suited for computation.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Value>
	 */
	public static interface ValueTranslator<Value extends Number> {
		public Value toValue(BigDecimal decimal);

		public BigDecimal toDecimal(Value value);
	}

	/**
	 * This method provides a simple way to estimate the termination time of a
	 * {@link Progress}. Basically, a {@link Progress} only provides current and
	 * max values, but based on their evolution we can predict when they will
	 * become equal by using {@link Predictor}s. If you use the right
	 * {@link Predictor} for the current value (<i>current(t)</i>) and the right
	 * one for the max value (<i>max(t)</i>), then you can obtain a fairly
	 * reliable prediction of the termination time.<br/>
	 * <br/>
	 * This method uses the <a
	 * href="https://en.wikipedia.org/wiki/Secant_method">secant method</a> to
	 * find a zero for a function. In particular, we try here to find a zero for
	 * <i>f(t) = max(t) - current(t)</i>, so we try to find <i>t</i> such that
	 * <i>max(t) = current(t)</i>.
	 * 
	 * @param currentPredictor
	 *            the {@link Predictor} for the current value of the
	 *            {@link Progress}
	 * @param maxPredictor
	 *            the {@link Predictor} for the max value of the
	 *            {@link Progress}
	 * @return the timestamp at which we expect the progress to finish
	 */
	public static <Value extends Number> long predictTerminationTime(
			Predictor<Value> currentPredictor, Predictor<Value> maxPredictor) {
		Value referenceValue = currentPredictor.predictValueAt(System
				.currentTimeMillis());
		ValueTranslator<Value> translator = createValueTranslator(referenceValue);

		long t1 = System.currentTimeMillis();
		BigDecimal diff1 = computeDiff(currentPredictor, maxPredictor,
				translator, t1);

		long t2 = t1 + 1;
		BigDecimal diff2 = computeDiff(currentPredictor, maxPredictor,
				translator, t2);

		while (diff1.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal dDiff = diff2.subtract(diff1).divide(
					new BigDecimal(t2 - t1), 20, RoundingMode.HALF_UP);

			if (dDiff.compareTo(BigDecimal.ZERO) == 0) {
				t2 += t2 != t1 ? t2 - t1 : 1;
			} else {
				long nextT = t1
						- diff1.divide(dDiff, 20, RoundingMode.HALF_UP)
								.longValue();

				t1 = t2;
				diff1 = diff2;

				t2 = nextT;
			}

			if (t2 == t1) {
				throw new UnableToPredictException(
						"Extreme case reach, avoid further computation");
			} else {
				diff2 = computeDiff(currentPredictor, maxPredictor, translator,
						t2);
			}
		}

		return t1;
	}

	private static <Value extends Number> BigDecimal computeDiff(
			Predictor<Value> currentPredictor, Predictor<Value> maxPredictor,
			ValueTranslator<Value> translator, long time) {
		BigDecimal current = translator.toDecimal(currentPredictor
				.predictValueAt(time));
		BigDecimal max = translator
				.toDecimal(maxPredictor.predictValueAt(time));
		BigDecimal diff = max.subtract(current);
		return diff;
	}
}
