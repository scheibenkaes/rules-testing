(ns rules.core
  (:require [clara.rules :refer :all]))

;; https://en.wikipedia.org/wiki/Body_mass_index#Categories
(defn calculate-bmi
  "Calculate the body mass index for the patient given."
  [{weight-kg :weight height-m :height}]
  (/ weight-kg
     (Math/pow height-m 2)))

(defrecord Patient [id weight height gender])

(defrecord Overweight [id])

(defrecord Underweight [id])

(defrecord BMI [id bmi])

(def patients
  [(map->Patient {:id "Bob" :weight 90 :height 1.75 :gender :male})
   (map->Patient {:id "Alice" :weight 70 :height 1.57 :gender :female})
   (map->Patient {:id "Cyrill" :weight 120 :height 1.90 :gender :male})
   (map->Patient {:id "Denise" :weight 55 :height 1.65 :gender :female})
   (map->Patient {:id "Edwin" :weight 82 :height 2.01 :gender :male})
   (map->Patient {:id "Franz" :weight 182 :height 1.77 :gender :male})
   (map->Patient {:id "Iggy" :weight 42 :height 1.61 :gender :male})])

(defrule bmi-rule
  [Patient [{id :id :as patient}]
   (= ?id id)
   (= ?bmi (calculate-bmi patient))]
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

(comment

  (-> session
      (insert-all patients)
      (fire-rules)
      #_(query underweight-persons :?gender :male)
      (query get-underweights)
      )

  (map calculate-bmi patients)

  )
