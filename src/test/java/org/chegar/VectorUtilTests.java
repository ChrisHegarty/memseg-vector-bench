package org.chegar;

import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VectorUtilTests {

    final double delta = 1e-3;

    @RepeatedTest(1000)
    public void testFloat32DotProduct() throws Exception {
        var bench = new MemorySegmentBench();
        bench.setup();
        try {
            float expected = dotProductFloat32Scalar(bench.floatsA, bench.floatsB);
            assertEquals(expected, bench.dotProductArray(), delta);
            assertEquals(expected, bench.dotProductHeapSeg(), delta);
            assertEquals(expected, bench.dotProductNativeSeg(), delta);
        } finally {
            bench.teardown();
        }
    }

    /** Computes the dot product of the given vectors a and b. */
    static float dotProductFloat32Scalar(float[] a, float[] b) {
        float res = 0;
        for (int i = 0; i < a.length; i++) {
            res += a[i] * b[i];
        }
        return res;
    }
}
