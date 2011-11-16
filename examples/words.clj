(ns words
  (:require
   [ clojure.contrib.duck-streams   :as ds]
   [com.github.kyleburton.clj-bloom :as bf]))

(def words-file "/usr/share/dict/words")

(defn all-words []
  (ds/read-lines words-file))

(defn add-words-to-filter! [filter]
  (dorun
   (doseq [word (all-words)]
     (bf/add! filter (.toLowerCase word)))))

(defn run [hash-fn]
  (let [filter (bf/bloom-filter (* 10 1024 1024) hash-fn)]
    (add-words-to-filter! filter)
    (dorun
     (doseq [w (.split "The quick brown ornithopter hyper-jumped over the lazy trollusk" "\\s+")]
       (if (bf/include? filter (.toLowerCase w))
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

(eprintf "bf/make-hash-fn-hash-code")
(def word-flt-1pct          (make-words-filter 2875518 7 bf/make-hash-fn-hash-code))
(eprintf "n=2875518, k=10, p=0.01: Java's hashCode: fp=%f  cardinality=%d\n"
        (report-fp-rate 100000 word-flt-1pct)
        (.cardinality (:bitarray word-flt-1pct)))

(eprintf "bf/make-hash-fn-crc32")
(def word-flt-crc32-1pct    (make-words-filter 2875518 7 bf/make-hash-fn-crc32))
(eprintf "n=2875518, k=10, p=0.01: CRC32:           fp=%f  cardinality=%d\n"
        (report-fp-rate 100000 word-flt-crc32-1pct)
        (.cardinality (:bitarray word-flt-crc32-1pct)))

(eprintf "bf/make-hash-fn-adler32")
(def word-flt-adler32-1pct  (make-words-filter 2875518 7 bf/make-hash-fn-adler32))
(eprintf "n=2875518, k=10, p=0.01: Adler32:         fp=%f  cardinality=%d\n"
        (report-fp-rate 100000 word-flt-adler32-1pct)
        (.cardinality (:bitarray word-flt-adler32-1pct)))

(eprintf "bf/make-hash-fn-md5")
(def word-flt-md5-1pct      (make-words-filter 2875518 7 bf/make-hash-fn-md5))
(eprintf "n=2875518, k=10, p=0.01: MD5:             fp=%f  cardinality=%d\n"
        (report-fp-rate 100000 word-flt-md5-1pct)
        (.cardinality (:bitarray word-flt-md5-1pct)))

(eprintf "bf/make-hash-fn-sha1")
(def word-flt-sha1-1pct     (make-words-filter 2875518 7 bf/make-hash-fn-sha1))
(eprintf "n=2875518, k=10, p=0.01: SHA1:            fp=%f  cardinality=%d\n"
        (report-fp-rate 100000 word-flt-sha1-1pct)
        (.cardinality (:bitarray word-flt-sha1-1pct)))
