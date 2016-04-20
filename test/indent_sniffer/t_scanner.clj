(ns indent-sniffer.t-scanner
  (:require [midje.sweet :refer :all]
            [indent-sniffer.scanner :as subject]))

(fact "can scan lines of text and categorise"
      (subject/scan "") => {:blank true}
      (subject/scan " ") => {:blank true}
      (subject/scan " x") => {:blank false :tabs 0 :spaces 1}
      (subject/scan "  \t \tx") => {:blank false :tabs 2 :spaces 3}
      )

(tabular
  (fact "can categorise lines that match particular indent scenarios"
        (subject/matches {:blank ?blank :tabs ?tabs :spaces ?spaces} ?indent) => ?result)
  ?blank ?tabs ?spaces ?indent   ?result
  true   0     0       2         :indifferent
  false  0     0       2         :indifferent
  false  1     0       2         :bad
  false  0     2       2         :good
  false  0     3       2         :bad
  false  0     4       2         :good
  false  1     2       2         :bad

  true   0     0       3         :indifferent
  false  0     0       3         :indifferent
  false  1     0       3         :bad
  false  0     2       3         :bad
  false  0     3       3         :good
  false  0     4       3         :bad
  false  1     3       3         :bad

  true   0     0       4         :indifferent
  false  0     0       4         :indifferent
  false  1     0       4         :bad
  false  0     2       4         :bad
  false  0     3       4         :bad
  false  0     4       4         :good
  false  1     4       4         :bad

  true   0     0       0         :indifferent
  false  0     0       0         :indifferent
  false  1     0       0         :good
  false  0     2       0         :bad
  false  0     3       0         :bad
  false  0     4       0         :bad
  false  1     1       0         :bad
)

(tabular
  (fact "can calculate indent levels for a given scenario"
        (subject/line-indent {:blank ?blank :tabs ?tabs :spaces ?spaces} ?indent) => ?result)
  ?blank ?tabs ?spaces ?indent   ?result
  true   0     0       2         nil
  false  0     0       2         0
  false  0     2       2         1
  false  0     4       2         2
  false  0     5       2         nil
  false  2     0       0         2
)

(fact "can accumultate matches, defaulting to zero for simpler maths"
      (subject/match-frequencies [:good :good :bad])
      => {:good 2 :bad 1 :indifferent 0})

(fact "given match counts for lines in a file, the best matches above base threshold"
      (subject/best-indent [{:matches {:good 9 :bad 1 :indifferent 3}
                             :indent-size 2}
                            {:matches {:good 1 :bad 9 :indifferent 7}
                             :indent-size 3}])
      => {:matches {:good 9 :bad 1 :indifferent 3} :indent-size 2}

      (subject/best-indent [{:matches {:good 1 :bad 9 :indifferent 3}
                             :indent-size 2}
                            {:matches {:good 1 :bad 9 :indifferent 7}
                             :indent-size 3}])
      => nil

      (subject/best-indent [{:matches {:good 199 :bad 1 :indifferent 3}
                             :indent-size 2
                             :extra :foo}
                            {:matches {:good 198 :bad 1 :indifferent 3}
                             :indent-size 3
                             :extra :bar}])
      => {:matches {:good 198 :bad 1 :indifferent 3} :indent-size 3 :extra :bar}
      )

(fact "line stats get accumulated for each indent candidate"
      (subject/accumulate-line-stats
        ["  two"] [2])
      => (just (just {:indent-size 2
                      :matches     {:good 1 :bad 0 :indifferent 0}
                      :stats       (contains {:max 1 :mean (roughly 1.0)})
                      :indents     [1]
                      })
               )

      (subject/accumulate-line-stats
        [""
         "zero"
         "  two"
         "   three"
         "    four"] [2 4])
      => (just (just {:indent-size 2
                      :matches     {:bad 1 :good 2 :indifferent 2}
                      :stats       (contains {:max 2 :mean (roughly 1.0)})
                      :indents     [0 1 2]
                      })
               (contains {:indent-size 4
                          :matches     {:bad 2 :good 1 :indifferent 2}
                          :stats       (contains {:max 1 :sample-count 2 :mean 0.5})
                          :indents     [0 1]
                          })))

(future-fact "just the best match stats can be returned by best-line-stats"
      (subject/best-line-stats
        [""
         "zero"
         "  two"
         "    four"] [2 4])
      => {:indent-size  2
          :count   3
          :matches {:bad 0 :good 2 :indifferent 2}
          :max     2
          :mean    1.0
          :median  1.0
          :indents [0 1 2]}
      )