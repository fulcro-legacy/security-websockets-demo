(ns security-demo.ui.root
  (:require
    [fulcro.client.mutations :as m]
    [fulcro.client.data-fetch :as df]
    [fulcro.client.dom :as dom]
    [security-demo.api.mutations :as api]
    [fulcro.client.primitives :as prim :refer [defsc]]))

;; The main UI of your application

(defn meaning-render [component load-markers which-meaning known-meaning]
  (let [load-marker (get load-markers which-meaning)
        meaning     (cond
                      load-marker "(Deep Thought Hums...)"
                      (or (string? known-meaning) (pos? known-meaning)) known-meaning
                      :otherwise "")]
    (dom/div
      (dom/span (str "The meaning of " (name which-meaning) " is " meaning "."))
      (dom/button {:onClick (fn [e]
                              (df/load-field component which-meaning
                                ; put the load marker in the df/marker-table at a key like :life
                                :marker which-meaning))} "Ask Deep Thought."))))

(defsc Meaning [this {:keys [ui/ping-number life universe everything] :as props}]
  {:initial-state {:life "unknown" :universe "unknown" :everything "unknown" :ui/ping-number 0}
   :ident         (fn [] [:meaning/by-id :truth])
   :query         [:ui/ping-number :life :universe :everything [df/marker-table '_]]}
  (let [marker-table (get props df/marker-table)]
    (dom/div
      (dom/div
        (dom/button {:onClick (fn [e]
                                (m/set-integer! this :ui/ping-number :value (inc ping-number))
                                (prim/transact! this `[(api/ping {:x ~ping-number})]))} "Ping the server!"))
      (meaning-render this marker-table :life life)
      (meaning-render this marker-table :universe universe)
      (meaning-render this marker-table :everything everything))))

(def ui-meaning (prim/factory Meaning))

(defsc Root [this {:keys [root/meaning]}]
  {:initial-state {:root/meaning {}}
   :query         [{:root/meaning (prim/get-query Meaning)}]}
  (dom/div
    (ui-meaning meaning)))
