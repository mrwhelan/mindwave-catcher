(ns mindwave-catcher.core
  (:require [clojure.java.io :as io :only [writer reader]]
            [clojure.data.json :as json :only [read-str]]
            [clojure.walk :as walk :only [keywordize-keys]])
  (:import  [java.io BufferedReader]
            [java.net Socket]))

(defn startup-str
  "Builds the startup command string. If enabled is set to true,
   we will get all the EEG data sent, including readings from the
   forehead sensor."
  [enabled]
  (str "{\"enableRawOutput\":"
       enabled
       ", \"format\": \"Json\"}"))

(defn- clojurize
  "Turns json output into clojure data"
  [data-line]
  (-> data-line
      (json/read-str)
      (walk/keywordize-keys)))

(defn catch-waves
  "Connects to the ThinkGear Connector at the host and port,
   causing EEG data to be captured. If raw-output-enabled is
   set to true, all data will be sent including raw EEG data.
   Data is converted to clojure maps as it arrives, and function
   f will be applied to each map as it arrives."
  [host port raw-output-enabled f]
  (with-open [sock (Socket. host port)
              writer (io/writer sock)
              reader (BufferedReader. (io/reader sock))]
    (.append writer (startup-str raw-output-enabled))
    (.flush writer)
    (doseq [line (line-seq reader)]
      (f (clojurize line)))))
