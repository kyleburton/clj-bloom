set -e 
set -x
lein deps
lein jar
lein pom
#scp pom.xml clj-bloom.jar clojars@clojars.org:
