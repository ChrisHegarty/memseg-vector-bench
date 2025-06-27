package org.chegar;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.lang.foreign.ValueLayout.JAVA_FLOAT_UNALIGNED;

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 10, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@State(Scope.Thread)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(3)
public class MemorySegmentBench {

    // @Param({"1024"})
    int size = 1024;

    float[] floatsA;
    float[] floatsB;
    MemorySegment heapSegA, heapSegB, nativeSegA, nativeSegB;
    Arena arena;

    @Setup
    public void setup() throws Exception {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // random float arrays for float methods
        floatsA = new float[size];
        floatsB = new float[size];
        for (int i = 0; i < size; ++i) {
            floatsA[i] = random.nextFloat();
            floatsB[i] = random.nextFloat();
        }
        heapSegA = MemorySegment.ofArray(floatsA);
        heapSegB = MemorySegment.ofArray(floatsB);

        arena = Arena.ofConfined();
        nativeSegA = arena.allocate((long) floatsA.length * Float.BYTES);
        MemorySegment.copy(MemorySegment.ofArray(floatsA), JAVA_FLOAT_UNALIGNED, 0L, nativeSegA, JAVA_FLOAT_UNALIGNED, 0L, floatsA.length);
        nativeSegB = arena.allocate((long) floatsB.length * Float.BYTES);
        MemorySegment.copy(MemorySegment.ofArray(floatsB), JAVA_FLOAT_UNALIGNED, 0L, nativeSegB, JAVA_FLOAT_UNALIGNED, 0L, floatsB.length);
    }

    @Benchmark
    @Fork(value = 2, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
    public float dotProductArray() {
        return VectorUtil.dotProduct(floatsA, floatsB);
    }

    @Benchmark
    @Fork(value = 2, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
    public float dotProductHeapSeg(){
        return VectorUtil.dotProduct(heapSegA, heapSegB);
    }

    @Benchmark
    @Fork(value = 2, jvmArgsPrepend = {"--add-modules=jdk.incubator.vector"})
    public float dotProductNativeSeg() {
        return VectorUtil.dotProduct(nativeSegA, nativeSegB);
    }

    @TearDown
    public void teardown() {
        arena.close();
    }
}