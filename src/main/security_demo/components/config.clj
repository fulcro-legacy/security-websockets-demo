(ns security-demo.components.config
  (:require [fulcro.server :as server]
            [mount.core :refer [defstate]]))

;; You'll need defaults.edn and dev.edn in resources/config with at least {} as content.
(defstate config :start (server/load-config {:config-path "config/dev.edn"}))
