(ns garage-simulation.core
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as g]
            [clojure.spec.test :as t]
            [com.gfredericks.test.chuck.generators :as cg]))

(s/def ::number-of-parking-spaces (s/and int? pos?))

(s/def ::parking-spaces (s/coll-of ::number-of-parking-spaces :kind vector?))

(def parking-spaces [5 2])

(def empty-parking-spaces (ref #{}))

(def vehicles (ref {}))

(s/fdef initialize-garage!
        :args (s/cat :parking-spaces ::parking-spaces))

(defn initialize-garage!
  "Sets the initial state of the garage."
  [parking-spaces]
  (dosync
   (ref-set vehicles {})
   (ref-set empty-parking-spaces
            (into #{}
                  (for [[level spaces] (map-indexed vector parking-spaces)
                        space-number (range spaces)]
                    [level space-number])))))

(def licence-plate-regex #"[A-Z]{4}\d{3}")

(s/def ::licence-plate
  (s/with-gen
    (s/and string? #(re-matches licence-plate-regex %))
    #(cg/string-from-regex licence-plate-regex)))

(s/def ::parking-space (s/coll-of (s/and int? (s/or :zero zero? :positive pos?)) :kind vector? :count 2))

(s/fdef locate-vehicle
        :args (s/cat :licence-plate ::licence-plate)
        :ret (s/or ::parking-space nil?))

(defn locate-vehicle
  "Given a licence plate, returns the location of a vehicle as a vector with the
   level and parking space number, nil if not present."
  [licence-plate]
  (@vehicles licence-plate))

(s/fdef number-of-free-parking-spaces
        :ret (s/and int? (s/or :zero zero? :positive pos?)))

(defn number-of-free-parking-spaces
  "Returns the current number of free parking spaces in the garage."
  []
  (count @empty-parking-spaces))

(s/def ::vehicles (s/nilable (s/map-of ::licence-plate ::parking-space)))

(s/fdef exit-garage!
        :args (s/cat :licence-plate ::licence-plate)
        :ret ::vehicles
        :fn #(not (contains? (:ret %) (-> % :args :licence-plate))))

(defn exit-garage!
  "Simulates a vehicle exiting the garage. Returns the state of the garage if
   such a vehicle exists in the garage, nil otherwise."
  [licence-plate]
  (dosync
   (if-let [vehicle-location (locate-vehicle licence-plate)]
     (do
       (alter empty-parking-spaces conj vehicle-location)
       (alter vehicles dissoc licence-plate)))))

(s/fdef enter-garage!
        :args (s/cat :licence-plate ::licence-plate)
        :ret ::vehicles
        :fn #(or (nil? (:ret %))
                 contains? (:ret %) (-> % :args :licence-plate)))

(defn enter-garage!
  "Simulates a vehicle entering the garage. Return the state of the garage if
   there is still free space, nil if no empty parking space exists."
  [licence-plate]
  (dosync
   (if-let [parking-space (first @empty-parking-spaces)]
     (do
       (alter empty-parking-spaces disj parking-space)
       (alter vehicles assoc licence-plate parking-space)))))
