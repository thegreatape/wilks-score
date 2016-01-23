(ns wilks.core
  (:require [reagent.core :as r]))

(defonce state (r/atom {:lifts {}}))

(defn set-value! [id value]
  (swap! state assoc-in [:lifts id] value))

(defn get-value [id]
  (get-in @state [:lifts id]))

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

(def weight 190)

(defn lbs-to-kg [lbs]
  (* lbs 0.453592))

(defn wilks-coefficient []
  (let [{:keys [:a :b :c :d :e :f]} male-coefficients
        w (lbs-to-kg weight)]
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

(defn lift-input [lift-key]
  [:input {:value (get-value lift-key)
           :on-change #(set-value! lift-key (-> % .-target .-value))}])

(defn home []
  [:div.row
   [:div.col-xs-12
    [:h1 "Wilks Score Calculator"]
    [row "Squat" [lift-input :squat]]
    [row "Bench" [lift-input :bench]]
    [row "Deadlift" [lift-input :deadlift]]
    [row [:b "Wilks Score"] [:div (wilks-score)]]
   ]])

(defn mount-root []
 (r/render-component [home] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
