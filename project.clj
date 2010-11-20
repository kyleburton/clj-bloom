(defproject com.github.kyleburton/clj-bloom "1.0.2"
  :description "Bloom Filter implementation in Clojure, see also: http://github.com/kyleburton/clj-bloom"
  :warn-on-reflection true
  :dependencies
  [[org.clojure/clojure "1.2.0"]
   [org.clojure/clojure-contrib "1.2.0"]]
  :dev-dependencies
  [[swank-clojure "1.2.1"]
  [autodoc "0.7.1"]]
  :autodoc {
    :name "clj-bloom"
    :page-title "clj-bloom: API Documentation"
    :description "Bloom Filter"
    :web-home "http://kyleburton.github.com/projects/clj-bloom/"
  })
