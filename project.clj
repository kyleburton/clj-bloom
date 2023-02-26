(defproject com.github.kyleburton/clj-bloom "1.0.7"
  :description          "Bloom Filter implementation in Clojure, see also: http://github.com/kyleburton/clj-bloom"
  :url                  "http://github.com/kyleburton/clj-bloom"
  :lein-release         {:deploy-via :clojars}
  :license              {:name "Eclipse Public License - v 1.0"
                         :url "http://www.eclipse.org/legal/epl-v10.html"
                         :distribution :repo
                         :comments "Same as Clojure"}
  :deploy-repositories [["releases"  :clojars]
                        ["snapshots" :clojars]]
  :local-repo-classpath true
  :profiles             {:dev {:dependencies [[org.clojure/clojure                    "1.11.1"]
                                              [org.clojure/math.combinatorics         "0.1.6"]
                                              [cider/cider-nrepl                      "0.28.7"]
                                              [org.clojure/data.json                  "2.4.0"]
                                              [org.clojure/tools.logging              "1.2.4"]]
                               :resource-paths ["examples"]}
                         :1.7    {:dependencies [[org.clojure/clojure                 "1.7.0"]
                                                 [org.clojure/math.combinatorics      "0.1.6"]]}
                         :1.8    {:dependencies [[org.clojure/clojure                 "1.8.0"]
                                                 [org.clojure/math.combinatorics      "0.1.6"]]}
                         :1.9    {:dependencies [[org.clojure/clojure                 "1.9.0"]
                                                 [org.clojure/math.combinatorics      "0.1.6"]]}
                         :1.10   {:dependencies [[org.clojure/clojure                 "1.10.3"]
                                                 [org.clojure/math.combinatorics      "0.1.6"]]}
                         :1.11   {:dependencies [[org.clojure/clojure                 "1.11.1"]
                                                 [org.clojure/math.combinatorics      "0.1.6"]]}}
  :aliases              {"all" ["with-profile" "1.7:1.8:1.9:1.10:1.11"]}
  :global-vars          {*warn-on-reflection* true}
  :dependencies         [])
