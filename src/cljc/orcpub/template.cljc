(ns orcpub.template
  (:require [clojure.spec :as spec]
            [orcpub.modifiers :as modifiers]))

(spec/def ::name string?)
(spec/def ::key keyword?)
(spec/def ::min (spec/int-in 0 10))
(spec/def ::max (spec/int-in 1 10))
(spec/def ::attribute (spec/keys :req [::name ::key]))
(spec/def ::attributes (spec/+ ::attribute))
(spec/def ::derived-value (spec/or :func (spec/fspec :args (spec/cat :entity map?))
                                   :keyword keyword?))
(spec/def ::derived-attribute (spec/keys :req [::name ::key ::derived-value]))
(spec/def ::derived-attributes (spec/+ ::derived-attribute))
(spec/def ::modifiers (spec/+ ::modifiers/modifier))
(spec/def ::option (spec/keys :req [::name ::key]
                              :opt [::modifiers ::selections]))
(spec/def ::options (spec/+ ::option))
(spec/def ::selection (spec/keys :req [::name ::key ::options]
                                 :opt [::min ::max]))
(spec/def ::selections (spec/+ ::selection))
(spec/def ::template (spec/keys :opt [::attributes ::derived-attributes ::selections]))

(spec/def ::modifier-map-value (spec/or :modifiers ::modifiers
                                        :modifier-map ::modifier-map))
(spec/def ::modifier-map-entry (spec/tuple keyword? ::modifier-map-value))
(spec/def ::modifier-map (spec/map-of keyword? ::modifier-map-value))

(declare make-modifier-map-from-selections)

(defn make-modifier-map-entry-from-option [option]
  [(::key option)
   (let [modifiers (select-keys option [::modifiers])
         selections (::selections option)]
     (if selections
       (merge (make-modifier-map-from-selections (::selections option)) modifiers)
       modifiers))])
(spec/fdef
 make-modifier-map-entry-from-option
 :args ::option
 :ret ::modifier-map-entry)

(defn make-modifier-map-entry-from-selection [selection]
  [(::key selection)
   (into {} (map make-modifier-map-entry-from-option (::options selection)))])
(spec/fdef
 make-modifier-map-entry-from-selection
 :args ::selection
 :ret ::modifier-map-entry)

(defn make-modifier-map-from-selections [selections]
  (into {} (map make-modifier-map-entry-from-selection selections)))
(spec/fdef
 make-modifier-map-entry-from-selections
 :args ::selections
 :ret ::modifier-map)

(defn make-modifier-map [template]
  (prn template)
  (make-modifier-map-from-selections (::selections template)))
(spec/fdef
 make-modifier-map
 :args ::template
 :ret ::modifier-map)

(spec/fdef make-modifier-map
           :args (spec/cat :template ::template)
           :ret ::modifier-map)