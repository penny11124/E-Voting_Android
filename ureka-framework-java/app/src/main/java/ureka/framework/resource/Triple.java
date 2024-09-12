package ureka.framework.resource;

public final class Triple {
    private final Object first;
    private final Object second;
    private final Object third;

    public Triple(Object first, Object second, Object third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Object getTripleFirst() {
            return this.first;
        }

    public Object getTripleSecond() {
            return this.second;
        }
    public Object getTripleThird() {
            return this.third;
        }

    @Override
    public String toString() {
        return "Triple{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
