(ns render
  (:require
   [babashka.fs :as fs]
   [babashka.pods :as pods]
   [clojure.data.xml :as xml]
   [clojure.edn :as edn]
   [clojure.string :as str]
   [highlighter :as h]
   [selmer.parser :as selmer]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.markdown :as md])
(require '[pod.retrogradeorbit.bootleg.utils :as utils])

;;;; Structure
(defrecord Structure
    [out-dir
     template-dir
     src-assets-dir
     src-posts-dir
     src-meta-dir
     asset-dir
     category-dir
     post-dir
     base-html
     post-html
     meta-html])

(defn out-path [{:keys [out-dir] :as s} target-kw & args]
  (assert (get s target-kw) (str s))
  (apply fs/file out-dir (get s target-kw) args))

(defn structure [out-dir]
  (let [s (map->Structure
           {:out-dir       "public"
            :template-dir  (fs/file "templates")
            :src-posts-dir (fs/file "posts")
            :src-asset-dir (fs/file "assets")
            :src-meta-dir  (fs/file "meta")
            :asset-dir     (fs/file "assets")
            :category-dir  (fs/file "tags")
            :post-dir      (fs/file "post")
            :meta-dir      (fs/file "meta")
            :base-html     (slurp "templates/base.html")
            :meta-html     (slurp "templates/meta.html")
            :post-html     (slurp "templates/post.html")
            :redirect-html (slurp "templates/redirect.html")})]
    (fs/create-dirs (out-path s :asset-dir))
    (fs/create-dirs (out-path s :meta-dir))
    (fs/create-dirs (out-path s :post-dir))
    s))

(defn html-file [file]
  (str/replace file ".md" ".html"))

(defn base-filename [file]
  (str/replace file ".md" ""))

;;;; Images and CSS

(defn copy-assets [structure]
  (fs/copy-tree
   (:src-asset-dir structure)
   (out-path structure :asset-dir)
   {:replace-existing true}))

(defn copy-stylesheet [structure]
  (->> (slurp (fs/file (:template-dir structure) "style.css"))
       (spit (fs/file (:out-dir structure) "style.css") )))

;;;; Generate posts from markdown

(defn markdown->html [file]
  (println "Processing markdown for file:" (str file))
  (let [markdown (slurp file)
        markdown (h/highlight-clojure markdown)
        ;; make links without markup clickable
        markdown (str/replace markdown #"http[A-Za-z0-9/:.=#?_-]+([\s])"
                              (fn [[match ws]]
                                (format "[%s](%s)%s"
                                        (str/trim match)
                                        (str/trim match)
                                        ws)))
        ;; allow links with markup over multiple lines
        markdown (str/replace markdown #"\[[^\]]+\n"
                              (fn [match]
                                (str/replace match "\n" "$$RET$$")))
        html     (md/markdown markdown :data :html :reference-links? true)
        html     (str/replace html "$$RET$$" "\n")]
    html))

(def discuss-fallback
  "https://github.com/hugoduncan/hugoduncan.github.com/discussions/categories/posts")

(defn render-post
  [{:keys [base-html
           post-html
           redirect-html
           post-dir
           src-posts-dir]
    :as   structure}
   bodies
   {:keys [description file title date legacy discuss]
    :or   {discuss discuss-fallback}}]
  (let [cache-file    (fs/file ".work" (html-file file))
        markdown-file (fs/file src-posts-dir file)
        stale?        (seq (fs/modified-since
                            cache-file
                            [markdown-file
                             "posts.edn"
                             "templates"
                             "templates/base-html"
                             "templates/post-html"
                             "templates/redirect-html"
                             "blog-code/render.clj"
                             "blog-code/highlighter.clj"]))
        body          (if stale?
                        (let [body (markdown->html markdown-file)]
                          (spit cache-file body)
                          body)
                        (slurp cache-file))
        _             (swap! bodies assoc file body)
        body          (selmer/render
                       post-html
                       {:body    body
                        :title   title
                        :date    date
                        :discuss discuss})
        html          (selmer/render
                       base-html
                       {:title       title
                        :body        body
                        :description description
                        :base        "../"})
        html-file     (html-file file)
        base-filename (base-filename file)]
    (spit (out-path structure :post-dir html-file) html)
    (when legacy
      (let [legacy-dir (out-path structure :post-dir base-filename)]
        (fs/create-dirs legacy-dir)
        (let [redirect-html (selmer/render
                             redirect-html
                             {:new_url (str (fs/path post-dir html-file))})]
          (spit (out-path structure :post-dir base-filename "index.html")
                redirect-html))))))

(defn render-posts
  [structure posts bodies]
  (doseq [post posts]
    (render-post structure bodies post)))

(defn render-meta
  [{:keys [base-html
           meta-html
           src-meta-dir]
    :as   structure}
   bodies
   {:keys [description file title]}]
  (let [cache-file    (fs/file ".work" (html-file file))
        markdown-file (fs/file src-meta-dir file)
        stale?        (seq (fs/modified-since
                            cache-file
                            [markdown-file
                             "posts.edn"
                             "templates"
                             "templates/base-html"
                             "templates/meta-html"
                             "templates/redirect-html"
                             "blog-code/render.clj"
                             "blog-code/highlighter.clj"]))
        body          (if stale?
                        (let [body (markdown->html markdown-file)]
                          (spit cache-file body)
                          body)
                        (slurp cache-file))
        _             (swap! bodies assoc file body)
        body          (selmer/render
                       meta-html
                       {:body  body
                        :title title})
        html          (selmer/render
                       base-html
                       {:title        title
                        :body         body
                        :base         "../"
                        :description  description
                        :skip-archive true})
        html-file     (html-file file)]
    (spit (out-path structure :meta-dir html-file) html)))

(defn- render-metas
  [structure metas bodies]
  (doseq [meta metas]
    (render-meta structure bodies meta)))

;;;; Generate archive page

(defn post-links [{:keys [post-dir]} posts]
  [:div {:style "width: 600px;"}
   [:h1 "Archive"]
   [:ul.index
    (for [{:keys [file title date preview]} posts
          :when                             (not preview)]
      [:li [:span
            [:a
             {:href (str (fs/path post-dir (html-file file)))}
             title]
            " - " date]])]])

;;;; Generate index page with last 3 posts

(defn index [{:keys [post-dir]} posts bodies]
  (for [{:keys [file title date preview discuss]
         :or   {discuss discuss-fallback}} (take 3 posts)
        :when                              (not preview)]
    [:div
     [:h1 [:a {:href (str (fs/path post-dir (html-file file)))}
           title]]
     (get @bodies file)
     [:p "Discuss this post " [:a {:href discuss} "here"] "."]
     [:p [:i "Published: " date]]]))

;;;; Generate atom feeds

(import java.time.format.DateTimeFormatter)

(defn rfc-3339-now []
  (let [fmt (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ssxxx")
        now (java.time.ZonedDateTime/now java.time.ZoneOffset/UTC)]
    (.format now fmt)))

(defn rfc-3339 [yyyy-MM-dd]
  (let [in-fmt     (DateTimeFormatter/ofPattern "yyyy-MM-dd")
        local-date (java.time.LocalDate/parse yyyy-MM-dd in-fmt)
        fmt        (DateTimeFormatter/ofPattern "yyyy-MM-dd'T'HH:mm:ssxxx")
        now        (java.time.ZonedDateTime/of (.atTime local-date 23 59 59) java.time.ZoneOffset/UTC)]
    (.format now fmt)))

(xml/alias-uri 'atom "http://www.w3.org/2005/Atom")

(defn atom-feed
  ;; validate at https://validator.w3.org/feed/check.cgi
  [blog-root posts bodies]
  (-> (xml/sexp-as-element
       [::atom/feed
        {:xmlns "http://www.w3.org/2005/Atom"}
        [::atom/title "Pelure"]
        [::atom/link {:href (str blog-root "index.xml") :rel "self"}]
        [::atom/link {:href blog-root}]
        [::atom/updated (rfc-3339-now)]
        [::atom/id blog-root]
        [::atom/author
         [::atom/name "Hugo Duncan"]]
        (for [{:keys [date
                      description
                      file
                      preview
                      title]} posts
              :when           (not preview)
              :let            [html (html-file file)
                               link (str blog-root "post/" html)]]
          [::atom/entry
           [::atom/id link]
           [::atom/link {:href link}]
           [::atom/title title]
           (when description
             [::atom/summary description])
           [::atom/updated (rfc-3339 date)]
           [::atom/content {:type "html"}
            [:-cdata (get @bodies file)]]])])
      xml/indent-str))

(def blog-root "http://hugoduncan.org/")

(defn posts []
  (->> "posts.edn"
       slurp
       (format "[%s]")
       edn/read-string
       (sort-by :date (comp - compare))
       vec))

(defn posts-with-categories
  [posts categories]
  (filterv
   (fn [post]
     (some (:categories post) categories))
   posts))

(defn metas []
  (->> "metas.edn"
       slurp
       (format "[%s]")
       edn/read-string
       vec))

(defn categories [posts]
  (reduce
   (fn [res {:keys [categories] :as post}]
     (reduce
      (fn [res category]
        (update res category (fnil conj []) post))
      res
      categories))
   {}
   posts))

(defn render-archive
  [{:keys [out-dir base-html] :as structure} posts]
  (println "Rendering archive to :" (str (fs/file out-dir "archive.html")))
  (spit (fs/file out-dir "archive.html")
        (selmer/render
         base-html
         {:skip-archive true
          :body         (utils/convert-to (post-links structure posts) :html)
          :base         ""})))

(defn render-index [{:keys [out-dir base-html] :as structure} posts bodies]
  (println "Rendering index to :" (str (fs/file out-dir "index.html")))
  (spit (fs/file out-dir "index.html")
        (selmer/render
         base-html
         {:body (utils/convert-to (index structure posts bodies) :html)
          :base ""})))

(defn render-atom [{:keys [out-dir]} feed-name posts bodies]
  (println "Rendering atom to :" feed-name)
  (spit (fs/file out-dir feed-name) (atom-feed blog-root posts bodies)))


(defn category-name->path [category]
  (str/replace category " " "-"))

(defn render-category-page [{:keys [base-html] :as structure} category bodies]
  (let [file (out-path structure :category-dir
                       (category-name->path (key category))
                       "index.html")]
    (spit file
          (selmer/render
           base-html
           {:body  (utils/convert-to
                    (index structure (val category) bodies)
                    :html)
            :title (key category)
            :base  "../../"}))))

(defn render-category-feed [structure category bodies]
  (render-atom
   structure
   (fs/path (:category-dir structure)
            (category-name->path (key category))
            "index.xml")
   (val category)
   bodies))

(defn render-categories [structure categories bodies]
  (doseq [category categories]
    (render-category-page structure category bodies)
    (render-category-feed structure category bodies)))

(defn render []
  (let [structure    (structure "public")
        bodies       (atom {}) ; cache, re-used when generating atom, etc
        posts        (posts)
        public-posts (vec (remove :draft posts))
        categories   (categories public-posts)
        metas        (metas)
        clj-posts    (posts-with-categories posts ["clojure" "clojurescript"])]

    (fs/create-dirs (fs/file ".work"))
    (copy-assets structure)
    (copy-stylesheet structure)
    (render-posts structure posts bodies)
    (render-metas structure metas bodies)
    (render-index structure public-posts bodies)
    (render-archive structure public-posts)
    (render-categories structure categories bodies)
    (render-atom structure "index.xml" public-posts bodies)
    (render-atom structure "planetclojure.xml" clj-posts bodies)))

(render)
