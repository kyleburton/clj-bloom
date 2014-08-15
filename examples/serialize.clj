(ns serialize
  [:require
   [clojure.java.io                 :as io]
   [com.github.kyleburton.clj-bloom :as bloom]])


;; the 7 is the number of hashes to use, these settings should result
;; in about a 1% error rate
(defn make-filter []
  (bloom/bloom-filter
   2875518 ;; estimated number of of entries
   (bloom/make-permuted-hash-fn
    bloom/make-hash-fn-hash-code
    (map str (range 0 7)))))

(defn populate-with-words [flt]
  (with-open [rdr (io/reader "/usr/share/dict/words")]
    (dorun
     (doseq [word (line-seq rdr)]
       (bloom/add! flt word)))))

(defn bitset->file [bitset fname]
  (with-open [oos (java.io.ObjectOutputStream. (java.io.FileOutputStream. fname))]
    (.writeObject oos bitset)))

(defn file->bitset [fname]
  (with-open [ois (java.io.ObjectInputStream. (java.io.FileInputStream. fname))]
    (.readObject ois)))

(defn main []
  (let [filter1 (make-filter)]
    (time
     (populate-with-words filter1))
    (bitset->file (:bitarray filter1) "bitset.dat")
    (let [filter2 (assoc (make-filter)
                    :bitarray (file->bitset "bitset.dat"))]
      (println (format "is 'orange' in filter1? %s" (bloom/include? filter1 "orange")))
      (println (format "is 'orange' in filter2? %s" (bloom/include? filter2 "orange"))))))

(comment
  (main)
  ;; "Elapsed time: 1489.591 msecs"
  ;; is 'orange' in filter1? true
  ;; is 'orange' in filter2? true



  )