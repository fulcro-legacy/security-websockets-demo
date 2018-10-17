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
    [security-demo.components.websockets :refer [websockets]]
    [taoensso.timbre :as timbre]))

(def ^:private not-found-handler
  (fn [_]
    {:status  404
     :headers {"Content-Type" "text/plain"}
     :body    "NOPE"}))

(defn- is-wsrequest? [{:keys [uri]}] (= "/chsk" uri))

(defn enforce-csrf!
  "Checks the CSRF token. If it is ok, runs the `ok-response-handler`; otherwise returns a 403 response
  and logs the CSRF violation."
  [{:keys [anti-forgery-token params]} ok-response-handler]
  (let [{:keys [csrf-token]} params
        token-matches? (and (seq csrf-token) (= csrf-token anti-forgery-token))]
    (timbre/debug "Setting up websocket request. Incoming security token is: " csrf-token)
    (timbre/debug "Expected CSRF token is " anti-forgery-token)
    (if token-matches?
      (ok-response-handler)
      (do
        (timbre/error "CSRF FAILURE. The token received does not match the expected value.")
        (-> (response/response "Cross site requests are not supported.")
          (response/status 403))))))

(defn wrap-websockets
  "Add websocket middleware.  This middleware does a CSRF check on the GET (normal Ring only checks POSTS)
  to ensure we don't start a Sente handshake unless the client has already proven it knows the CSRF token."
  [base-request-handler]
  (fn [{:keys [request-method] :as req}]
    (if (is-wsrequest? req)
      (let [{:keys [ring-ajax-post ring-ajax-get-or-ws-handshake]} websockets]
        ;; The enforcement is really on GET, which ring's middleware won't block,
        ;; but which exposes the token in the handshake
        (enforce-csrf! req (fn []
                             (case request-method
                               :get (ring-ajax-get-or-ws-handshake req)
                               :post (ring-ajax-post req)))))
      (base-request-handler req))))

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
      wrap-websockets
      server/wrap-transit-params
      server/wrap-transit-response
      (server/wrap-protect-origins {:allow-when-origin-missing? false
                                    :legal-origins              legal-origins})
      (wrap-uris {"/"           generate-index
                  "/index.html" generate-index})
      (wrap-defaults defaults-config)
      wrap-gzip)))

