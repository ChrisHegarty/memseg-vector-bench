package org.chegar;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;

import static jdk.incubator.vector.VectorOperators.ADD;

public class VectorUtil {

    static final VectorSpecies<Float> FLOAT_SPECIES = FloatVector.SPECIES_128;
    static final ByteOrder LE = ByteOrder.LITTLE_ENDIAN;

    /** vectorized float dot product body */
    static float dotProduct(float[] a, float[] b) {
        assert a.length == b.length;
        assert a.length % (FLOAT_SPECIES.vectorByteSize() * 4) == 0;
        int i = 0;
        FloatVector acc1 = FloatVector.zero(FLOAT_SPECIES);
        FloatVector acc2 = FloatVector.zero(FLOAT_SPECIES);
        FloatVector acc3 = FloatVector.zero(FLOAT_SPECIES);
        FloatVector acc4 = FloatVector.zero(FLOAT_SPECIES);
        final int limit = a.length;
        for (; i < limit; i += 4 * FLOAT_SPECIES.length()) {
            FloatVector va1 = FloatVector.fromArray(FLOAT_SPECIES, a, i);
            FloatVector vb1 = FloatVector.fromArray(FLOAT_SPECIES, b, i);
            acc1 = va1.fma(vb1, acc1);

            FloatVector va2 = FloatVector.fromArray(FLOAT_SPECIES, a, i + FLOAT_SPECIES.length());
            FloatVector vb2 = FloatVector.fromArray(FLOAT_SPECIES, b, i + FLOAT_SPECIES.length());
            acc2 = va2.fma(vb2, acc2);

            FloatVector va3 = FloatVector.fromArray(FLOAT_SPECIES, a, i + 2 * FLOAT_SPECIES.length());
            FloatVector vb3 = FloatVector.fromArray(FLOAT_SPECIES, b, i + 2 * FLOAT_SPECIES.length());
            acc3 = va3.fma(vb3, acc3);

            FloatVector va4 = FloatVector.fromArray(FLOAT_SPECIES, a, i + 3 * FLOAT_SPECIES.length());
            FloatVector vb4 = FloatVector.fromArray(FLOAT_SPECIES, b, i + 3 * FLOAT_SPECIES.length());
            acc4 = va4.fma(vb4, acc4);
        }
        // reduce
        FloatVector res1 = acc1.add(acc2);
        FloatVector res2 = acc3.add(acc4);
        return res1.add(res2).reduceLanes(ADD);
    }

    static float dotProduct(MemorySegment a, MemorySegment b) {
        assert a.byteSize() == b.byteSize();
        assert a.byteSize() % (FLOAT_SPECIES.vectorByteSize() * 4L) == 0L;

        int i = 0;
        FloatVector acc1 = FloatVector.zero(FLOAT_SPECIES);
        FloatVector acc2 = FloatVector.zero(FLOAT_SPECIES);
        FloatVector acc3 = FloatVector.zero(FLOAT_SPECIES);
        FloatVector acc4 = FloatVector.zero(FLOAT_SPECIES);

        final int limit = (int) a.byteSize();  // using the ms size hurts performance
        //final int limit = 4096; // see how much can be got by just hardcoding the limit

        for (; i < limit; i += FLOAT_SPECIES.vectorByteSize() * 4) {
            FloatVector va1 = FloatVector.fromMemorySegment(FLOAT_SPECIES, a, i, LE);
            FloatVector vb1 = FloatVector.fromMemorySegment(FLOAT_SPECIES, b, i, LE);
            acc1 = va1.fma(vb1, acc1);

            FloatVector va2 = FloatVector.fromMemorySegment(FLOAT_SPECIES, a, i + FLOAT_SPECIES.vectorByteSize(), LE);
            FloatVector vb2 = FloatVector.fromMemorySegment(FLOAT_SPECIES, b, i + FLOAT_SPECIES.vectorByteSize(), LE);
            acc2 = va2.fma(vb2, acc2);

            FloatVector va3 = FloatVector.fromMemorySegment(FLOAT_SPECIES, a, i + 2 * FLOAT_SPECIES.vectorByteSize(), LE);
            FloatVector vb3 = FloatVector.fromMemorySegment(FLOAT_SPECIES, b, i + 2 * FLOAT_SPECIES.vectorByteSize(), LE);
            acc3 = va3.fma(vb3, acc3);

            FloatVector va4 = FloatVector.fromMemorySegment(FLOAT_SPECIES, a, i + 3 * FLOAT_SPECIES.vectorByteSize(), LE);
            FloatVector vb4 = FloatVector.fromMemorySegment(FLOAT_SPECIES, b, i + 3 * FLOAT_SPECIES.vectorByteSize(), LE);
            acc4 = va4.fma(vb4, acc4);
        }
        // reduce
        FloatVector res1 = acc1.add(acc2);
        FloatVector res2 = acc3.add(acc4);
        return res1.add(res2).reduceLanes(ADD);
    }
}
