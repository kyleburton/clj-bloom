[![https://clojars.org/com.github.kyleburton/clj-bloom](https://clojars.org/com.github.kyleburton/clj-bloom/latest-version.svg)](https://clojars.org/com.github.kyleburton/clj-bloom)

# clj-bloom


[Bloom Filter](https://en.wikipedia.org/wiki/Bloom_filter) implementation in Clojure.

A bloom filter is a probabilistic data structure for testing set membership.  A deterministic data structure for testing set membership is a `java.util.Set`, some languages would use a hash (or map) data structure with a boolean as the value and the target strings as the key.  As the number of entries in your set grows, so will the memory usage of these data structures.  Bloom filters sacrifice determinism in favor of significantly lower memory usage.  Bloom filters do not suffer from false negatives (indicating that a given string is not in the set when it is in fact in the set).  They do suffer from false positives (indicating that the string is in the set when it is in fact not), though the false positive rate can be estimated fairly accurately, more importantly, the size of the filter and the number of hashes can be chosen to give you a target false positive rate.

The example code in [words.clj](https://github.com/kyleburton/clj-bloom/blob/master/examples/words.clj) demonstrates calculating the size of the filter and number of hashes based on an a priori estimate of set cardinality and a target false positive rate.  It fills the filter from a words data set (`/usr/share/dict/words`, which should exist on most Linux and OS X systems), and measures the false-positive rate by looking for numbers converted to strings (none of which are assumed to be in the filter).

This implementation uses a Java `java.util.BitSet` as the bit array implementation and provides several helpers for different hashing functions.

# Usage

The following example code can be found in the file `examples/words.clj`  It initializes a bloom filter with the dictionary from `/usr/local/dict/words`.   You can run this example by executing `lein run -m words`.

```clojure
(ns words
  (:require
   [clojure.java.io                 :as io]
   [com.github.kyleburton.clj-bloom :as bf])
  (:import
   [java.util BitSet]))

(def ^{:dynamic true} words-file           "/usr/share/dict/words")
(def ^{:dynamic true} num-expected-entries 2875518)

(def all-words (memoize (fn [words-file]
                          (with-open [rdr (io/reader words-file)]
                            (doall (line-seq rdr))))))

(defn add-words-to-filter! [filter]
  (dorun
   (doseq [word (all-words words-file)]
     (bf/add! filter (.toLowerCase ^String word)))))

(defn run [hash-fn]
  (let [filter (bf/bloom-filter (* 10 1024 1024) hash-fn)]
    (add-words-to-filter! filter)
    (dorun
     (doseq [w (.split "The quick brown ornithopter hyper-jumped over the lazy trollusk" "\\s+")]
       (if (bf/include? filter (.toLowerCase ^String w))
         (prn (format "HIT:  '%s' in the filter" w))
         (prn (format "MISS: '%s' not in the filter" w)))))))

(defn make-words-filter [m-expected-entries k-hashes hash-fn]
  (let [flt (bf/bloom-filter
             m-expected-entries
             (bf/make-permuted-hash-fn
              (or hash-fn bf/make-hash-fn-hash-code)
              (map str (range 0 k-hashes))))]
    (add-words-to-filter! flt)
    flt))

(defn words-fp-rate [count flt]
  (loop [times count
         fps   0]
    (cond (= 0 times)
          fps
          (bf/include? flt (str times))
          (recur (dec times)
                 (inc fps))

          :else
          (recur (dec times)
                 fps))))

(defn report-fp-rate [count flt]
  (let [rate (words-fp-rate count flt)]
    (/ rate count 1.0)))

(defn eprintf [& args]
  (.println System/err (apply format args)))

(def word-flt-1pct          (make-words-filter num-expected-entries 7 bf/make-hash-fn-hash-code))
(def word-flt-crc32-1pct    (make-words-filter num-expected-entries 7 bf/make-hash-fn-crc32))
(def word-flt-adler32-1pct  (make-words-filter num-expected-entries 7 bf/make-hash-fn-adler32))
(def word-flt-md5-1pct      (make-words-filter num-expected-entries 7 bf/make-hash-fn-md5))
(def word-flt-sha1-1pct     (make-words-filter num-expected-entries 7 bf/make-hash-fn-sha1))

(defn -main [& args]
  (let [words-file (or (first args)
                            words-file)]
   (binding [words-file (or (first args)
                            words-file)
             num-expected-entries (count (all-words words-file))]
     (eprintf "words-file=%s, num-expected-entries=%d" words-file num-expected-entries)
     (eprintf "bf/make-hash-fn-hash-code")
     (eprintf "n=%s, k=10, p=0.01: Java's hashCode: fp=%f  cardinality=%d\n"
              num-expected-entries
              (report-fp-rate 100000 word-flt-1pct)
              (.cardinality ^BitSet (:bitarray word-flt-1pct)))
     (eprintf "bf/make-hash-fn-crc32")
     (eprintf "n=%s, k=10, p=0.01: CRC32:           fp=%f  cardinality=%d\n"
              num-expected-entries
              (report-fp-rate 100000 word-flt-crc32-1pct)
              (.cardinality ^BitSet (:bitarray word-flt-crc32-1pct)))
     (eprintf "bf/make-hash-fn-adler32")
     (eprintf "n=%s, k=10, p=0.01: Adler32:         fp=%f  cardinality=%d\n"
              num-expected-entries
              (report-fp-rate 100000 word-flt-adler32-1pct)
              (.cardinality ^BitSet (:bitarray word-flt-adler32-1pct)))
     (eprintf "bf/make-hash-fn-md5")
     (eprintf "n=%s, k=10, p=0.01: MD5:             fp=%f  cardinality=%d\n"
              num-expected-entries
              (report-fp-rate 100000 word-flt-md5-1pct)
              (.cardinality ^BitSet (:bitarray word-flt-md5-1pct)))
     (eprintf "bf/make-hash-fn-sha1")
     (eprintf "n=%s, k=10, p=0.01: SHA1:            fp=%f  cardinality=%d\n"
              num-expected-entries
              (report-fp-rate 100000 word-flt-sha1-1pct)
              (.cardinality ^BitSet (:bitarray word-flt-sha1-1pct))))))
```

`/usr/share/dict/words` on my system is 24,86,813 bytes while the recommended size of this filter for a 1% FP rate is 2,875,518 bytes - a nearly ten fold reduction in required memory.  If the entries were larger the memory savings would be larger as well.

## Parameters

The [Wikipedia article on Bloom Filters](https://en.wikipedia.org/wiki/Bloom_filter) discusses methods for determining optimal values for the number of hashes, `k`, the size of the bit array, `m`, given an expected number of entries `n` and a target false positive probability, `p`.

This library provides a functions to help you determine those parameters for your data set.  Following along with the `words.clj` example, the `/usr/share/dict/words` file on my system contains about 234,936 words.  This gives us `n = 234936` entries, if we can tolerate a 1% error rate, the size of the bit array should be:

```clojure
(num-bits-for-entries-and-fp-probability 234936 0.01) => 2251875
```

or about 2.2 million bits.  Given this bit array size, we can now compute an optimal number of hashes:

```clojure
(num-hash-fns-for-entries-and-bits 234936 2251875)  => 6.643855378585771
```

rounding to 7.  You can obtain both of these values in one call with:

```clojure
(optimal-n-and-k 300000 0.01) => [2875518  7]
```

Based on the benchmarks in the `examples/words.clj` file, I do not recommend using Java's `hashCode` function for use with the bloom filter.  It results in a significantly higher false positive rate then the other hashes.  My recommendation is to use the values reported by `optimal-n-and-k` as a starting point and measure your actual false positive rate, keeping in mind the ['4.8 bits per key' rule](https://www.igvita.com/2008/12/27/scalable-datasets-bloom-filters-in-ruby/) as a way to decrease the false-positive rate by an order of magnitude.

There is also a helper function `make-optimal-filter` that takes an estimated number of entries, a desired false-positive probability and an optional hash builder function.

## Hash Functions

The `words.clj` example shows how to create your own hash function.  As part of the definition of a bloom filter, a number of hashes `k` are executed to determine the bits to set.  The interface for this hash function should take a `java.lang.String` and an `int`.  The string being the data to compute the hash for, and the `int` being the size in bits (`n`) of the bit array.  The hash function must return the sequence of bit locations to be set in the filter for the given input string.

## Serialization

The internal state of the Bloom Filter (the number of insertions and the BitSet) can be serialized easily (there are helper functions in the `com.github.kyleburton.clj-bloom.io` name space).  The hash-fn, being a function, can not be easily serialized.  When serializing a filter's data, you must be sure to provide the original hash function when de-serializing the filter.  See [examples/serialize.clj](blob/master/examples/serialize.clj)

# Limitations

## `java.util.BitSet`

The current implementation uses a `java.util.BitSet`, which is limited to `2^32 - 1` bits. This constrains the number of elements and the false-positive probabilities that can be achieved.

Both `java.util.BitSet` and `java.math.BigInteger` have this limitation.  I will look into implementing a version of the filter that uses `byte` arrays under the hood so that this limitation can be lifted.

# Installation

If you're using Leiningen, add the following to your `project.clj` file's `:dependencies`:

```clojure
  [com.github.kyleburton/clj-bloom "1.0.7"]
```

For maven:

```xml
  <dependencies>
    <dependency>
      <groupId>com.github.kyleburton</groupId>
      <artifactId>clj-bloom</artifactId>
      <version>1.0.7</version>
    </dependency>
    ...
  </dependencies>
```

## Development


```console
$ lein with-profile dev run -m clj-bloom.nrepl
$ lein all test

```

## Building

To build using Leiningen:

```console
  clj-bloom$ lein all test
  clj-bloom$ lein jar
  clj-bloom$ lein uberjar
```

Deploy

```console
$ lein deploy clojars
$ lein release
```


# References

* [Bloom Filter on Wikipedia](https://en.wikipedia.org/wiki/Bloom_filter)
* [Bloom Filters: A Powerful Tool](https://www.rubyinside.com/bloom-filters-a-powerful-tool-599.html)
* [Counting Bloom Filter implemented in Ruby](https://github.com/igrigorik/bloomfilter)
* [Scalable Data-sets: Bloom Filters in Ruby](https://www.igvita.com/2008/12/27/scalable-datasets-bloom-filters-in-ruby/)
* [Bloom Filters - the math](https://pages.cs.wisc.edu/~cao/papers/summary-cache/node8.html)

# License

[Same as Clojure](https://clojure.org/license)
