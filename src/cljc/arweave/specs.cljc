(ns arweave.specs
  (:require [malli.core :as m]
            [clojure.string :as s]))

(def Connection
  [:map
   [:host string?]
   [:protocol [:enum "http" "https"]]
   [:port integer?]
   [:timeout pos-int?]])

(def NodeInfo
  [:map
   [:queue-length integer?]
   [:peers integer?]
   [:release integer?]
   [:current string?]
   [:blocks number?]
   [:network string?]
   [:version integer?]
   [:height integer?]
   [:node-state-latency integer?]])

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
