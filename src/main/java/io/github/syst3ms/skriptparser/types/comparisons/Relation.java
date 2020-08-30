package io.github.syst3ms.skriptparser.types.comparisons;

/**
 * A relation between two objects
 */
public enum Relation {
    /**
     * The two objects are equal
     */
    EQUAL(0),
    /**
     * The two objects are not equal
     */
    NOT_EQUAL(0),
    /**
     * The first object is greater than the other one
     */
    GREATER(1),
    /**
     * The first object is greater or equal to the other one
     */
    GREATER_OR_EQUAL(1),
    /**
     * The first object is smaller than the other one
     */
    SMALLER(-1),
    /**
     * The first object is smaller or equal to the other one
     */
    SMALLER_OR_EQUAL(-1);

    private final int comparison;

    Relation(int comparison) {
        this.comparison = comparison;
    }

    /**
     * Returns EQUAL for true or NOT_EQUAL for false
     *
     * @param b a boolean
     * @return <tt>b ? Relation.EQUAL : Relation.NOT_EQUAL</tt>
     */
    public static Relation get(boolean b) {
        return b ? Relation.EQUAL : Relation.NOT_EQUAL;
    }

    /**
     * Gets a Relation from a difference: If i is 0, EQUAL is returned, if i is greater than 0, GREATER is returned, otherwise SMALLER.
     *
     * @param i an integer
     * @return <tt>i == 0 ? Relation.EQUAL : i > 0 ? Relation.GREATER : Relation.SMALLER</tt>
     */
    public static Relation get(int i) {
        return i == 0 ? Relation.EQUAL : i > 0 ? Relation.GREATER : Relation.SMALLER;
    }

    /**
     * Gets a Relation from a difference: If d is 0, {@link #EQUAL} is returned, if d is greater than 0, {@link #GREATER} is returned, otherwise {@link #SMALLER}.
     * If d is {@link Double#NaN}, then {@link #NOT_EQUAL} is returned
     *
     * @param d a double
     * @return <tt>d == 0 ? Relation.EQUAL : d > 0 ? Relation.GREATER : Relation.SMALLER</tt>, or {@code Relation.NOT_EQUAL} if <tt>Double.isNan(d)</tt>
     */
    public static Relation get(double d) {
        if (Double.isNaN(d))
            return NOT_EQUAL;
        return d == 0 ? Relation.EQUAL : d > 0 ? Relation.GREATER : Relation.SMALLER;
    }

    /**
     * Test whether this relation is fulfilled if another is, i.e. if the parameter relation fulfils <code>X rel Y</code>, then this relation fulfils <code>X rel Y</code> as
     * well.
     *
     * @param other another {@link Relation}
     * @return Whether this relation is part of the given relation, e.g. <code>GREATER_OR_EQUAL.is(EQUAL)</code> returns true.
     */
    public boolean is(Relation other) {
        if (other == this)
            return true;
        switch (this) {
            case EQUAL:
            case SMALLER:
            case GREATER:
                return false;
            case NOT_EQUAL:
                return other == SMALLER || other == GREATER;
            case GREATER_OR_EQUAL:
                return other == GREATER || other == EQUAL;
            case SMALLER_OR_EQUAL:
                return other == SMALLER || other == EQUAL;
        }
        assert false;
        return false;
    }

    /**
     * Returns this relation's string representation, which is similar to "equal to" or "greater than".
     */
    @Override
    public String toString() {
        switch (this) {
            case EQUAL:
                return "equal to";
            case NOT_EQUAL:
                return "not equal to";
            case GREATER:
                return "greater than";
            case GREATER_OR_EQUAL:
                return "greater than or equal to";
            case SMALLER:
                return "smaller than";
            case SMALLER_OR_EQUAL:
                return "smaller than or equal to";
        }
        assert false;
        return "!!!ERROR!!!";
    }

    /**
     * Gets the inverse of this {@link Relation}, i.e if this relation fulfils <code>X rel Y</code>, then the returned relation fulfils <code>!(X rel Y)</code>.
     *
     * @return the opposite of this relation
     */
    public Relation getInverse() {
        switch (this) {
            case EQUAL:
                return NOT_EQUAL;
            case NOT_EQUAL:
                return EQUAL;
            case GREATER:
                return SMALLER_OR_EQUAL;
            case GREATER_OR_EQUAL:
                return SMALLER;
            case SMALLER:
                return GREATER_OR_EQUAL;
            case SMALLER_OR_EQUAL:
                return GREATER;
        }
        assert false;
        return NOT_EQUAL;
    }

    /**
     * Gets the {@link Relation} with switched arguments, i.e. if this relation fulfils <code>X rel Y</code>, then the returned relation fulfills <code>Y rel X</code>.
     *
     * @return the switched relation
     */
    public Relation getSwitched() {
        switch (this) {
            case EQUAL:
                return EQUAL;
            case NOT_EQUAL:
                return NOT_EQUAL;
            case GREATER:
                return SMALLER;
            case GREATER_OR_EQUAL:
                return SMALLER_OR_EQUAL;
            case SMALLER:
                return GREATER;
            case SMALLER_OR_EQUAL:
                return GREATER_OR_EQUAL;
        }
        assert false;
        return NOT_EQUAL;
    }

    public boolean isEqualOrInverse() {
        return this == Relation.EQUAL || this == Relation.NOT_EQUAL;
    }

    public int getComparison() {
        return comparison;
    }
}
