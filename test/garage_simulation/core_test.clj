(ns garage-simulation.core-test
  (:require [clojure.test :refer :all]
            [garage-simulation.core :refer :all]
            [midje.sweet :refer :all]))

(against-background
 [(before :facts (initialize-garage! [2 1]))]

 (fact "After a vehicle enters the garage it should be possible to locate it."
       (let [licence-plate "ASDF001"]
           (enter-garage! licence-plate)
           (locate-vehicle licence-plate)) => [0 0])

 (fact "If an attempt to locate an unknown vehicle is made nil should be
       returned."
       (locate-vehicle "unknown licence plate") => nil)

 (fact "After a vehicle enters the garage the number of free parking spaces
        should be one less than before."
       (let [free-parking-spaces-before (number-of-free-parking-spaces)]
         (enter-garage! "ASDF001")
         (number-of-free-parking-spaces) => (dec free-parking-spaces-before)))

 (fact "If too many vehicles try to enter the garage nil should be returned."
       (enter-garage! "ASDF001")
       (enter-garage! "ASDF002")
       (enter-garage! "ASDF003")
       (enter-garage! "ASDF004") => nil)

 (fact "Given a vehicle entered the garage it should be possible for that
       vehicle to exit the garage."
       (let [licence-plate "ASDF001"
             free-parking-spaces-before (number-of-free-parking-spaces)]
         (enter-garage! licence-plate)
         (exit-garage! licence-plate) => {}
         (number-of-free-parking-spaces) => free-parking-spaces-before
         (locate-vehicle licence-plate) => nil))

 (fact "If an unknown vehicle is requested to exit the garage nil should be
       returned."
       (exit-garage! "ASDF001") => nil))
