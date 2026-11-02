# LeetCode Hard, one a day

One LeetCode hard per folder: every solution from brute force to optimal in Java, a harness that proves they agree, and a README that walks through the solution visually, chapter by chapter, the same way the video does.

| # | Problem | Pattern | Best | Folder |
|---|---|---|---|---|
| 4 | Median of Two Sorted Arrays | binary search on a partition | O(log min(m, n)) | [0004-median-of-two-sorted-arrays](0004-median-of-two-sorted-arrays/) |

## Each folder

```
NNNN-problem-name/
├── README.md        visual breakdown: brute force → optimal, dry runs, proof, bugs, follow-ups, modern Java
├── run.sh           compile and run the harness
├── Harness.java     examples, edge cases, a differential test against a reference, timing
├── solutions/       one paste-ready LeetCode `class Solution` per approach
└── homework/        the homework answer
```

## Run

Requires JDK 17 or newer.

```bash
cd 0004-median-of-two-sorted-arrays
./run.sh --no-timing   # correctness only, a few seconds
./run.sh               # plus the timing table
```

Diagrams in each walkthrough are made with [Archify](https://github.com/tt-a1i/archify): the README shows light and dark SVGs, and `diagrams/interactive/` holds the explorable HTML versions.
