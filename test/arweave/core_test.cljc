(ns arweave.core-test
  (:require [arweave.core :as arweave]
            [arweave.specs :as specs]
            [clojure.core.async :refer [<! >! <!! >!! chan go go-loop]]
            [clojure.test :refer [deftest is are testing use-fixtures]]
            [malli.core :as m]))

(def live-data-tx-id "bNbA3TEQVL60xlgCcqdz4ZPHFZ711cZ3hmkpGttDt_U")

(def live-data-tx-id-large "KDKSOaecDl_IM4E0_0XiApwdrElvb9TnwOzeHt65Sno")

(deftest connection
  (testing "default conn"
    (let [{:keys [host port protocol timeout] :as conn} (arweave/create-conn)]
      (is (= "arweave.net" host))
      (is (= 443 port))
      (is (= "https" protocol))
      (is (= 15000 timeout))
      (is (nil? (m/explain specs/Connection conn)))))

  (testing "alternative host"
    (let [{:keys [host port protocol timeout] :as conn} (arweave/create-conn {:host "localhost"})]
      (is (= "localhost" host))
      (is (= 443 port))
      (is (= "https" protocol))
      (is (= 15000 timeout))
      (is (nil? (m/explain specs/Connection conn)))))

  (testing "auto port"
    (let [{:keys [host port protocol timeout] :as conn} (arweave/create-conn {:host "localhost"
                                                                              :protocol "http"})]
      (is (= "localhost" host))
      (is (= 80 port))
      (is (= "http" protocol))
      (is (= 15000 timeout))
      (is (nil? (m/explain specs/Connection conn))))))


(deftest info
  (testing "info from default conn"
    (is (nil? (m/explain specs/NodeInfo
                         (<!! (arweave/info (arweave/create-conn)))))))

  (testing "info from wrong conn"
    (let [resp (<!! (arweave/info nil))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-invalid (:anomaly/category resp)))))

  (testing "info from unavailable conn"
    (let [resp (<!! (arweave/info (arweave/create-conn {:host ""})))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-error (:anomaly/category resp))))))

(deftest peers
  (testing "peers from default conn"
    (is (nil? (m/explain specs/NodePeers
                         (<!! (arweave/peers (arweave/create-conn)))))))

  (testing "peers from wrong conn"
    (let [resp (<!! (arweave/peers nil))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-invalid (:anomaly/category resp)))))

  (testing "peers from unavailable conn"
    (let [resp (<!! (arweave/peers (arweave/create-conn {:host ""})))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-error (:anomaly/category resp))))))

(deftest transaction-status
  (testing "successful tx status"
    (is (nil? (m/explain specs/TxStatus
                         (<!! (arweave/status (arweave/create-conn)
                                              live-data-tx-id))))))

  (testing "non existing tx"
    (let [resp (<!! (arweave/status (arweave/create-conn)
                                    "foobar"))]
      (is (nil? (m/explain specs/TxStatus resp)))
      (is (= {:status 404 :confirmed nil} resp))))

  (testing "wrong conn tx status"
    (let [resp (<!! (arweave/status nil live-data-tx-id))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-invalid (:anomaly/category resp)))))

  (testing "unavailable conn tx status"
    (let [resp (<!! (arweave/status (arweave/create-conn {:host ""})
                                    live-data-tx-id))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-error (:anomaly/category resp))))))

(deftest transaction-getting
  (testing "successful tx get-data (default)"
    (let [resp (<!! (arweave/get-data (arweave/create-conn) live-data-tx-id))]
      (is (string? resp))
      (is (= '(\newline \< \! \D \O \C \T \Y \P \E \space \h \t \m \l \> \newline)
             (take 17 resp)))
      (is (= '(\< \/ \b \o \d \y \> \newline \< \/ \h \t \m \l \>)
             (take-last 15 resp)))
      (is (= 12517 (count resp)))))

  (testing "successful tx get"
    (let [resp (<!! (arweave/get (arweave/create-conn) live-data-tx-id))]
      (is (nil? (m/explain specs/Transaction resp)))
      (is (= [{:name "Q29udGVudC1UeXBl" :value "dGV4dC9odG1s"}
              {:name "VXNlci1BZ2VudA" :value "QXJ3ZWF2ZURlcGxveS8xLjEuMA"}]
             (:tags resp)))
      (is (= "12517" (:data-size resp)))
      (is (= live-data-tx-id (:id resp)))

      #_(is (string? resp))
      #_(is (= '(\newline \< \! \D \O \C \T \Y \P \E \space \h \t \m \l \> \newline)
               (take 17 resp)))
      #_(is (= '(\< \/ \b \o \d \y \> \newline \< \/ \h \t \m \l \>)
               (take-last 15 resp)))
      #_(is (= 12517 (count resp)))
      #_(is (and (boolean? (arweave/verify resp))
                 (= true (arweave/verify resp))))))

  (testing "successful tx get-data (:as :utf-8)"
    (let [resp (<!! (arweave/get-data (arweave/create-conn) live-data-tx-id {:as :utf-8}))]
      (is (string? resp))
      (is (= '(\newline \< \! \D \O \C \T \Y \P \E \space \h \t \m \l \> \newline)
             (take 17 resp)))
      (is (= '(\< \/ \b \o \d \y \> \newline \< \/ \h \t \m \l \>)
             (take-last 15 resp)))
      (is (= 12517 (count resp)))))

  (testing "successful tx get-data (:as :byte-array)"
    (let [resp (<!! (arweave/get-data (arweave/create-conn) live-data-tx-id {:as :byte-array}))]
      (is (bytes? resp))
      (is (= '(10 60 33 68 79 67 84 89 80 69 32 104 116 109 108 62 10 60 104 116)
             (take 20 resp)))
      (is (= '(121 62 10 60 47 104 116 109 108 62)
             (take-last 10 resp)))
      (is (= 12517 (count resp)))))

  (testing "non existing messed up tx"
    (let [resp (<!! (arweave/get-data (arweave/create-conn)
                                      "foobar"))]
      (is (m/validate specs/Anomaly resp))
      (is (or (= :arweave.anomaly/tx-invalid (:anomaly/category resp))
              (= :arweave.anomaly/tx-not-found (:anomaly/category resp))))))

  (testing "non existing tx"
    (let [resp (<!! (arweave/get-data (arweave/create-conn)
                                      "foobarfoobarfoobar12312312312312asdasdasdasdasd3123"))]
      (is (m/validate specs/Anomaly resp))
      (is (or (= :arweave.anomaly/tx-invalid (:anomaly/category resp))
              (= :arweave.anomaly/tx-not-found (:anomaly/category resp))))))

  (testing "wrong conn tx"
    (let [resp (<!! (arweave/get-data nil live-data-tx-id))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-invalid (:anomaly/category resp)))))

  (testing "unavailable conn tx"
    (let [resp (<!! (arweave/get-data (arweave/create-conn {:host ""}) live-data-tx-id))]
      (is (m/validate specs/Anomaly resp))
      (is (= :arweave.anomaly/conn-error (:anomaly/category resp)))))

  #_(testing "successful tx get large tx (:as :byte-array)"
      (let [resp (<!! (arweave/get-data (arweave/create-conn)
                                        live-data-tx-id-large
                                        {:as :byte-array}))]
        (is (bytes? resp))
        (is (= '(0 0 0 24 102 116 121 112 109 112)
               (take 10 resp)))
        (is (= '(-80 54 -79 114 -106 76 102 39 49 -36)
               (take-last 10 resp)))
        (is (= 14166765 (count resp))))))
