(ns wilks.core
  (:require [reagent.core :as r]))

(defonce state (r/atom {:lifts {} :gender "male"}))

(defn set-value! [path value]
  (swap! state assoc-in path value))

(defn get-value [path]
  (get-in @state path))

(defn total []
  (let [{:keys [:squat :deadlift :bench]} (:lifts @state)
        lifts [squat deadlift bench]]
    (when (not-any? clojure.string/blank? lifts)
      (apply + (map js/parseInt lifts)))))

(def male-coefficients
  {:a -216.0475144
   :b 16.2606339
   :c -0.002388645
   :d -0.00113732
   :e 7.01863e-06
   :f -1.291e-08 })

(def female-coefficients
  {:a 594.31747775582
   :b -27.23842536447
   :c 0.82112226871
   :d -0.00930733913
   :e 0.00004731582
   :f -0.00000009054})

(defn lbs-to-kg [lbs]
  (* lbs 0.453592))

(defn gendered-coefficients []
  (if (= "male" (get-value [:gender]))
    male-coefficients
    female-coefficients))

(defn wilks-coefficient []
  (let [{:keys [:a :b :c :d :e :f]} (gendered-coefficients)
        w (lbs-to-kg (js/parseInt (get-value [:weight])))]
    (/ 500 (+
             a
             (* b w)
             (* c (.pow js/Math w 2))
             (* d (.pow js/Math w 3))
             (* e (.pow js/Math w 4))
             (* f (.pow js/Math w 5))))))

(defn wilks-score []
  (if-not (nil? (total))
    (let [score (* (lbs-to-kg (total)) (wilks-coefficient))]
      (.round js/Math score))))

(defn row [label & body]
  [:div.row
   [:div.col-md-2
    [:span label]]
   [:div.col-md-3 body ] ])

(defn bound-input [k]
  [:input {:value (get-value k)
           :on-change #(set-value! k (-> % .-target .-value))}])

(defn lift-input [lift-key]
  (bound-input [:lifts lift-key]))

(defn weight-input []
  (bound-input [:weight]))

(defn gender-select []
  [:select {:on-change #(set-value! [:gender] (-> % .-target .-value))}
   [:option {:value "male" :selected (= (get-value [:gender]) "male")} "Male"]
   [:option {:value "female" :selected (= (get-value [:gender]) "female")} "Female"]])

(defn explanation []
  [:p
   [:a
    {:href "https://en.wikipedia.org/wiki/Wilks_Coefficient"}
    "The Wilks Formula " ]
   "is a score used to compare the relative strengths of powerlifters
   while adjusting for bodyweight and gender."])

(defn footer []
  [:div.footer
   [:a {:href "https://github.com/thegreatape/wilks-score"} "Source"]
   " | "
   "Written by Thomas Mayfield, mostly to learn a bit of "
   [:a {:href "https://github.com/clojure/clojurescript"} "ClojureScript"]])

(defn home []
  [:div.row.jumbotron
   [:div.col-xs-12
    [:h1 "Wilks Score Calculator"]
    (explanation)
    [row "Gender" [gender-select]]
    [row "Weight (lbs)" [weight-input]]
    [row "Squat" [lift-input :squat]]
    [row "Bench" [lift-input :bench]]
    [row "Deadlift" [lift-input :deadlift]]
    [row [:b "Wilks Score"] [:b (wilks-score)]]
    (footer)]])

(defn mount-root []
 (r/render-component [home] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
