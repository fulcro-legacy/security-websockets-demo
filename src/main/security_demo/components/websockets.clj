(ns security-demo.components.websockets
  (:require [mount.core :refer [defstate]]
            [fulcro.server :as server]
            [fulcro.websockets :as fws]
            [com.stuartsierra.component :as component]))

(defstate server-parser :start (server/fulcro-parser))

(defstate websockets
  :start (let [websockets (fws/make-websockets server-parser {})]
           (component/start websockets))
  :stop (when websockets (component/stop websockets)))
