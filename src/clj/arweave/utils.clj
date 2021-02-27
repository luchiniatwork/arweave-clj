(ns arweave.utils
  (:import (java.util Base64)))

#_(defn ^String bytes->base64 [to-encode]
    (.encode (Base64/getEncoder) to-encode))

(defn ^String str->base64url [^String to-encode]
  (when to-encode
    (String. (.encode (Base64/getUrlEncoder) (.getBytes to-encode)))))

(defn ^String base64url->str [^String to-decode]
  (when to-decode
    (String. (.decode (Base64/getUrlDecoder) (.getBytes to-decode)))))
