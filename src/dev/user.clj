(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [mount.core :as mount]
    [security-demo.components.http-server :refer [http-server]]))

(set-refresh-dirs "src/dev" "src/main")

(defn- refresh [& args] (apply tools-ns/refresh args))
(defn- start [] (mount/start (mount/with-args {:config "config/dev.edn"})))
(defn stop [] (mount/stop))
(defn restart
  "Stop, refresh, and restart the server."
  []
  (stop)
  (refresh :after 'user/start))

