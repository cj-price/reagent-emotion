(ns emotion.core
  (:require ["@emotion/react" :as emotion]
            [reagent.impl.protocols :as p]
            [reagent.impl.template :as t]
            [reagent.core :as r]
            [reagent.dom :as rdom]))

(defn simple-test-0 []
  [:div {:css {"&:after" {:content "\"Hi\""}}}])

(defn simple-test-1 []
  [:div {:css {:background-color "purple"
               :color            "pink !important"}}
   "Simple test"])

(defn simple-test-n []
  [:div {:css {:background-color "pink"
               :color            "purple !important"}}
   "A simple component that uses emotions "
   [:code ":css"]
   " prop to change colors"])

(defn shorthand-test []
  [:div>p {:css {:transform "rotate(180deg)"}}
   "Rotate your text 180deg"])

(defn functional-test []
  [:div {:css {:animation          "wiggle 3s infinite"
               :text-align         "center"
               :transform-origin   "center"
               "@keyframes wiggle" {"0%"   {:transform "rotate(0deg)"}
                                    "25%"  {:transform "rotate(0deg)"}
                                    "50%"  {:transform "rotate(5deg)"}
                                    "75%"  {:transform "rotate(-5deg)"}
                                    "100%" {:transform "rotate(0deg)"}}}}
   "This is a functional component that has some styling"])

(defn emotion-components []
  [:div
   [simple-test-0]
   [simple-test-1]
   [simple-test-n]
   [shorthand-test]
   [:f> functional-test]])

;;; Emotion compiler

(defn make-element
  [this argv component jsprops first-child]
  (case (- (count argv) first-child)
    ;; Optimize cases of zero or one child
    0 (emotion/jsx component jsprops)

    1 (emotion/jsx component jsprops
        (p/as-element this (nth argv first-child nil)))

    (.apply emotion/jsx nil
      (reduce-kv (fn [a k v]
                   (when (>= k first-child)
                     (.push a (p/as-element this v)))
                   a)
        #js [component jsprops] argv))))

(defn emotion-compiler [opts]
  (let [id (gensym "reagent-compiler")
        fn-to-element (if (:function-components opts)
                        t/maybe-function-element
                        t/reag-element)
        parse-fn (get opts :parse-tag t/cached-parse)]
    (reify p/Compiler
      ;; This is used to as cache key to cache component fns per compiler
      (get-id [this] id)
      (parse-tag [this tag-name tag-value]
        (parse-fn this tag-name tag-value))
      (as-element [this x]
        (t/as-element this x fn-to-element))
      (make-element [this argv component jsprops first-child]
        (make-element this argv component jsprops first-child)))))

(def compiler (emotion-compiler {}))

(defn simple-example []
  (r/create-class
    {:component-did-mount #(rdom/render
                             [emotion-components]
                             (js/document.getElementById "emotion-container")
                             compiler)
     :reagent-render
     (fn []
       [:div#emotion-container])}))



(defn ^:export run []
  (rdom/render [simple-example] (js/document.getElementById "app")))
