set -e
set -x
rm -rf lib
lein deps

lein classpath > .classpath

java -cp $(cat .classpath) clojure.main examples/words.clj
