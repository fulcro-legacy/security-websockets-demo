(ns security-demo.client
  (:require [fulcro.client :as fc]
            [fulcro.client.network :as net]
            [security-demo.ui.root :as root]
            [fulcro.i18n :as i18n]
            ["intl-messageformat" :as IntlMessageFormat]))

(defonce app (atom nil))

(defn mount []
  (reset! app (fc/mount @app root/Root "app")))

(defn start []
  (mount))

(def secured-request-middleware
  (->
    (net/wrap-csrf-token identity (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!"))
    (net/wrap-fulcro-request)))

(defn ^:export init []
  (reset! app (fc/new-fulcro-client
                :networking {:remote (net/fulcro-http-remote {:url                "/api"
                                                              :request-middleware secured-request-middleware})}))
  (start))
