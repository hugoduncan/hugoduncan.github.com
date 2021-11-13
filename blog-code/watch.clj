(ns watch
  (:require
   [babashka.pods :as pods]
   [render]))

(pods/load-pod 'org.babashka/filewatcher "0.0.1")
(require '[pod.babashka.filewatcher :as fw])

(defn watch []
  (fw/watch "posts"
            (fn [_]
              (println "Re-rendering")
              (render/render)))

  (fw/watch "templates"
            (fn [_]
              (println "Re-rendering")
              (render/render)))

  (fw/watch "blog-code"
            (fn [_]
              (println "Re-rendering")
              (require '[render :reload])
              (render/render)))

  @(promise))
