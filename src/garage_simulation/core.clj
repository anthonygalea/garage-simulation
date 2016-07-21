(ns garage-simulation.core)

(def garage-levels {0 15
                    1 10})

(def empty-parking-spaces (ref #{}))

(def vehicles (ref {}))

(defn initialize-garage
  "Sets the initial state of the garage."
  [levels]
  (dosync
   (ref-set vehicles {})
   (ref-set empty-parking-spaces
            (into #{}
                  (mapcat (fn [level]
                            (map #(vector (key level) %) (range (val level))))
                          levels)))))

(defn locate-vehicle
  "Given a licence plate, returns the location of a vehicle as a vector with the
   level and parking space number, nil if not present."
  [licence-plate]
  (@vehicles licence-plate))

(defn number-of-free-parking-spaces
  "Returns the current number of free parking spaces in the garage."
  []
   (count @empty-parking-spaces))

(defn exit-garage
  "Simulates a vehicle exiting the garage. Returns the state of the garage if
   such a vehicle exists in the garage, nil otherwise."
  [licence-plate]
  (dosync
   (if-let [vehicle-location (locate-vehicle licence-plate)]
     (do
       (alter empty-parking-spaces conj vehicle-location)
       (alter vehicles dissoc licence-plate)))))

(defn enter-garage
  "Simulates a vehicle entering the garage. Return the state of the garage if
   there is still free space, nil if no empty parking space exists."
  [licence-plate]
  (dosync
   (if-let [parking-space (first @empty-parking-spaces)]
     (do
       (alter empty-parking-spaces disj parking-space)
       (alter vehicles assoc licence-plate parking-space)))))
