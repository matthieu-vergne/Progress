package fr.vergne.progress.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;

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
		final Collection<ProgressListener<Value>> listeners = new HashSet<ProgressListener<Value>>();

		final ProgressListener<Value> listener = new ProgressListener<Value>() {

			@Override
			public void currentUpdate(Value value) {
				Value max = values.get(1);
				if (max != null && value.doubleValue() > max.doubleValue()) {
					throw new IllegalArgumentException("Value higher than max:"
							+ value + " > " + max);
				} else {
					values.set(0, value);
					for (ProgressListener<Value> listener : listeners) {
						listener.currentUpdate(value);
					}
				}
			}

			@Override
			public void maxUpdate(Value maxValue) {
				values.set(1, maxValue);
				for (ProgressListener<Value> listener : listeners) {
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
			public boolean isFinished() {
				Value value = getCurrentValue();
				Value max = getMaxValue();
				return max != null && max.equals(value);
			}

			@Override
			public void addProgressListener(ProgressListener<Value> listener) {
				listeners.add(listener);
			}

			@Override
			public void removeProgressListener(ProgressListener<Value> listener) {
				listeners.remove(listener);
			}

			@Override
			protected void finalize() throws Throwable {
				setter.removeProgressListener(listener);
			}

			@Override
			public String toString() {
				return ProgressUtil.toString(this);
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
	 * This method aims at providing a global {@link Progress} over a collection
	 * of more specific {@link Progress} instances. The idea is that the current
	 * (resp. max) {@link Value} of this {@link Progress} equals the sum of the
	 * current (resp. max) {@link Value}s of each sub-{@link Progress}.
	 * consequently, the {@link Progress} returned is finished only when all the
	 * sub-{@link Progress} instances are also finished.
	 * 
	 * @param subProgresses
	 *            all the {@link Progress} instances to cover
	 * @return the global {@link Progress}
	 */
	public <Value extends Number> Progress<Value> createCombinedProgress(
			final Collection<? extends Progress<Value>> subProgresses) {
		if (subProgresses == null || subProgresses.isEmpty()) {
			throw new IllegalArgumentException("No sub-progresses provided: "
					+ subProgresses);
		} else {
			final List<Value> values = new ArrayList<Value>(2);
			values.add(computeCurrentValue(subProgresses));
			values.add(computeMaxValue(subProgresses));

			final Collection<ProgressListener<Value>> listeners = new HashSet<Progress.ProgressListener<Value>>();

			final ProgressListener<Value> globalListener = new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					Value globalValue = computeCurrentValue(subProgresses);
					values.set(0, globalValue);
					for (ProgressListener<Value> listener : listeners) {
						listener.currentUpdate(globalValue);
					}
				}

				@Override
				public void maxUpdate(Value maxValue) {
					Value globalValue = computeMaxValue(subProgresses);
					values.set(1, globalValue);
					for (ProgressListener<Value> listener : listeners) {
						listener.maxUpdate(globalValue);
					}
				}
			};

			for (Progress<Value> subprogress : subProgresses) {
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
				public boolean isFinished() {
					return getCurrentValue().equals(getMaxValue());
				}

				@Override
				public void addProgressListener(ProgressListener<Value> listener) {
					listeners.add(listener);
				}

				@Override
				public void removeProgressListener(
						ProgressListener<Value> listener) {
					listeners.remove(listener);
				}

				@Override
				protected void finalize() throws Throwable {
					for (Progress<Value> subprogress : subProgresses) {
						subprogress.removeProgressListener(globalListener);
					}
				}
			};
		}
	}

	private <Value extends Number> Value computeCurrentValue(
			final Collection<? extends Progress<Value>> subProgresses) {
		List<Value> values = new LinkedList<Value>();
		for (Progress<Value> progress : subProgresses) {
			values.add(progress.getCurrentValue());
		}
		Adder<Value> adder = selectAdder(values.get(0));
		return sum(values, adder);
	}

	private <Value extends Number> Value computeMaxValue(
			final Collection<? extends Progress<Value>> subProgresses) {
		List<Value> values = new LinkedList<Value>();
		for (Progress<Value> progress : subProgresses) {
			values.add(progress.getMaxValue());
		}

		if (values.contains(null)) {
			return null;
		} else {
			Adder<Value> adder = selectAdder(values.get(0));
			return sum(values, adder);
		}
	}

	private <Value extends Number> Value sum(List<Value> values,
			Adder<Value> adder) {
		Iterator<Value> iterator = values.iterator();
		Value value = iterator.next();
		while (iterator.hasNext()) {
			value = adder.add(value, iterator.next());
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	private <Value extends Number> Adder<Value> selectAdder(Value value) {
		if (value == null) {
			throw new NullPointerException(
					"Cannot choose the right adder with a null value");
		} else if (value instanceof Integer) {
			return (Adder<Value>) new Adder<Integer>() {

				@Override
				public Integer add(Integer v1, Integer v2) {
					checkNoNullOperand(v1, v2);
					return v1 + v2;
				}
			};
		} else if (value instanceof Long) {
			return (Adder<Value>) new Adder<Long>() {

				@Override
				public Long add(Long v1, Long v2) {
					checkNoNullOperand(v1, v2);
					return v1 + v2;
				}
			};
		} else if (value instanceof Short) {
			return (Adder<Value>) new Adder<Short>() {

				@Override
				public Short add(Short v1, Short v2) {
					checkNoNullOperand(v1, v2);
					return (short) (v1 + v2);
				}
			};
		} else if (value instanceof Float) {
			return (Adder<Value>) new Adder<Float>() {

				@Override
				public Float add(Float v1, Float v2) {
					checkNoNullOperand(v1, v2);
					return v1 + v2;
				}
			};
		} else if (value instanceof Double) {
			return (Adder<Value>) new Adder<Double>() {

				@Override
				public Double add(Double v1, Double v2) {
					checkNoNullOperand(v1, v2);
					return v1 + v2;
				}
			};
		} else if (value instanceof BigInteger) {
			return (Adder<Value>) new Adder<BigInteger>() {

				@Override
				public BigInteger add(BigInteger v1, BigInteger v2) {
					checkNoNullOperand(v1, v2);
					return v1.add(v2);
				}
			};
		} else if (value instanceof BigDecimal) {
			return (Adder<Value>) new Adder<BigDecimal>() {

				@Override
				public BigDecimal add(BigDecimal v1, BigDecimal v2) {
					checkNoNullOperand(v1, v2);
					return v1.add(v2);
				}
			};
		} else if (value instanceof Byte) {
			return (Adder<Value>) new Adder<Byte>() {

				@Override
				public Byte add(Byte v1, Byte v2) {
					checkNoNullOperand(v1, v2);
					return (byte) (v1 + v2);
				}
			};
		} else {
			throw new RuntimeException("Unmanaged type: " + value.getClass());
		}
	}

	private <Value extends Number> void checkNoNullOperand(Value v1, Value v2) {
		if (v1 == null || v2 == null) {
			throw new NullPointerException("We cannot add null values: " + v1
					+ " + " + v2);
		} else {
			// OK
		}
	}

	private static interface Adder<Value extends Number> {
		public Value add(Value v1, Value v2);
	}
}
