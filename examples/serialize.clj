(ns serialize
  [:require
   [clojure.java.io                 :as io]
   [com.github.kyleburton.clj-bloom :as bloom]])

(defonce num-hashes 7)
(defonce num-expected-entries 2875518)

;; the 7 is the number of hashes to use, these settings should result
;; in about a 1% error rate
(defn make-hash-fn []
  (bloom/make-permuted-hash-fn
   bloom/make-hash-fn-hash-code
   (map str (range 0 num-hashes))))

(defn make-filter []
  (bloom/bloom-filter
   num-expected-entries
   (make-hash-fn)))

(defn populate-with-words [flt]
  (with-open [rdr (io/reader "/usr/share/dict/words")]
    (dorun
     (doseq [word (line-seq rdr)]
       (bloom/add! flt word)))))

(defn filter->object-output-stream [^java.util.Map flt ^java.io.ObjectOutputStream oos]
  (.writeObject oos (:num-bits flt))
  (.writeObject oos @(:insertions flt))
  (.writeObject oos (:bitarray flt)))

(defn object-input-stream->filter [^java.io.ObjectInputStream ois hash-fn]
  (let [[num-bits insertions bitarray] [(.readObject ois) (.readObject ois) (.readObject ois)]]
    {:bitarray   bitarray
     :hash-fn    hash-fn
     :num-bits   num-bits
     :insertions (atom insertions)}))

;; An issue with serializing filters is the :hash-fn, it would need to
;; be re-constructed on de-serialization, passing the configuration
;; that created the hash functions would be a good way of doing that
;; but is not a core concern of the clj-bloom library and probably
;; shouldn't be(?).  A reasonable approach is to not serialize the
;; function, but allow it to be re-provided on deserialization.
(defn filter->file [flt ^String fname]
  (with-open [oos (java.io.ObjectOutputStream. (java.io.FileOutputStream. fname))]
    (filter->object-output-stream flt oos)))

;; NB: you must provide an equivalent hash-fn on deserialization
(defn file->filter [^String fname hash-fn]
  (with-open [ois (java.io.ObjectInputStream. (java.io.FileInputStream. fname))]
    (object-input-stream->filter ois hash-fn)))

(defn test-full-filter-serialization []
  (let [fname "f1.dat"
        f1    (doto
                  (make-filter)
                populate-with-words)]
    (filter->file f1 fname)
    (let [f2 (file->filter fname (make-hash-fn))]
      (printf "f1.cardinality=%d\n" (-> f1 :bitarray .cardinality))
      (printf "f2.cardinality=%d\n" (-> f1 :bitarray .cardinality))
      (printf "f1 contains 'orange'? %s\n" (bloom/include? f1 "orange"))
      (printf "f2 contains 'orange'? %s\n" (bloom/include? f2 "orange")))))

(defn -main [& args]
  (test-full-filter-serialization))

(comment
  (time
   (test-full-filter-serialization))
  ;; f1.cardinality=616322
  ;; f2.cardinality=616322
  ;; f1 contains 'orange'? true
  ;; f2 contains 'orange'? true
  ;; "Elapsed time: 791.19503 msecs"

  )