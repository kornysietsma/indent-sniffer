(ns indent-sniffer.scanner
  (:require [indent-sniffer.bitmaps :as bitmaps]
            [clojure.string :as string]
            [clojure.set :as set]
            [com.stuartsierra.frequencies :as freq]
            [clojure.pprint :refer [pprint]]
            [taoensso.timbre :as timbre
             :refer (trace  debug  info  warn  error  fatal)]))

(def ws-pattern #"^([ \t]*)(.*)$")

(defn scan "categorise text by number of spaces, tabs, and blankness"
  [text]
  (let [[_ ws rem] (re-matches ws-pattern text)]
    (cond
      (empty? rem) {:blank true}
      (empty? ws) {:blank false :spaces 0 :tabs 0}
      :else (merge {:blank false :spaces 0 :tabs 0} (-> (frequencies ws)
                                                        (set/rename-keys {\space :spaces \tab :tabs}))))))

(defn matches "does a scan result match an indent scenario"
  [{:keys [blank spaces tabs]} indent]
  (if (zero? indent)                                        ; tab
    (cond
      blank :indifferent
      (= 0 spaces tabs) :indifferent
      (not= 0 spaces) :bad
      :else :good)
    (cond
      blank :indifferent
      (= 0 spaces tabs) :indifferent
      (not= 0 tabs) :bad
      (= 0 (rem spaces indent)) :good
      :else :bad
      )))

(def guessed-spaces-if-tabs 0.5)
(def guessed-tabs-if-spaces 4)

(defn line-indent
  "calculate the indent 'value' for a given line and scenario, or nil if it's a bad match"
  ([line-matches indent] (line-indent line-matches indent false))
  ([{:keys [blank spaces tabs]} indent best-guess]
   (if (zero? indent)                                        ; tab
     (cond
       blank nil
       (= 0 spaces tabs) 0
       (not= 0 spaces) (if best-guess
                         (+ tabs (* guessed-spaces-if-tabs spaces))
                         nil)
       :else tabs)
     (cond
       blank nil
       (= 0 spaces tabs) 0
       (not= 0 tabs) (if best-guess
                       (+ (/ spaces indent) (* guessed-tabs-if-spaces tabs))
                       nil)
       (= 0 (rem spaces indent)) (/ spaces indent)
       :else (if best-guess
               (/ spaces indent)
               nil)))))


; indent rules
; eliminate any indent matches that have less than a minimum % good - usually should be near 100% but rogue tabs or comments might mess things up.  Not going to try to guess what mixed tabs and spaces means!
(def minimum-good 0.7)
; if we have no matches, going to report that for now so we can tune.  Might be we default to ignore, or use spaces.

; if we have multiple matches, we want the biggest one, as long as they are close, whatever that means!
;   for example, A: if indent is really 4, but someone has used 2 for a small chunk, ideally we'd use 4
;   if however B: indent is really 2 but we happen to have a lot of 4-level lines, we would like to guess 2.
; in A, we should have mostly 4 but some 2 - and 2 will always be >= 4!  So maybe 0.9 at 4, 0.95 at 2.
; in B, we should have quite a lot more of a gap - so maybe 0.8 at 4, 0.95 at 2.

; for now, just use a tougher threshold if multiple matches happen.
(def minimum-good-multiple-matches 0.9)

(defn- above-mingood
  [{{:keys [good bad indifferent]} :matches}]
  (if (zero? (+ good bad))
    false
    (< minimum-good (/ good (+ good bad)))))

(defn- above-mingood-multi
  [{{:keys [good bad indifferent]} :matches}]
  (if (zero? (+ good bad))
    false
    (< minimum-good-multiple-matches (/ good (+ good bad)))))

(defn- biggest-indent [indents]
  (->> indents
       (sort-by :indent-size)
       last))

(defn best-indent "find the best indent stats based on matches"
  [indent-matches]
  (let [good-indents (filter above-mingood indent-matches)
        best-indents (filter above-mingood-multi indent-matches)]
    (cond
      (empty? good-indents) (do
                              (if (timbre/log? :info)
                                (info "no good indents in" (pr-str indent-matches)))
                              nil)
      (= 1 (count good-indents)) (first good-indents)
      (= 1 (count best-indents)) (first best-indents)
      (empty? best-indents) (biggest-indent good-indents)
      :else (biggest-indent best-indents))))


(defn- scan-lines [lines]
  (map scan lines))

(defn match-frequencies [matches]
  (let [{:keys [good bad indifferent]} (frequencies matches)]
    {:good (or good 0)
     :bad (or bad 0)
     :indifferent (or indifferent 0)}))

(defn accumulate-line-stats [lines indent-scenarios]
  (for [indent-size indent-scenarios]
    (let [line-infos (scan-lines lines)
          line-matches (map #(matches % indent-size) line-infos)
          line-match-stats (match-frequencies line-matches)
          line-indents (filter identity (map #(line-indent % indent-size true) line-infos))]
      {:indent-size indent-size
       :matches     line-match-stats
       :stats       (freq/stats (frequencies line-indents))
       :indents     (bitmaps/indents->imageurl line-indents)})))

(defn best-line-stats [lines indents]
  (if-let [line-stats (accumulate-line-stats lines indents)]
    (best-indent line-stats)
    nil))

(defn best-file-line-stats [reader indents]
  (best-line-stats (line-seq reader) indents))