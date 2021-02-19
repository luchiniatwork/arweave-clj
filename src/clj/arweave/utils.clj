(ns arweave.utils
  (:import (java.util Base64)))

#_(defn ^String bytes->base64 [to-encode]
    (.encode (Base64/getEncoder) to-encode))

(defn ^String str->base64 [^String to-encode]
  (.encode (Base64/getEncoder) (.getBytes to-encode)))

(defn ^String base64-str->str [^String to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))
