package fr.epicanard.mapsaver.utils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Either<E, R> {
    final Optional<E> error;
    final Optional<R> result;

    private Either(final Optional<E> error, final Optional<R> result) {
        this.error = error;
        this.result = result;
    }

    public static <E, R> Either<E, R> Left(E error) {
        return new Either<>(Optional.of(error), Optional.empty());
    }

    public static <E, R> Either<E, R> Right(R result) {
        return new Either<>(Optional.empty(), Optional.of(result));
    }

    public boolean isRight() {
        return result.isPresent();
    }

    public boolean isLeft() {
        return error.isPresent();
    }

    public Either<E, R> apply(final Consumer<R> applier) {
        this.result.ifPresent(applier);
        return this;
    }

    public Either<E, R> match(final Consumer<E> left, final Consumer<R> right) {
        if (isLeft()) {
            this.error.ifPresent(left);
        } else {
            this.result.ifPresent(right);
        }
        return this;
    }

//    public <S> Either<E, S> map(Function<R, S> mapper) {
//        if (!this.isRight()) {
//            return (Either<E, S>) this;
//        } else {
//            return new Either<>(this.error, this.result.map(mapper));
//        }
//    }
}
