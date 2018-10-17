(ns security-demo.components.middleware
  (:require
    [clojure.pprint :refer [pprint]]
    [fulcro.server :as server]
    [hiccup.page :as h.page]
    [mount.core :refer [defstate]]
    [ring.middleware.defaults :refer [wrap-defaults]]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.middleware.cors :refer [wrap-cors]]
    [ring.util.response :as response]
    [ring.util.response :refer [response file-response resource-response]]
    [security-demo.api.mutations]                           ;; ensure reads/mutations are loaded
    [security-demo.api.read]
    [security-demo.components.config :refer [config]]
    [security-demo.components.server-parser :refer [server-parser]]
    [taoensso.timbre :as timbre]
    [clojure.string :as str]))

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

(defn generate-index [{:keys [anti-forgery-token]}]
  (timbre/info "Embedding CSRF token in index page")
  (-> (h.page/html5 {}
        [:head {:lang "en"}
         [:meta {:charset "UTF-8"}]
         [:script (str "var fulcro_network_csrf_token = '" anti-forgery-token "';")]]
        [:body
         [:div#app]
         [:script {:src "/js/main/app.js"}]
         [:script "security_demo.client.init()"]])
    response/response
    (response/content-type "text/html")))

(defn wrap-uris
  "Wrap the given request URIs to a handler function."
  [handler uri-map]
  (fn [{:keys [uri] :as req}]
    (if-let [generator (get uri-map uri)]
      (generator req)
      (handler req))))

(defstate middleware
  :start
  (let [defaults-config (:ring.middleware/defaults-config config)
        legal-origins   (get config :legal-origins #{"localhost"})]
    (timbre/debug "Configuring middleware-defaults with" (with-out-str (pprint defaults-config)))
    (timbre/info "Restricting origins to " legal-origins)
    (when-not (get-in defaults-config [:security :ssl-redirect])
      (timbre/warn "SSL IS NOT ENFORCED: YOU ARE RUNNING IN AN INSECURE MODE (only ok for development)"))
    (-> not-found-handler
      wrap-api
      server/wrap-transit-params
      server/wrap-transit-response
      (server/wrap-protect-origins {:allow-when-origin-missing? false
                                    :legal-origins              legal-origins})
      (wrap-uris {"/"           generate-index
                  "/index.html" generate-index})
      (wrap-defaults defaults-config)
      wrap-gzip)))

