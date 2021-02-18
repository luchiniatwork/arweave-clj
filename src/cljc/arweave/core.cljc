(ns arweave.core
  (:refer-clojure :exclude [get])
  (:require [arweave.http :as http]
            [arweave.specs :as specs]
            [camel-snake-kebab.core :as csk]
            [clojure.core.async :refer [<! >! <!! >!! chan go go-loop]]
            [jsonista.core :as json]
            [malli.core :as m]))

(defn ^:private url [{:keys [protocol host port]} path]
  (str protocol "://" host ":" port "/" path))

(defn ^:private api-raw-get [{:keys [timeout] :as conn} url]
  (http/get url
            {:connection-timeout timeout}))

(defn ^:private api-get [{:keys [timeout] :as conn} url]
  (let [{:keys [body] :as resp} (http/get url
                                          {:accept :json
                                           :connection-timeout timeout})]
    (assoc resp
           :body-raw body
           :body (json/read-value body (json/object-mapper
                                        {:encode-key-fn csk/->snake_case_string
                                         :decode-key-fn csk/->kebab-case-keyword})))))

(defn create-conn
  ([]
   (create-conn nil))
  ([{:keys [port protocol] :as opts}]
   (let [protocol' (or protocol "https")
         port' (if (nil? port) (case protocol' "http" 80 "https" 443))]
     (merge {:host "arweave.net"
             :protocol protocol'
             :port port'
             :timeout 15000}
            opts))))

(defn info [conn]
  (go (:body (api-get conn (url conn "info")))))

(defn peers [conn]
  (go (:body (api-get conn (url conn "peers")))))

(defn status [conn tx-id]
  (go (let [{:keys [status body]} (api-get conn (url conn (str "tx/" tx-id "/status")))]
        (if (= 200 status)
          {:status 200
           :confirmed body}
          {:status status
           :confirmed nil}))))

(defn get [conn tx-id]
  (go (let [{:keys [status body]} (api-raw-get conn (url conn tx-id))]
        (println body)
        (case status
          200 body
          202 (throw (ex-info "TX Pending" {:anomaly/category :arweave.anomaly/tx-pending}))
          404 (throw (ex-info "TX Not Found" {:anomaly/category :arweave.anomaly/tx-not-found}))
          410 (throw (ex-info "TX Failed" {:anomaly/category :arweave.anomaly/tx-failed}))
          (throw (ex-info "TX Invalid" {:anomaly/category :arweave.anomaly/tx-invalid}))))))
