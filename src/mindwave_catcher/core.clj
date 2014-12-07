(ns mindwave-catcher.core
  (:require [clojure.java.io :as io :only [writer reader]]
            [clojure.data.json :as json :only [read-str]]
            [clojure.walk :as walk :only [keywordize-keys]])
  (:import  [java.io BufferedReader]
            [java.net Socket]))

  (def startup (str "{\"enableRawOutput\": true, \"format\": \"Json\"}"))

  (defn- clojurize
    "Turns json output into clojure data"
    [data-line]
    (-> data-line
        (json/read-str)
        (walk/keywordize-keys)))

  (defn catch-waves
    "Connects to the ThinkGear Connector at the host and port,
     then prints the EEG data that issues forth, as Clojure maps."
    [host port]
    (with-open [sock (Socket. host port)
                writer (io/writer sock)
                reader (BufferedReader. (io/reader sock))]
      (.append writer startup)
      (.flush writer)
      (doseq [line (line-seq reader)]
        (let [clj-line (clojurize line)]
          (if (:eSense clj-line) (println (:eegPower clj-line)))))))
