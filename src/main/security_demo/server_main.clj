(ns security-demo.server-main
  (:require
    [mount.core :refer [start with-args]]
    [security-demo.components.http-server :refer [http-server]])
  (:gen-class))

(defn -main [& args]
  (start (with-args {:config "config/prod.edn"})))
