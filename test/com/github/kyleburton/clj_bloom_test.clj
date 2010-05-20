(ns com.github.kyleburton.clj-bloom-test
  (:require [com.github.kyleburton.clj-bloom :as bf])
  (:use [clojure.test]))

(deftest make-bloom-filter-test
  (testing "creating a bloom filter"
           (is (thrown? Exception (bf/make-bloom-filter)))
           (is (bf/make-bloom-filter 1024)))
  (testing "new bloom filters should be empty"
           (is (.isEmpty (:bitarray (bf/make-bloom-filter 1024))))
           (is (not (nil? (:hash-fns (bf/make-bloom-filter 1024)))))))

(deftest add-test
  (testing "add shoud not be empty"
    (let [filter (bf/make-bloom-filter 1024)]
      (bf/add! filter "foo")
      (is (not (.isEmpty (:bitarray filter)))))))

;; (add-test)

(deftest include?-test
  (testing "after adding, a string should be in the filter"
    (let [filter (bf/make-bloom-filter 1024)]
      (is (not (bf/include? filter "foo")))
      (bf/add! filter "foo")
      (is      (bf/include? filter "foo"))
      (is (not (bf/include? filter "bar"))))))

;; (include?-test)

