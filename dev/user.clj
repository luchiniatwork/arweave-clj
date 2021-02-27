(ns user
  (:require [arweave.core :as arweave]
            [arweave.utils :as utils]
            [clojure.core.async :refer [<! >! <!! >!! chan go go-loop]]))

(comment
  (arweave/create-conn)

  (<!! (arweave/info (arweave/create-conn)))

  (<!! (arweave/info (arweave/create-conn {:host "asd"})))

  (<!! (arweave/peers (arweave/create-conn)))

  (<!! (arweave/status (arweave/create-conn)
                       "ofxQ_02g2aW-fZ1tlxKYiVyWQeXVvvm7M4REIZ9l3To"))

  (<!! (arweave/get-data (arweave/create-conn)
                         "ofxQ_02g2aW-fZ1tlxKYiVyWQeXVvvm7M4REIZ9l3To"))

  (<!! (arweave/get-data (arweave/create-conn)
                         "KDKSOaecDl_IM4E0_0XiApwdrElvb9TnwOzeHt65Sno"))

  (<!! (arweave/get (arweave/create-conn)
                    "ofxQ_02g2aW-fZ1tlxKYiVyWQeXVvvm7M4REIZ9l3To"))

  (<!! (arweave/get (arweave/create-conn)
                    "bNbA3TEQVL60xlgCcqdz4ZPHFZ711cZ3hmkpGttDt_U"))
  )
