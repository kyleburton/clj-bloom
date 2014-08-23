(ns ^{:doc "IO Helpers for clj-bloom.  Supports serialization and
  de-serialization of the state portion of a Bloom filter."}
  com.github.kyleburton.clj-bloom.io
  (:import
   [java.io
    ObjectInputStream
    ObjectOutputStream
    FileOutputStream
    FileInputStream]))

(defn object-input-stream->filter
  "Deserializes a filter from an ObjectInputStream.  Reads the number
of bits, the number of insertions and the bitset (the state of the
filter)."
  ([^ObjectInputStream ois]
     (let [[num-bits insertions bitarray] [(.readObject ois) (.readObject ois) (.readObject ois)]]
       {:bitarray   bitarray
        :num-bits   num-bits
        :insertions (atom insertions)}))
  ([^ObjectInputStream ois hash-fn]
     (assoc
         (object-input-stream->filter ois)
       :hash-fn    hash-fn)))

(defn filter->object-output-stream
  "Serializes a filter into an ObjectOutputStream.  Writes the number
  of bits, the number of insertions and the bitset (the state of the
  filter).  NB: this function does not serialize the hash-fn."
  [^java.util.Map flt ^ObjectOutputStream oos]
  (.writeObject oos (:num-bits flt))
  (.writeObject oos @(:insertions flt))
  (.writeObject oos (:bitarray flt)))

(defn filter->file
  "Store a filter's state in a file."
  [flt ^String fname]
  (with-open [oos (ObjectOutputStream. (FileOutputStream. fname))]
    (filter->object-output-stream flt oos)))

(defn file->filter
  "Reads, deserializes, a filter stored in a file.  NB: you must supply the filter's hash-fn."
  ([^String fname]
     (with-open [ois (ObjectInputStream. (FileInputStream. fname))]
       (object-input-stream->filter ois)))
  ([^String fname hash-fn]
     (assoc
         (file->filter fname)
       :hash-fn hash-fn)))

