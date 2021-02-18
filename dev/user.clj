(ns user
  (:require [arweave.core :as arweave]
            [clojure.core.async :refer [<! >! <!! >!! chan go go-loop]]))

(comment
  (arweave/create-conn)

  (<!! (arweave/info (arweave/create-conn)))

  (<!! (arweave/peers (arweave/create-conn)))

  (<!! (arweave/status (arweave/create-conn)
                       "ofxQ_02g2aW-fZ1tlxKYiVyWQeXVvvm7M4REIZ9l3To"))

  (<!! (arweave/get (arweave/create-conn)
                    "ofxQ_02g2aW-fZ1tlxKYiVyWQeXVvvm7M4REIZ9l3T"))
  )
