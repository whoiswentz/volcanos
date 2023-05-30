(ns volcanos.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def csv-file
  (-> "volcanos.csv"
      io/resource
      io/reader))

(defn slash->set [s]
  (set (map str/trim (str/split s #"/"))))

(defn string->integer [s]
  (Integer/parseInt s))

(defn string->double [s]
  (Double/parseDouble s))

(defn parse-eruption-date [date]
  (if (= "Unknown" date)
    nil
    (let [[_ y e] (re-matches #"(\d+) (.+)" date)]
      (cond
        (= e "BCE")
        (- (Integer/parseInt y))
        (= e "CE")
        (Integer/parseInt y)
        :else
        (throw (ex-info "Could not parse year." {:year date}))))))

(defn parse-numbers [volcano]
  (-> volcano
      (update :elevation-meter string->integer)
      (update :longitude string->double)
      (update :latitude string->double )
      (assoc :last-known-parsed (parse-eruption-date (:last-known-eruption volcano)))
      (update :tectonic-setting slash->set)
      (update :dominant-rock-type slash->set)))


(def csv-lines
  (with-open [csv csv-file]
    (doall
      (csv/read-csv csv))))

(defn transform-header [header]
  (if (= header "Elevation (m)")
    :elevation-meter
    (-> header
        clojure.string/lower-case
        (clojure.string/replace #" " "-")
        keyword)))

(defn transform-header-row [header-lines]
  (map transform-header header-lines))

(def volcanos-records
  (let [csv-lines (rest csv-lines)
        header-lines (transform-header-row (first csv-lines))
        volcano-datas (rest csv-lines)]
    (map (fn [volcano-data]
           (zipmap header-lines volcano-data))
         volcano-datas)))

(def volcano-parsed
  (map parse-numbers volcanos-records))

