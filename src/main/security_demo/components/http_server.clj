(ns security-demo.components.http-server
  (:require
    [mount.core :refer [defstate]]
    [org.httpkit.server :refer [run-server]]
    [security-demo.components.config :refer [config]]
    [security-demo.components.middleware :refer [middleware]]
    [taoensso.timbre :as timbre]))

(def legal-server-options #{:ip :port :thread :queue-size :max-body :max-ws :max-line :proxy-protocol
                            :worker-pool :worker-name-prefix :error-logger :warn-logger :event-logger :event-names})

(defstate http-server
  :start
  (let [server-opts    (select-keys (:http-kit/config config) legal-server-options)
        port           (:port server-opts)
        server-stop-fn (run-server middleware server-opts)]
    (timbre/info (str "Web server (http://localhost:" port ")") "started successfully. Config of http-kit options:" server-opts)
    server-stop-fn)

  ;; httpkit returns a function that you call to stop it, which will be stored in the state holder
  :stop
  (http-server))
