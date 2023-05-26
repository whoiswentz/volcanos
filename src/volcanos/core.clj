(ns volcanos.core
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def csv-file
  (-> "volcanos.csv"
      io/resource
      io/reader))

(defn parse-numbers [volcano]
  (-> volcano
      (update :elevation-meter #(Integer/parseInt %))
      (update :longitude #(Double/parseDouble %))
      (update :latitude #(Double/parseDouble %))))

(defn parse-date [volcanos]
  true)

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

