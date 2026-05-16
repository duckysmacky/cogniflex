package io.github.duckysmacky.cogniflex.analysis.static_;

public interface Specification<T> {
    boolean isSpecifiedBy(T taget);
}
