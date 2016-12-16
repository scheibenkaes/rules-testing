(ns rules.core
  (:require [clara.rules :refer :all]
            [clara.tools.inspect :as inspect]
            [rules.bmi :as bmidx]))

;; Data
(defrecord Patient [id weight height gender])


(def patients
  [(map->Patient {:id "Bob" :weight 90 :height 1.75 :gender :male})
   (map->Patient {:id "Alice" :weight 70 :height 1.57 :gender :female})
   (map->Patient {:id "Cyrill" :weight 120 :height 1.90 :gender :male})
   (map->Patient {:id "Denise" :weight 55 :height 1.65 :gender :female})
   (map->Patient {:id "Edwin" :weight 82 :height 2.01 :gender :male})
   (map->Patient {:id "Franz" :weight 182 :height 1.77 :gender :male})
   (map->Patient {:id "Iggy" :weight 42 :height 1.61 :gender :male})
   (map->Patient {:id "Madonna" :weight 42 :height 1.71 :gender :female})])

;; Facts
(defrecord Overweight [id])

(defrecord Underweight [id])

(defrecord BMI [id bmi])

;; Rulez
(defrule bmi-rule
  [Patient [{id :id :as patient}]
   (= ?id id)
   (= ?bmi (bmidx/calculate-bmi patient))]
  =>
  (insert! (->BMI ?id ?bmi)))

(defrule overweight-rule
  [Patient
   (= ?id id)]
  [BMI
   (= ?id id)
   (= ?bmi bmi)
   (>= ?bmi 25)]
  =>
  (insert! (->Overweight ?id)))

(defrule underweight-rule
  [Patient
   (= ?id id)]
  [BMI
   (= ?id id)
   (= ?bmi bmi)
   (< ?bmi 18.5)]
  =>
  (insert! (->Underweight ?id)))

(defquery get-overweights
  []
  [?overweight <- Overweight])

(defquery get-underweights
  []
  [?underweight <- Underweight])

(defquery overweight-persons
  [:?gender]
  [Patient (= ?id id) (= ?gender gender)]
  [Overweight (= ?id id)])

(defquery underweight-persons
  [:?gender]
  [Patient (= ?id id) (= ?gender gender)]
  [Underweight (= ?id id)])

(def session (mk-session 'rules.core))

(def inspected-session
  (-> (mk-session 'rules.core :cache false)
      (insert (->Patient "Tim" 190 1.55 :male)
              (->Patient "Tina" 56 1.60 :female))
      (fire-rules)))

(comment
  (map bmidx/calculate-bmi patients)

  (-> session
      (insert-all patients)
      (fire-rules)
      #_(query underweight-persons :?gender :male)
      (query get-underweights)


      )

  ;; see repl
  (inspect/explain-activations inspected-session)

  ;; what's next?

  )
