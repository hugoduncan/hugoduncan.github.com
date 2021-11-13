(ns convert
  "Convert old blog posts"
  (:require
   [babashka.fs :as fs]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clj-yaml.core :as yaml]))

(def posts-edn (io/file "posts.edn"))
(def posts-dir (io/file "posts"))
(def src-dir (io/file "src/content/post"))

(defmulti convert-meta-value
  (fn [[k _v]] k))

(defmethod convert-meta-value :title
  [[k v]]
  [k v])

(defmethod convert-meta-value :pubdate
  [[k v]]
  [:date v])

(defmethod convert-meta-value :date
  [[k v]]
  [:date v])

(defmethod convert-meta-value :description
  [[k v]]
  [:description v])

(defmethod convert-meta-value :tags
  [[k v]]
  [:categories (set v)])

(defmethod convert-meta-value :aliases
  [[k v]]
  [:aliases (set v)])

(defmethod convert-meta-value :draft
  [[k v]]
  [:draft v])

(defn convert-post [orig-post-path posts-edn posts-dir]
  (let [file-name     (fs/file-name orig-post-path)
        new-post-file (fs/file posts-dir file-name)
        post          (slurp orig-post-path)
        meta-lines    (->> post
                           str/split-lines
                           (drop 1)
                           (take-while (fn [x] (not= "---" x))))
        content-lines (->> post
                           str/split-lines
                           (drop (+ 2 (count meta-lines))))

        meta-kv (yaml/parse-string (str/join \newline meta-lines))
        ;; meta-kv       (mapv #(str/split % #":" 2) meta-lines)
        ;; meta-kv       (into {} meta-kv)
        meta-kv (zipmap
                 (map (comp keyword str/lower-case name) (keys meta-kv))
                 (vals meta-kv))
        _       (prn :meta meta-kv)
        meta-kv (into {} (map convert-meta-value meta-kv))
        meta-kv (assoc meta-kv
                       :file file-name
                       :legacy true)]
    (spit new-post-file (str/join \newline content-lines)
          :encoding "UTF-8")
    (spit posts-edn (prn-str meta-kv)
          :append   true
          :encoding "UTF-8")))


(defn convert-all
  []
  (doseq [file (file-seq src-dir)]
    (when (fs/regular-file? file)
      (try
        (prn (str file))
        (convert-post file posts-edn posts-dir)
        (catch Throwable e
          (println "Error converting" (str file))
          (throw e))))))

(convert-all)
