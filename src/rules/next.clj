(ns rules.next
  (:require [clara.rules :refer :all]
            [clara.rules.accumulators :as acc]
            [rules.core :refer [patients]]
            [clj-time.core :as joda])
  (:import [rules.core Patient]))

(defrecord Tall [id])

(defrule tall-rule
  [Patient (= ?id id) (= ?gender gender) (= ?height height)
   (or
    (and (= ?gender :male) (>= ?height 1.90))
    (and (= ?gender :female) (>= ?height 1.70)))]
  =>
  (insert! (->Tall ?id)))

(defquery tall-people
  []
  [?tall <- Tall])

(defrule tall-women-receive-a-promo-code
  [Tall (= ?id id)]
  [Patient (= ?id id)
   (= ?gender gender)
   (= gender :female)]
  =>
  ;; do stuff if rule matches
  (println "Promo for " ?id))

(defrecord Purchase [id date price])

(defrecord Total [total])

(def purchases [(->Purchase "Iggy" (joda/date-time 2015 4 1) 33.3)
                (->Purchase "Madonna" (joda/date-time 2015 6 3) 120.0)
                (->Purchase "Iggy" (joda/date-time 2015 4 1) 166.0)
                (->Purchase "Edwin" (joda/date-time 2016 1 1) 34)
                (->Purchase "Franz" (joda/date-time 2015 3 27) 89)])

(defrule total-purchases
  [?total <- (acc/sum :price) :from [Purchase]]
  =>
  (insert! (->Total ?total)))

(defquery purchases-in-year [:?year]
  [?purchase <- Purchase
   (= ?id id)
   (= ?date date)
   (= ?year (joda/year date))
   (joda/within?
    (joda/date-time ?year 1 1)
    (joda/date-midnight (inc ?year) 1 1)
    date)])

(defquery total []
  [?total <- Total])

(def session2 (mk-session 'rules.next))

(comment

  (-> session2
      (insert-all patients)
      (fire-rules)
      (query tall-people)
      )

  (-> session2
      (insert-all purchases)
      (fire-rules)
      (query total)
      )

    (-> session2
      (insert-all purchases)
      (fire-rules)
      (query purchases-in-year :?year 2016)
      )
  
  )

;;; what to do with this?
;;
;; Medical recommendations
;; If-else customer logic

;; use it with Java http://www.clara-rules.org/docs/java/
;;
;; use other libs in your language
;; Java http://www.drools.org/
;; JavaScript https://github.com/C2FO/nools (deprecated)
