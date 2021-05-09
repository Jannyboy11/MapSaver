package fr.epicanard.mapsaver.utils;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class Either<L, R> {
    final Optional<L> left;
    final Optional<R> right;

    private Either(final Optional<L> left, final Optional<R> right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> Left(L left) {
        return new Either<>(Optional.of(left), Optional.empty());
    }

    public static <L, R> Either<L, R> Right(R right) {
        return new Either<>(Optional.empty(), Optional.of(right));
    }

    public boolean isRight() {
        return right.isPresent();
    }

    public boolean isLeft() {
        return left.isPresent();
    }

    public Either<L, R> apply(final Consumer<R> applier) {
        this.right.ifPresent(applier);
        return this;
    }

    public Either<L, R> match(final Consumer<L> left, final Consumer<R> right) {
        if (isLeft()) {
            this.left.ifPresent(left);
        } else {
            this.right.ifPresent(right);
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
