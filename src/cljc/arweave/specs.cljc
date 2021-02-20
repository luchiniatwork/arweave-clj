(ns arweave.specs
  (:require [malli.core :as m]
            [clojure.string :as s]))

(def AnomalyCategory
  [:enum
   :arweave.anomaly/tx-pending
   :arweave.anomaly/tx-not-found
   :arweave.anomaly/tx-failed
   :arweave.anomaly/tx-invalid
   :arweave.anomaly/conn-invalid
   :arweave.anomaly/conn-error])

(def Anomaly
  [:map
   [:anomaly/category AnomalyCategory]
   [:anomaly/message string?]
   [:anomaly/provenance {:optional true} map?]])

(def Connection
  [:map
   [:host string?]
   [:protocol [:enum "http" "https"]]
   [:port integer?]
   [:timeout pos-int?]])

(def NodeInfo
  [:or
   [:map
    [:queue-length integer?]
    [:peers integer?]
    [:release integer?]
    [:current string?]
    [:blocks number?]
    [:network string?]
    [:version integer?]
    [:height integer?]
    [:node-state-latency integer?]]
   Anomaly])

(def NodePeers
  [:vector {:min 1}
   [:and
    string?
    [:fn (fn [i] (let [[host port] (s/split i #":")
                       parts (s/split host #"\.")]
                   (and (>= (Integer/parseInt port) 0)
                        (= 4 (count parts))
                        (every? #(let [n (Integer/parseInt %)]
                                   (and (>= n 0) (<= n 255))) parts))))]]])

(def TxStatus
  [:or
   [:map
    [:status [:fn #(= 200 %)]]
    [:confirmed
     [:map
      [:block-height integer?
       :block-indep-hash string?
       :number-of-confirmations integer?]]]]
   [:map
    [:status [:fn #(not= 200 %)]]
    [:confirmed nil?]]])

(def Tag
  [:map
   [:name string?]
   [:value string?]])

(def Transaction
  [:or
   [:map
    [:format [:enum 1 2]]
    [:id string?]
    [:last-tx string?]
    [:owner string?]
    [:tags [:vector Tag]]
    [:target string?]
    [:quantity string?]
    [:data bytes?]
    [:reward string?]
    [:signature string?]
    [:data-size string?]
    [:data-root string?]]
   Anomaly])
