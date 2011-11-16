set -e
set -x
lein deps

lein classpath > .classpath

java -cp $(cat .classpath) clojure.main examples/words.clj
