(ns security-demo.components.middleware
  (:require
    [clojure.pprint :refer [pprint]]
    [fulcro.server :as server]
    [mount.core :refer [defstate]]
    [ring.middleware.defaults :refer [wrap-defaults]]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.util.response :refer [response file-response resource-response]]
    [security-demo.components.config :refer [config]]
    [security-demo.components.server-parser :refer [server-parser]]
    [taoensso.timbre :as timbre]))

(def ^:private not-found-handler
  (fn [_]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "NOPE"}))

(defn wrap-api [handler]
  (fn [request]
    (if (= "/api" (:uri request))
      (server/handle-api-request
        server-parser
        ;; this map is `env`. Put other defstate things in this map and they'll be in the mutations/query env on server.
        {:config config}
        (:transit-params request))
      (handler request))))

(defstate middleware
  :start
  (let [defaults-config (:ring.middleware/defaults-config config)]
    (timbre/info "Configuring middleware-defaults with" (with-out-str (pprint defaults-config)))
    (-> not-found-handler
      wrap-api
      (wrap-defaults defaults-config)
      wrap-gzip)))

