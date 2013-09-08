(ns com.github.kyleburton.clj-bloom-test
  (:require [com.github.kyleburton.clj-bloom :as bf]
            [clojure.math.combinatorics      :as cmb])
  (:use [clojure.test]))

(deftest make-hash-fn-hash-code-test
  (testing "test the hash-fn helper"
    (is (thrown? Exception (bf/make-hash-fn-hash-code)))
    (is (= (.hashCode "foo1")
           ((bf/make-hash-fn-hash-code "1") "foo" 0xFFFFFFFF)))))

(deftest bloom-filter-test
  (testing "creating a bloom filter"
    (is (thrown? Exception (bf/bloom-filter)))
    (is (bf/bloom-filter 1024 (bf/make-crc32 5))))
  (testing "new bloom filters should be empty"
    (is (.isEmpty ^java.util.BitSet (:bitarray (bf/bloom-filter 1024 (bf/make-crc32 5)))))))

(deftest add-test
  (testing "add shoud not be empty"
    (let [filter (bf/bloom-filter 1024 (bf/make-crc32 5))]
      (bf/add! filter "foo")
      (is (not (.isEmpty ^java.util.BitSet (:bitarray filter))))
      (is (= 1 (bf/insertions filter))))))

(deftest include?-test
  (testing "after adding, a string should be in the filter"
    (let [filter (bf/bloom-filter 1024 (bf/make-crc32 5))]
      (is (not (bf/include? filter "foo")))
      (bf/add! filter "foo")
      (is      (bf/include? filter "foo"))
      (is (not (bf/include? filter "bar")))
      (is (= 1 (bf/insertions filter))))))

(deftest hash-fns-should-differ-test
  (testing "The core hash functions should produce different reuslts"
    (dorun
     (doseq [pair (cmb/combinations
                   [(sort ((bf/make-permuted-hash-fn bf/make-hash-fn-hash-code ["1" "2" "3" "4" "5"]) "foo" 100))
                    (sort ((bf/make-permuted-hash-fn bf/make-hash-fn-crc32     ["1" "2" "3" "4" "5"]) "foo" 100))
                    (sort ((bf/make-permuted-hash-fn bf/make-hash-fn-adler32   ["1" "2" "3" "4" "5"]) "foo" 100))
                    (sort ((bf/make-permuted-hash-fn bf/make-hash-fn-md5       ["1" "2" "3" "4" "5"]) "foo" 100))
                    (sort ((bf/make-permuted-hash-fn bf/make-hash-fn-sha1      ["1" "2" "3" "4" "5"]) "foo" 100))]
                   2)]
       (is (not (= (first pair) (second pair))))))))

(deftest test-optimal-n-and-k
  ;; This tests a bug where optimal-n-and-k used ints instead of longs...
  (is (bf/optimal-n-and-k 400000000 0.01)))

;; (deftest test-make-optimal-filter
;;   ;; this tests a bug that existed in <=1.0.1 with large filters...
;;   (is (bf/make-optimal-filter 400000000 0.01)))

