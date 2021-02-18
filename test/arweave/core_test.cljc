(ns arweave.core-test
  (:require [arweave.core :as arweave]
            [arweave.specs :as specs]
            [clojure.core.async :refer [<! >! <!! >!! chan go go-loop]]
            [clojure.test :refer [deftest is are testing use-fixtures]]
            [malli.core :as m]))

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
  (is (nil? (m/explain specs/NodeInfo
                       (<!! (arweave/info (arweave/create-conn)))))))

(deftest peers
  (is (nil? (m/explain specs/NodePeers
                       (<!! (arweave/peers (arweave/create-conn)))))))

(deftest transaction-querying
  (testing "status"
    (is (nil? (m/explain specs/TxStatus
                         (<!! (arweave/status (arweave/create-conn)
                                              "ofxQ_02g2aW-fZ1tlxKYiVyWQeXVvvm7M4REIZ9l3To")))))))
