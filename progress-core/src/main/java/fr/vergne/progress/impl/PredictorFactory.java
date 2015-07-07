package fr.vergne.progress.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;

import fr.vergne.progress.Predictor;
import fr.vergne.progress.Progress;
import fr.vergne.progress.Progress.ProgressListener;
import fr.vergne.progress.impl.ProgressUtil.ValueTranslator;

/**
 * This {@link PredictorFactory} provides usual {@link Predictor}
 * implementations.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class PredictorFactory {

	/**
	 * While it is common to apply predictions on the current value of a
	 * {@link Progress}, also the max value can change. Thus, methods in
	 * {@link PredictorFactory} need to know which value they have to predict.
	 * The {@link PredictedValue} that they take as an argument allows to
	 * provide such information.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static enum PredictedValue {
		/**
		 * Predict the current value of the {@link Progress}.
		 */
		CURRENT_VALUE,
		/**
		 * Predict the max value of the {@link Progress}.
		 */
		MAX_VALUE
	};

	/**
	 * This method is a kind of fake {@link Predictor}: it always return the
	 * same value. This is particularly suited to contexts where we know that a
	 * value is not supposed to change (usually the max value of a
	 * {@link Progress}) but we still need to use a {@link Predictor} on it.
	 * 
	 * @param value
	 *            the value the {@link Predictor} should return
	 * @return the {@link Predictor} instance
	 */
	public <Value extends Number> Predictor<Value> createConstantPredictor(
			final Value value) {
		return new Predictor<Value>() {

			@Override
			public Value predictValueAt(long timestamp) {
				return value;
			}
		};
	}

	/**
	 * This method builds a simple {@link Predictor} which predicts values by
	 * assuming that they follow a linear evolution <i>f(x)=ax+b</i>. It stores
	 * the last values generated and produce its estimation based on them. To be
	 * smooth, it stores at least 100 values (for long runs) and at least 10s
	 * worth of values (for frequent updates).
	 * 
	 * @param progress
	 *            the {@link Progress} to listen
	 * @param target
	 *            the {@link PredictedValue}
	 * @return the {@link Predictor} which will predict the values
	 */
	// TODO prefer to use a linear least square method:
	// https://en.wikipedia.org/wiki/Linear_least_squares_%28mathematics%29#Example
	public <Value extends Number> Predictor<Value> createLinearPredictor(
			Progress<Value> progress, PredictedValue target) {
		final LinearData<Value> data = new LinearData<Value>();
		listenProgress(progress, target, new ValueListener<Value>() {

			@Override
			public void valueReceived(Value value) {
				data.times.addLast(System.currentTimeMillis());
				data.values.addLast(value);
				while (data.times.size() > 100
						&& data.times.getLast() - data.times.getFirst() > 10000) {
					data.values.removeFirst();
					data.times.removeFirst();
				}

				if (data.translator == null) {
					data.translator = ProgressUtil.createValueTranslator(value);
				} else {
					// use the same
				}
			}
		});
		return new Predictor<Value>() {

			@Override
			public Value predictValueAt(long timestamp) {
				if (data.times.isEmpty()) {
					throw new RuntimeException(
							"Impossible to predict anything without any generated value.");
				} else if (data.times.size() == 1) {
					return data.values.getFirst();
				} else {
					Long start = data.times.getFirst();
					Long stop = data.times.getLast();
					BigDecimal valueStart = data.translator
							.toDecimal(data.values.getFirst());
					BigDecimal valueStop = data.translator
							.toDecimal(data.values.getLast());

					BigDecimal dvFrom = valueStop.subtract(valueStart);
					BigDecimal dtFrom = new BigDecimal(stop - start);
					BigDecimal dtTo = new BigDecimal(timestamp - start);
					BigDecimal dvTo = dvFrom.multiply(dtTo).divide(dtFrom, 20,
							RoundingMode.HALF_UP);
					BigDecimal result = valueStart.add(dvTo);

					return data.translator.toValue(result);
				}
			}
		};
	}

	private static class LinearData<Value extends Number> {
		public final LinkedList<Value> values = new LinkedList<Value>();
		public final LinkedList<Long> times = new LinkedList<Long>();
		public ValueTranslator<Value> translator = null;
	}

	private static interface ValueListener<Value extends Number> {
		public void valueReceived(Value value);
	}

	private <Value extends Number> void listenProgress(
			Progress<Value> progress, PredictedValue target,
			final ValueListener<Value> listener) {
		if (target == PredictedValue.CURRENT_VALUE) {
			progress.addProgressListener(new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					listener.valueReceived(value);
				}

				@Override
				public void maxUpdate(Value maxValue) {
					// nothing to do
				}
			});
		} else if (target == PredictedValue.MAX_VALUE) {
			progress.addProgressListener(new ProgressListener<Value>() {

				@Override
				public void currentUpdate(Value value) {
					// nothing to do
				}

				@Override
				public void maxUpdate(Value maxValue) {
					listener.valueReceived(maxValue);
				}
			});
		} else {
			throw new RuntimeException("Unmanaged target: " + target);
		}
	}

}
