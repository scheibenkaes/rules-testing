(ns rules.bmi)

;; https://en.wikipedia.org/wiki/Body_mass_index
(defn calculate-bmi
  "Calculate the body mass index for the patient given."
  [{weight-kg :weight height-m :height}]
  {:pre [(pos? height-m)]}
  (/ weight-kg
     (Math/pow height-m 2)))
