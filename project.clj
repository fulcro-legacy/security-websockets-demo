(defproject security-demo "0.1.0-SNAPSHOT"
  :description "My Cool Project"
  :license {:name "MIT" :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.7.0"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [thheller/shadow-cljs "2.6.12"]
                 [fulcrologic/fulcro "2.6.8-SNAPSHOT"]
                 [com.taoensso/timbre "4.10.0"]
                 [clj-time "0.15.0"]

                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.2"]
                 [bk/ring-gzip "0.2.1"]
                 [mount "0.1.12"]]

  :uberjar-name "security_demo.jar"

  :source-paths ["src/main"]

  :profiles {:uberjar    {:main           security-demo.server-main
                          :aot            :all
                          :jar-exclusions [#"public/js/test" #"public/js/cards" #"public/cards.html"]
                          :prep-tasks     ["clean" ["clean"]
                                           "compile" ["with-profile" "cljs" "run" "-m" "shadow.cljs.devtools.cli" "release" "main"]]}
             :production {}
             :cljs       {:source-paths ["src/main"]
                          :dependencies [[binaryage/devtools "0.9.10"]
                                         [org.clojure/core.async "0.4.474"]
                                         [fulcrologic/fulcro-inspect "2.2.4"]]}
             :dev        {:source-paths ["src/dev" "src/main"]
                          :jvm-opts     ["-XX:-OmitStackTraceInFastThrow"]

                          :dependencies [[org.clojure/tools.namespace "0.3.0-alpha4"]
                                         [org.clojure/tools.nrepl "0.2.13"]
                                         [com.cemerick/piggieback "0.2.2"]]
                          :repl-options {:init-ns          user
                                         :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}})
