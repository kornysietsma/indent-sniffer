(ns indent-sniffer.cli
  (:require
    [indent-sniffer.scanner :as scanner]
    [cheshire.core :as cheshire]
    [clojure.tools.cli :refer [parse-opts]]
    [clojure.java.io :as io]
    [clojure.string :as string]
    [me.raynes.fs :as fs]
    [clojure.pprint :refer [pprint]])
  (:gen-class))

(def cli-options
  [["-o" "--output filename" "select an output file name (default is STDOUT)"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["Utilities for slurping and combining source code metrics"
        ""
        "Usage: indent-sniff [options] files"
        ""
        "Options:"
        options-summary
        ""
        "Files:"
        "Files is one or more files - if you run zsh use a glob here like **/*.clj to traverse the filesystem"
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

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (zero? (count arguments)) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    (let [out-file (if (:output options)
                     (io/writer (:output options))
                     *out*)]
      (try
        (->
          (for [file arguments]
            (let [rdr (io/reader file)
                  out (scanner/best-file-line-stats rdr default-indents)]
              {:filename file
               :data     {:indents out}}))
          (cheshire/generate-stream out-file {:pretty true}))
        (finally
          (if (:output options)
            (.close out-file)))))))
