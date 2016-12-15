(ns rules.next
  (:require [clara.rules :refer :all])
  (:require [rules.core :refer [patients]])
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

(def session2 (mk-session 'rules.next))

(comment

 (-> session2
     (insert-all patients)
     (fire-rules)
     (query tall-people)
     )
 )

;;; what's it all about?
;;

;; use it with Java http://www.clara-rules.org/docs/java/
;;
;; use other libs in your language
;; Java http://www.drools.org/
;; JavaScript https://github.com/C2FO/nools (deprecated)
