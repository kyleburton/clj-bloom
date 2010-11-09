(ns com.github.kyleburton.clj-bloom)

(defn make-hash-fn-hash-code [#^String x]
  (fn [#^String s bytes]
    (mod (.hashCode (str s x))
         bytes)))

(defn make-hash-fn-crc32 [#^String x]
  (let [crc (java.util.zip.CRC32.)]
    (fn [#^String s bits]
      (.reset crc)
      (.update crc (.getBytes (str s x)))
      (mod (.getValue crc)
           bits))))

(defn make-hash-fn-adler32 [#^String x]
  (let [crc (java.util.zip.Adler32.)]
    (fn [#^String s bits]
      (.reset crc)
      (.update crc (.getBytes (str s x)))
      (mod (.getValue crc)
           bits))))

(defn make-hash-fn-md5 [#^String x]
  (let [md5 (java.security.MessageDigest/getInstance "MD5")]
    (fn [#^String s bits]
      (.reset md5)
      (.update md5 (.getBytes (str s x)))
      (.longValue
       (.mod (java.math.BigInteger. 1 (.digest md5))
             (java.math.BigInteger/valueOf bits))))))

(defn make-hash-fn-sha1 [#^String x]
  (let [sha1 (java.security.MessageDigest/getInstance "SHA1")]
    (fn [#^String s bits]
      (.reset sha1)
      (.update sha1 (.getBytes (str s x)))
      (.longValue
       (.mod (java.math.BigInteger. 1 (.digest sha1))
             (java.math.BigInteger/valueOf bits))))))

(defn make-permuted-hash-fn [base-fn values]
  (let [hash-fns (map base-fn values)]
    (fn [#^String s bits]
      (map #(%1 s bits) hash-fns))))

(defn bloom-filter [num-bits hash-fn]
  {:hash-fn hash-fn
   :num-bits num-bits
   :bitarray (java.util.BitSet. num-bits)
   :insertions (atom 0)})

(defn add! [filter #^String string]
  (reset! (:insertions filter)
          (inc @(:insertions filter)))
  (dorun
   (doseq [bit ((:hash-fn filter) string (:num-bits filter))]
     (.set (:bitarray filter)
           bit))))

(defn include? [filter #^String string]
  (loop [[bit & bits] ((:hash-fn filter) string (:num-bits filter))]
    (cond
      (not bit)                      true
      (.get (:bitarray filter) bit)  (recur bits)
      :else                          false)))

(defn insertions [filter]
  @(:insertions filter))

(defn num-bits-for-entries-and-fp-probability [n-entries fp-prob]
  (* -1
     (/ (* n-entries (Math/log fp-prob))
        (Math/pow (Math/log 2) 2))))

(defn num-hash-fns-for-entries-and-bits [n-entries m-bits]
  (* (/ m-bits n-entries)
     (Math/log 2)))

;; to get a 1% error rate for 500k entries:
;;   (num-bits-for-entries-and-fp-probability 500000 0.01) => 4792529.188683719
;; or about 5MM bits (* 5 1000 1000)
;; 0.1% error rate for 234936 entries
;;   (num-bits-for-entries-and-fp-probability 234936 0.001) => 3377812.912417795
;; or about 3.4MM bits
;;
;; (num-hash-fns-for-entries-and-bits 234936 3377812.912417795) => 9.965784284662087

;; (num-bits-for-entries-and-fp-probability 234936 0.01)
;; (num-hash-fns-for-entries-and-bits 234936 2251875)  6.643855378585771
(defn optimal-n-and-k [entries prob]
  (let [n (num-bits-for-entries-and-fp-probability entries prob)
        k (num-hash-fns-for-entries-and-bits entries n)]
    [(long (Math/ceil n)) (long (Math/ceil k))]))

;; (optimal-n-and-k 10000  0.01)   [  95851  7]
;; (optimal-n-and-k 10000  0.001)  [ 143776 10]
;; (optimal-n-and-k 300000 0.01)   [2875518  7]
;; (optimal-n-and-k 300000 0.001)  [4313277 10]

;; (* 9.6 300000) 2880000.0

(defn make-optimal-filter [entries prob & [hash-fn]]
  (let [[m k] (optimal-n-and-k entries prob)]
    (bloom-filter
     m
     (make-permuted-hash-fn
      (or hash-fn make-hash-fn-crc32)
      (map str (range 0 k))))))

(defn make-crc32   [k] (make-permuted-hash-fn make-hash-fn-crc32 (map str (range 0 k))))
(defn make-adler32 [k] (make-permuted-hash-fn make-hash-fn-adler32 (map str (range 0 k))))
(defn make-md5     [k] (make-permuted-hash-fn make-hash-fn-md5 (map str (range 0 k))))
(defn make-sha1    [k] (make-permuted-hash-fn make-hash-fn-sha1 (map str (range 0 k))))
