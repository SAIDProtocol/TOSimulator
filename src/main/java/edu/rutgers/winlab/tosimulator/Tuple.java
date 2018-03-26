package edu.rutgers.winlab.tosimulator;

/**
 *
 * @author jiachen
 * @param <T1>
 * @param <T2>
 */
public class Tuple<T1, T2> {

    private T1 v1;
    private T2 v2;

    public Tuple(T1 v1, T2 v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public T1 getV1() {
        return v1;
    }

    public void setV1(T1 v1) {
        this.v1 = v1;
    }

    public T2 getV2() {
        return v2;
    }

    public void setV2(T2 v2) {
        this.v2 = v2;
    }

}
