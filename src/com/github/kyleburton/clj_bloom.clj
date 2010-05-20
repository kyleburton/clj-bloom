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

(def *default-hash-fns*
     [(make-hash-fn-hash-code "1")
      (make-hash-fn-hash-code "2")
      (make-hash-fn-hash-code "3")
      (make-hash-fn-hash-code "4")
      (make-hash-fn-hash-code "5")])

(defstruct bloom-filter :hash-fns :num-bits :bitarray)

(defn make-bloom-filter
  ([num-bits]          (make-bloom-filter num-bits *default-hash-fns*))
  ([num-bits hash-fns] (struct bloom-filter hash-fns num-bits (java.util.BitSet. num-bits))))

(defn add! [filter #^String string]
  (dorun
   (doseq [hfn (:hash-fns filter)]
     (.set (:bitarray filter)
           (hfn string (:num-bits filter))))))


(defn include? [filter #^String string]
  (loop [[hfn & hash-fns] (:hash-fns filter)]
    (cond
      (not hfn)
      true

      (.get (:bitarray filter)
            (hfn string (:num-bits filter)))

      (recur hash-fns)

      :else
      false)))




