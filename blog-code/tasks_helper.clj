(ns tasks-helper
  (:require
   [babashka.fs :as fs]
   [babashka.process :as p]
   [clojure.string :as str]
   [selmer.parser :as selmer]))

(defn parse-opts
  ([opts] (parse-opts opts nil))
  ([opts opts-def]
   (let [[cmds opts] (split-with #(not (str/starts-with? % ":")) opts)]
     (reduce
      (fn [opts [arg-name arg-val]]
        (let [k  (keyword (subs arg-name 1))
              od (k opts-def)
              v  ((or (:parse-fn od) identity) arg-val)]
          (if-let [c (:collect-fn od)]
            (update opts k c v)
            (assoc opts k v))))
      {:cmds cmds}
      (partition 2 opts)))))

(def opts (parse-opts *command-line-args*))

(def post-template
  (str/triml "
{:title {{title | safe }}
 :file {{file | safe }}
 :categories {{categories | safe }}
 :date {{date | safe }}}\n"))

(defn now []
  (pr-str
   (.format (java.time.LocalDate/now)
            (java.time.format.DateTimeFormatter/ofPattern "yyyy-MM-dd"))))

(defn new []
  (let [{:keys [file title categories]} opts]
    (assert file "Must provide a path-less file name with :file <file.md>")
    (assert title "Must provide a title with :title <title>")
    (let [post-file (fs/file "posts" file)]
      (when-not (fs/exists? post-file)
        (spit (fs/file "posts" file) "TODO: write blog post")
        (spit (fs/file "posts.edn")
              (selmer/render post-template
                             {:title      (pr-str title)
                              :file       (pr-str file)
                              :date       (now)
                              :categories (or (some-> categories set)
                                              #{"clojure"})
                              :preview    true})
              :append true)
        (p/process [(System/getenv "EDITOR") (str (fs/file "posts" file))])))))

(defn publish []
  @(p/process ["git" "add" "-A"] {:dir "public" :inherit true})
  @(p/process ["git" "commit" "-m" "Site update"] {:dir "public" :inherit true})
  @(p/process ["git" "push"] {:dir "public" :inherit true}))

(defn clean []
  (fs/delete-tree ".work"))
