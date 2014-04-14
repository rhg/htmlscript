(ns htmlscript.server
  (:require
    [cljs.nodejs :as n.js]))

(defn- GET
  [http url cont]
  (let [body (atom [])
        req (.get http url (fn [res]
                             (.setEncoding res "utf8")
                             (.on res "data" (fn [chunk]
                                               (swap! body conj chunk)))
                             (.on res "end" (fn []
                                              (cont [(apply str @body) nil])))))]
    (.on req "error" #(cont [nil %]))))

(let [http (js/require "http")]
  (defn on-client
    [socket]
    (.on socket "disconnect" #(println "Finally!"))
    (.on socket "url" (fn [url]
                        (GET http url (fn [[body]]
                                        (.emit socket url body))))))
  (defn -main
    [& args]
    (let [sio (js/require "socket.io")
          io (.listen sio 8080)]
      (.on io "connection" on-client))))

(set! *main-cli-fn* -main)
