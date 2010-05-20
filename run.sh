set -e
set -x
lein deps
test -f clj-bloom.jar            || lein jar      && lein test
test -f clj-bloom-standalone.jar || lein uberjar

java -cp clj-bloom-standalone.jar clojure.main examples/words.clj
