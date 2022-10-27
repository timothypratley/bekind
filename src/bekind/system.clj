(ns bekind.system
  (:require
    [bekind.handler :as h]
    [ring.adapter.jetty :as jetty]))

(defn start []
  (jetty/run-jetty #'h/app {:port 3000, :join? false})
  (println "server running on port 3000"))
