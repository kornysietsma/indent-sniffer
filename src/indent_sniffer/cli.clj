(ns indent-sniffer.cli
  (:require
    [indent-sniffer.scanner :as scanner]
    [cheshire.core :as cheshire]
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [me.raynes.fs :as fs]
    [clojure.pprint :refer [pprint]]
    [taoensso.timbre :as timbre])
  (:gen-class)
  (:import (java.io File)))

(def cli-options
  [["-o" "--output filename" "select an output file name (default is STDOUT)"]
   ["-e" "--extensions list-of-file-extensions" "list of file extensions to scan - case insensitive, comma separated"]
   ["-i" "--hidden" "include hidden directories and files"]
   ["-l" "--log" "log more info for un-indentable files"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Utilities for slurping and combining source code metrics"
        ""
        "Usage: indent-sniff [options] files or directories"
        ""
        "Options:"
        options-summary
        ""
        "Files or directories:"
        "individual files can be named, or you can specify directories to be recursively scanned (use other options to filter)"
        ""]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  (binding [*out* *err*]
    (println msg))
  (System/exit status))

(def default-indents [0 2 3 4])

(defn file-ends-with [file extension]
  (-> file
         .getName
         .toLowerCase
         (.endsWith extension)))

(defn filter-files [{:keys [hidden extensions]} file]
  (if (and (not hidden) (.isHidden file))
    false
    (if (nil? extensions)
      true
      (if (and (.isFile file) (not-any? (partial file-ends-with file) extensions))
        false
        true))))


(defn scan-file-or-files [options ^File file]
  (timbre/debug "scanning file/dir " file)
  (if (.isFile file)
    (let [rdr (io/reader file)
          out (scanner/best-file-line-stats rdr default-indents)]
      {:filename (.getPath file)
       :data     {:indents out}})
    (->> (.listFiles file)
         (filter (partial filter-files options))
         (map (partial scan-file-or-files options)))))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (zero? (count arguments)) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (if (:log options)
      (timbre/set-level! :debug)
      (timbre/set-level! :warn))
    (let [out-file (if (:output options)
                     (io/writer (:output options))
                     *out*)
          extensions (if (:extensions options)
                       (map string/lower-case (string/split (:extensions options) #","))
                       nil)
          hidden (:hidden options)]
      (try
        (->
          (for [file (map io/file arguments)]
            (scan-file-or-files {:extensions extensions :hidden hidden} file))
          flatten
          (cheshire/generate-stream out-file {:pretty true}))
        (finally
          (if (:output options)
            (.close out-file)))))))
