package fr.vergne.progress.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.impl.ProgressUtil.ValueTranslator;

/**
 * This {@link ProgressFactory} provides facilities to create different kinds of
 * {@link Progress} instances.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class ProgressFactory {

	/**
	 * Creates a {@link Progress} instance which can be manually updated. See
	 * the Javadoc of {@link ManualProgress} for further details.
	 * 
	 * @param startValue
	 *            the initial {@link Value} of the {@link Progress}
	 * @param maxValue
	 *            the max {@link Value} of the {@link Progress} (can be
	 *            <code>null</code>)
	 * @return the {@link Progress} instance
	 */
	public <Value extends Number> ManualProgress<Value> createManualProgress(
			Value startValue, Value maxValue) {
		return new ManualProgress<Value>(startValue, maxValue);
	}

	/**
	 * Creates a {@link Progress} instance which can be only in two states:
	 * finished or not. See the Javadoc of {@link BinaryProgress} for further
	 * details.
	 * 
	 * @return the {@link Progress} instance
	 */
	public BinaryProgress createBinaryProgress() {
		return new BinaryProgress();
	}

	/**
	 * Creates a {@link Progress} instance which should be manually updated via
	 * a {@link ProgressSetter} provided in argument.
	 * 
	 * @param setter
	 *            the setter used to update the {@link Progress}
	 * @param startValue
	 *            the initial {@link Value} of the {@link Progress}
	 * @param maxValue
	 *            the max {@link Value} of the {@link Progress} (can be
	 *            <code>null</code>)
	 * @return the {@link Progress} instance
	 * @deprecated This method has been deprecated to prefer the use of
	 *             {@link #createManualProgress(Number, Number)}.
	 */
	@Deprecated
	public <Value extends Number> Progress<Value> createManualProgress(
			final ProgressSetter<Value> setter, Value startValue, Value maxValue) {
		final List<Value> values = new ArrayList<Value>(2);
		values.add(startValue);
		values.add(maxValue);
		final Collection<ProgressListener<? super Value>> listeners = new HashSet<>();

		final ProgressListener<Value> listener = new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				Value max = values.get(1);
				if (max != null && value.doubleValue() > max.doubleValue()) {
					throw new IllegalArgumentException("Value higher than max:"
							+ value + " > " + max);
				} else {
					values.set(0, value);
					for (ProgressListener<? super Value> listener : listeners) {
						listener.currentUpdate(value);
					}
				}
			}

			@Override
			public void maxUpdate(Value maxValue) {
				values.set(1, maxValue);
				for (ProgressListener<? super Value> listener : listeners) {
					listener.maxUpdate(maxValue);
				}
			}
		};
		setter.addProgressListener(listener);

		return new Progress<Value>() {

			@Override
			public Value getCurrentValue() {
				return values.get(0);
			}

			@Override
			public Value getMaxValue() {
				return values.get(1);
			}

			@Override
			public void addProgressListener(
					ProgressListener<? super Value> listener) {
				listeners.add(listener);
			}

			@Override
			public void removeProgressListener(
					ProgressListener<? super Value> listener) {
				listeners.remove(listener);
			}

			@Override
			protected void finalize() throws Throwable {
				setter.removeProgressListener(listener);
			}

			@Override
			public String toString() {
				return ProgressUtil.DEFAULT_FORMATTER.format(this);
			}
		};
	}

	/**
	 * This class is used by the method
	 * {@link ProgressFactory#createManualProgress(ProgressSetter, Number, Number)}
	 * which has been deprecated. Please refer to this method's Javadoc to know
	 * how to replace it.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Value>
	 * @deprecated See
	 *             {@link ProgressFactory#createManualProgress(ProgressSetter, Number, Number)}
	 */
	@Deprecated
	public static class ProgressSetter<Value extends Number> {
		private final Collection<ProgressListener<Value>> listeners = new HashSet<ProgressListener<Value>>();

		public void setCurrentValue(Value value) {
			for (ProgressListener<Value> listener : listeners) {
				listener.currentUpdate(value);
			}
		}

		public void setMaxValue(Value value) {
			for (ProgressListener<Value> listener : listeners) {
				listener.maxUpdate(value);
			}
		}

		private void addProgressListener(ProgressListener<Value> listener) {
			listeners.add(listener);
		}

		private void removeProgressListener(ProgressListener<Value> listener) {
			listeners.remove(listener);
		}

	}

	/**
	 * @deprecated Use {@link #createGlobalAdditiveProgress(Collection)} to have
	 *             exactly the same behavior, or another
	 *             <code>createGlobalXxxProgress()</code> For a different kind
	 *             of global {@link Progress}.
	 */
	@Deprecated
	public <Value extends Number> Progress<Value> createCombinedProgress(
			final Collection<? extends Progress<Value>> subProgresses) {
		return createGlobalAdditiveProgress(subProgresses);
	}

	/**
	 * This method aims at providing a global {@link Progress} over a collection
	 * of more specific {@link Progress} instances by adding them: the current
	 * {@link Value} of the global {@link Progress} equals the sum of the
	 * current {@link Value}s of each sub-{@link Progress}. Consequently, the
	 * max {@link Value} of the global {@link Progress} is also the sum of the
	 * max {@link Value}s of each of them, and it finishes only when they are
	 * all finished.
	 * 
	 * @param subProgresses
	 *            all the {@link Progress} instances to cover
	 * @return the global {@link Progress}
	 */
	public <Value extends Number> Progress<Value> createGlobalAdditiveProgress(
			final Collection<? extends Progress<Value>> subProgresses) {
		if (subProgresses == null || subProgresses.isEmpty()) {
			throw new IllegalArgumentException("No sub-progresses provided: "
					+ subProgresses);
		} else {
			final Collection<Progress<Value>> fixedProgresses = new LinkedList<Progress<Value>>(
					subProgresses);

			final List<Value> values = new ArrayList<Value>(2);
			values.add(computeAdditiveCurrentValue(fixedProgresses));
			values.add(computeAdditiveMaxValue(fixedProgresses));

			final Collection<ProgressListener<? super Value>> listeners = new HashSet<>();

			final ProgressListener<Value> globalListener = new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					Value globalValue = computeAdditiveCurrentValue(fixedProgresses);
					values.set(0, globalValue);
					for (ProgressListener<? super Value> listener : listeners) {
						listener.currentUpdate(globalValue);
					}
				}

				@Override
				public void maxUpdate(Value maxValue) {
					Value globalValue = computeAdditiveMaxValue(fixedProgresses);
					values.set(1, globalValue);
					for (ProgressListener<? super Value> listener : listeners) {
						listener.maxUpdate(globalValue);
					}
				}
			};

			for (Progress<Value> subprogress : fixedProgresses) {
				subprogress.addProgressListener(globalListener);
			}

			return new Progress<Value>() {

				@Override
				public Value getCurrentValue() {
					return values.get(0);
				}

				@Override
				public Value getMaxValue() {
					return values.get(1);
				}

				@Override
				public void addProgressListener(
						ProgressListener<? super Value> listener) {
					listeners.add(listener);
				}

				@Override
				public void removeProgressListener(
						ProgressListener<? super Value> listener) {
					listeners.remove(listener);
				}

				@Override
				protected void finalize() throws Throwable {
					for (Progress<Value> subprogress : fixedProgresses) {
						subprogress.removeProgressListener(globalListener);
					}
				}
			};
		}
	}

	/**
	 * This method aims at providing a global {@link Progress} over a collection
	 * of more specific {@link Progress} instances by counting them: the current
	 * {@link Value} of the global {@link Progress} sums the progress ratio of
	 * each sub-{@link Progress} (current {@link Value} divided by max
	 * {@link Value}), such that a finished sub-{@link Progress} adds exactly 1.
	 * Consequently, the max {@link Value} of the global {@link Progress} is the
	 * number of sub-{@link Progress} instances managed, and it finishes only
	 * when all the sub-{@link Progress} instances are finished.
	 * 
	 * @param subProgresses
	 *            all the {@link Progress} instances to cover
	 * @return the global {@link Progress}
	 */
	public Progress<Double> createGlobalCountingProgress(
			Collection<? extends Progress<? extends Number>> subProgresses) {
		if (subProgresses == null || subProgresses.isEmpty()) {
			throw new IllegalArgumentException("No sub-progresses provided: "
					+ subProgresses);
		} else {
			final Collection<Progress<? extends Number>> fixedProgresses = new LinkedList<Progress<? extends Number>>(
					subProgresses);

			final List<Double> values = new ArrayList<Double>(2);
			values.add(computeCountingCurrentValue(fixedProgresses));
			values.add((double) fixedProgresses.size());

			final Collection<ProgressListener<? super Double>> listeners = new HashSet<>();

			final ProgressListener<Number> globalListener = new ProgressListener<Number>() {

				@Override
				public void currentUpdate(Number value) {
					double globalValue = computeCountingCurrentValue(fixedProgresses);
					values.set(0, globalValue);
					for (ProgressListener<? super Double> listener : listeners) {
						listener.currentUpdate(globalValue);
					}
				}

				@Override
				public void maxUpdate(Number maxValue) {
					// The current value should change due to ratios
					currentUpdate(null);
					// But surely the max value remains constant
					// so no computation/notification
				}
			};

			final ProgressListenerMap listenerMap = new ProgressListenerMap();
			for (Progress<? extends Number> subprogress : fixedProgresses) {
				listen(subprogress, globalListener, listenerMap);
			}

			return new Progress<Double>() {

				@Override
				public Double getCurrentValue() {
					return values.get(0);
				}

				@Override
				public Double getMaxValue() {
					return values.get(1);
				}

				@Override
				public void addProgressListener(
						ProgressListener<? super Double> listener) {
					listeners.add(listener);
				}

				@Override
				public void removeProgressListener(
						ProgressListener<? super Double> listener) {
					listeners.remove(listener);
				}

				@Override
				protected void finalize() throws Throwable {
					for (Progress<? extends Number> subprogress : fixedProgresses) {
						unlisten(subprogress, listenerMap);
					}
				}
			};
		}
	}

	private <Value extends Number> void unlisten(Progress<Value> subprogress,
			ProgressListenerMap listenerMap) {
		subprogress.removeProgressListener(listenerMap.get(subprogress));
	}

	private <Value extends Number> void listen(Progress<Value> subprogress,
			final ProgressListener<Number> globalListener,
			ProgressListenerMap listenerMap) {
		ProgressListener<Value> listener = new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				globalListener.currentUpdate(value);
			}

			@Override
			public void maxUpdate(Value maxValue) {
				globalListener.maxUpdate(maxValue);
			}
		};
		subprogress.addProgressListener(listener);
		listenerMap.put(subprogress, listener);
	}

	private <Value extends Number> Value computeAdditiveCurrentValue(
			final Collection<? extends Progress<Value>> subProgresses) {
		List<Value> values = new LinkedList<Value>();
		for (Progress<Value> progress : subProgresses) {
			values.add(progress.getCurrentValue());
		}
		ValueTranslator<Value> translator = ProgressUtil
				.createValueTranslator(values.get(0));
		return sum(values, translator);
	}

	private double computeCountingCurrentValue(
			final Collection<? extends Progress<? extends Number>> subProgresses) {
		BigDecimal value = BigDecimal.ZERO;
		for (Progress<? extends Number> progress : subProgresses) {
			Number actualMax = progress.getMaxValue();
			if (actualMax == null) {
				// add 0, so ignore
			} else {
				BigDecimal current = new BigDecimal(progress.getCurrentValue()
						.toString());
				BigDecimal max = new BigDecimal(actualMax.toString());
				value = value
						.add(current.divide(max, 20, RoundingMode.HALF_UP));
			}
		}
		return value.doubleValue();
	}

	private <Value extends Number> Value computeAdditiveMaxValue(
			final Collection<? extends Progress<Value>> subProgresses) {
		List<Value> values = new LinkedList<Value>();
		for (Progress<Value> progress : subProgresses) {
			values.add(progress.getMaxValue());
		}

		if (values.contains(null)) {
			return null;
		} else {
			ValueTranslator<Value> translator = ProgressUtil
					.createValueTranslator(values.get(0));
			return sum(values, translator);
		}
	}

	private <Value extends Number> Value sum(List<Value> values,
			ValueTranslator<Value> translator) {
		Iterator<Value> iterator = values.iterator();
		BigDecimal value = translator.toDecimal(iterator.next());
		while (iterator.hasNext()) {
			value = value.add(translator.toDecimal(iterator.next()));
		}
		return translator.toValue(value);
	}

	class ProgressListenerMap {
		private Map<Progress<? extends Number>, ProgressListener<? extends Number>> map = new HashMap<Progress<? extends Number>, Progress.ProgressListener<? extends Number>>();

		public <Value extends Number> void put(Progress<Value> key,
				ProgressListener<Value> value) {
			map.put(key, value);
		}

		@SuppressWarnings("unchecked")
		public <Value extends Number> ProgressListener<Value> get(
				Progress<Value> key) {
			return (ProgressListener<Value>) map.get(key);
		}
	}
}
