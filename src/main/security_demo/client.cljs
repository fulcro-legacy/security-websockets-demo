(ns security-demo.client
  (:require [fulcro.client :as fc]
            [fulcro.websockets :as fws]
            [security-demo.ui.root :as root]))

(defonce app (atom nil))

(defn mount []
  (reset! app (fc/mount @app root/Root "app")))

(defn start []
  (mount))

(defn push-handler [msg] (js/console.log "Push received" msg))

(defn ^:export init []
  (reset! app
    (fc/new-fulcro-client
      :networking {:remote
                   (fws/make-websocket-networking
                     {:websockets-uri "/chsk"
                      :push-handler   push-handler
                      ;; we use these instead of Sente's ajax-options (which would be nice for setting headers)
                      ;; because sente doesn't use those for the initial socket ping.
                      ;; Not ideal, since the CSRF token might end up stored in browser caches, proxy servers, etc.
                      :req-params     {:csrf-token (or js/fulcro_network_csrf_token "TOKEN-NOT-IN-HTML!")}
                      :auto-retry?    true})}))
  (start))
