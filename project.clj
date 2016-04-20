(defproject indent-sniffer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [me.raynes/fs "1.4.6"]
                 [me.raynes/conch "0.8.0"]
                 [org.clojure/tools.cli "0.3.3"]
                 [cheshire "5.5.0"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 ]
  :main indent-sniffer.cli
  :profiles {
             :uberjar {:aot :all}
             :dev {:dependencies [[midje "1.8.3"]]}
             }
  :plugins [[lein-midje "3.2"]]
  )
