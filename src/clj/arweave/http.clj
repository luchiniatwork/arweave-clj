(ns arweave.http
  (:refer-clojure :exclude [get])
  (:require [clj-http.client :as http]))

(defn get
  ([url]
   (get url nil))
  ([url opts]
   (http/get url opts)))
