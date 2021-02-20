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

(defn ^:private anomaly? [{:keys [anomaly/category]}]
  (not (nil? category)))

(defn ^:private validate-conn [conn]
  (when (not (m/validate specs/Connection conn))
    {:anomaly/category :arweave.anomaly/conn-invalid
     :anomaly/message "Invalid connection"}))

(defn ^:private api-raw-get
  ([conn url]
   (api-raw-get conn url {:expected-status-fn (constantly true)}))
  ([{:keys [timeout] :as conn} url {:keys [expected-status-fn as]}]
   (or (validate-conn conn)
       (try
         (let [{:keys [status body] :as resp} (http/get url
                                                        {:as as
                                                         :connection-timeout timeout
                                                         :throw-exceptions false})]
           (if (expected-status-fn status)
             resp
             {:anomaly/category :arweave.anomaly/conn-error
              :anomaly/message "Connection error"
              :anomaly/provenance {:status status
                                   :body body}}))
         (catch Throwable ex
           {:anomaly/category :arweave.anomaly/conn-error
            :anomaly/message "Connection error"
            :anomaly/provenance (Throwable->map ex)})))))

(defn ^:private api-get
  ([conn url]
   (api-get conn url {:expected-status-fn (constantly true)}))
  ([{:keys [timeout] :as conn} url {:keys [expected-status-fn]}]
   (or (validate-conn conn)
       (try
         (let [{:keys [status body] :as resp} (http/get url
                                                        {:accept :json
                                                         :connection-timeout timeout
                                                         :throw-exceptions false})]
           (if (expected-status-fn status)
             (assoc resp
                    :body-raw body
                    :body (json/read-value body (json/object-mapper
                                                 {:encode-key-fn csk/->snake_case_string
                                                  :decode-key-fn csk/->kebab-case-keyword})))
             {:anomaly/category :arweave.anomaly/conn-error
              :anomaly/message "Connection error"
              :anomaly/provenance {:status status
                                   :body body}}))
         (catch Throwable ex
           {:anomaly/category :arweave.anomaly/conn-error
            :anomaly/message "Connection error"
            :anomaly/provenance (Throwable->map ex)})))))


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
  (go (let [resp (api-get conn (url conn "info") {:expected-status-fn #(= 200 %)})]
        (if (anomaly? resp)
          resp
          (:body resp)))) )

(defn peers [conn]
  (go (let [resp (api-get conn (url conn "peers") {:expected-status-fn #(= 200 %)})]
        (if (anomaly? resp)
          resp
          (:body resp)))))

(defn status [conn tx-id]
  (go (let [{:keys [status body] :as resp} (api-get conn (url conn (str "tx/" tx-id "/status")))]
        (if (anomaly? resp)
          resp
          (if (= 200 status)
            {:status 200
             :confirmed body}
            {:status status
             :confirmed nil})))))

(defn get
  ([conn tx-id]
   (get conn tx-id {:as :utf-8}))
  ([conn tx-id {:keys [as]}]
   (go (let [{:keys [status body] :as resp}
             (api-raw-get conn (url conn tx-id)
                          {:expected-status-fn #(get #{200 202 400 404 410} %)
                           :as as})]
         (if (anomaly? resp)
           resp
           (case status
             200 body
             202 {:anomaly/category :arweave.anomaly/tx-pending
                  :anomaly/message "TX pending"}
             404 {:anomaly/category :arweave.anomaly/tx-not-found
                  :anomaly/message "TX not found"}
             410 {:anomaly/category :arweave.anomaly/tx-failed
                  :anomaly/message "TX failed"}
             {:anomaly/category :arweave.anomaly/tx-invalid
              :anomaly/message "TX invalid"}))))))

(defn verify
  [conn tx-id]
  (or (validate-conn conn)
      ))
