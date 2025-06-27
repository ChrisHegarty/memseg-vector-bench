This repository contians a small JMH benchmark which demonstrates
a performance regression when comparing looping over arrays of floats
to that of MemorySegments.

Apple M2, AArch64

```
Benchmark                               Mode  Cnt   Score   Error  Units
MemorySegmentBench.dotProductArray      avgt   20  61.154 ± 0.266  ns/op
MemorySegmentBench.dotProductHeapSeg    avgt   20  98.806 ± 3.143  ns/op
MemorySegmentBench.dotProductNativeSeg  avgt   20  95.282 ± 0.356  ns/op
```

This run replaces MemorySegment::byteSize with just the hardcoded byte
size. Just an experiment to determine how much byteSize() is costing.

```
Benchmark                               Mode  Cnt   Score   Error  Units
MemorySegmentBench.dotProductArray      avgt   20  61.730 ± 0.791  ns/op
MemorySegmentBench.dotProductHeapSeg    avgt   20  67.543 ± 0.215  ns/op
MemorySegmentBench.dotProductNativeSeg  avgt   20  66.818 ± 0.185  ns/op
```

I have not yet tested on Intel/x64.
