(ns serialize
  [:require
   [clojure.java.io                 :as io]
   [com.github.kyleburton.clj-bloom :as bloom]
   [com.github.kyleburton.clj-bloom.io :refer [filter->file file->filter]]])

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

(defn test-full-filter-serialization []
  (let [fname "f1.dat"
        f1    (doto
                  (make-filter)
                populate-with-words)]
    (filter->file f1 fname)
    (let [f2 (file->filter fname (make-hash-fn))]
      (printf "f1.cardinality=%d\n" (-> f1 :bitarray .cardinality))
      (printf "f2.cardinality=%d\n" (-> f1 :bitarray .cardinality))
      (printf "f1 contains 'orange'? %s (expect true)\n" (bloom/include? f1 "orange"))
      (printf "f2 contains 'orange'? %s (expect true)\n" (bloom/include? f2 "orange"))
      (bloom/add! f2 "123456")
      (printf "f1 contains '123456'? %s (expect false)\n" (bloom/include? f1 "123456"))
      (printf "f2 contains '123456'? %s (expect true)\n" (bloom/include? f2 "123456")))))

(defn -main [& _args]
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
