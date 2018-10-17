(ns security-demo.components.server-parser
  (:require [fulcro.server :as server]
            [mount.core :refer [defstate]]))

(defstate server-parser :start (server/fulcro-parser))
