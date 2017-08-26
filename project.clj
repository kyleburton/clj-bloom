(defproject com.github.kyleburton/clj-bloom "1.0.5-SNAPSHOT"
  :description "Bloom Filter implementation in Clojure, see also: http://github.com/kyleburton/clj-bloom"
  :url         "http://github.com/kyleburton/clj-bloom"
  :lein-release         {:deploy-via :clojars}
  :license              {:name "Eclipse Public License - v 1.0"
                         :url "http://www.eclipse.org/legal/epl-v10.html"
                         :distribution :repo
                         :comments "Same as Clojure"}
  ;; :repositories         {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :deploy-repositories [["releases"  {:url "https://clojars.org/repo" :creds :gpg}]
                        ["snapshots" {:url "https://clojars.org/repo" :creds :gpg}]]
  :local-repo-classpath true
  :profiles             {:dev {:dependencies [[org.clojure/clojure                    "1.9.0-alpha3"]
                                              [org.clojure/math.combinatorics         "0.0.4"]
                                              [org.clojure/tools.nrepl                "0.2.12"]
                                              [cider/cider-nrepl                      "0.13.0"]]
                               :resource-paths ["examples"]}}
  :global-vars          {*warn-on-reflection* true}
  :dependencies         [])
