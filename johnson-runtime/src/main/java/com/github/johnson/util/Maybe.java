package com.github.johnson.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represent either a (nullable) value, or the absence of a value.
 * <p>
 * <ul>
 * <li>To create an instance representing a value, use the static method {@code of(T value)}.
 * <li>To create an instance representing the absence of value, use the static method {@code empty()}.
 * </ul>
 * The difference with {@link java.util.Optional} is that the value may be {@code null}.
 * 
 * @param <T>
 *            The type of the value.
 */
public class Maybe<T> {
	private static final Maybe<?> EMPTY = new Maybe<>(null);

	private final T value;

	/**
	 * Create a {@link Maybe} instance representing the specified value.
	 */
	private Maybe(T value) {
		this.value = value;
	}

	/**
	 * Return a {@link Maybe} instance representing the specified value.
	 */
	public static <T> Maybe<T> of(T value) {
		return new Maybe<T>(value);
	}

	/**
	 * If {@code isPresent} is true, return a Maybe instance representing the specified value; otherwise, return the
	 * empty Maybe instance.
	 */
	public static <T> Maybe<T> onlyIf(boolean isPresent, T value) {
		return isPresent ? new Maybe<>(value) : empty();
	}

	/**
	 * If {@code value} is non-null, return a Maybe instance representing the specified value; otherwise, return the
	 * empty Maybe instance.
	 */
	public static <T> Maybe<T> onlyIfNonNull(T value) {
		return value != null ? new Maybe<>(value) : empty();
	}

	public static <T, IterableType extends Iterable<T>> Maybe<IterableType> onlyIfNonEmpty(IterableType value) {
		return value != null && value.iterator().hasNext() ? new Maybe<>(value) : empty();
	}

	public static Maybe<String> onlyIfNonEmpty(String value) {
		return value != null && !value.isEmpty() ? new Maybe<>(value) : empty();
	}

	public static <T> Maybe<T> empty() {
		@SuppressWarnings("unchecked")
		final Maybe<T> res = (Maybe<T>) EMPTY;
		return res;
	}

	public boolean isPresent() {
		return this != EMPTY;
	}

	public T get() {
		return value;
	}

	public T orElse(T other) {
		return isPresent() ? value : other;
	}

	public Maybe<T> filter(Predicate<? super T> predicate) {
		Objects.requireNonNull(predicate);
		if (!isPresent())
			return this;
		else return predicate.test(value) ? this : empty();
	}

	public <U> Maybe<U> map(Function<? super T, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent())
			return empty();
		else {
			return Maybe.of(mapper.apply(value));
		}
	}

	public <U> Maybe<U> flatMap(Function<? super T, Maybe<U>> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent())
			return empty();
		else {
			return mapper.apply(value);
		}
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (isPresent()) consumer.accept(value);
	}

	public T orElseGet(Supplier<? extends T> other) {
		return isPresent() ? value : other.get();
	}

	public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
		if (isPresent()) {
			return value;
		} else {
			throw exceptionSupplier.get();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof Maybe)) {
			return false;
		}

		final Maybe<?> other = (Maybe<?>) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value, isPresent());
	}

	@Override
	public String toString() {
		return isPresent() ? String.format("Maybe[%s]", value) : "Maybe.empty";
	}
}
