(ns bekind.handler
  (:require
    [bekind.db :as db]
    [clojure.pprint :as pprint]
    [reitit.ring :as ring]
    [reitit.coercion.spec]
    [reitit.swagger :as swagger]
    [reitit.swagger-ui :as swagger-ui]
    [reitit.ring.coercion :as coercion]
    [reitit.dev.pretty :as pretty]
    [reitit.ring.middleware.muuntaja :as muuntaja]
    [reitit.ring.middleware.exception :as exception]
    [reitit.ring.middleware.multipart :as multipart]
    [reitit.ring.middleware.parameters :as parameters]
    [muuntaja.core :as m]))

(defn plus [x y]
  (- x y))

;; In Memory

(defonce db (atom []))

(defn add-item [item]
  (swap! db conj item))

(defn vec-remove [v idx]
  (into
    (subvec v 0 idx)
    (subvec v (inc idx))))

(defn remove-item [item]
  (let [idx (.indexOf @db item)]
    (when (>= idx 0)
      (swap! db vec-remove idx))))

(defn list-items []
  @db)

;; ----

(defn cart [body]
  (pprint/pprint body)
  (let [{{:keys [action parameters]} :result} body
        {:keys [item]} parameters]
    (condp = action
      "item.add" (db/add-item item)
      "item.remove" (db/remove-item item)
      "item.list" ())
    {"action" action
     "item" item
     "list" (db/list-items)}))

(def routes
  [["/swagger.json"
    {:get {:no-doc  true
           :swagger {:info {:title       "my-api"
                            :description "with reitit-ring"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/cart"
    {:post {:summary    "this is the cart"
            :parameters {:body any?}
            :responses  {200 {:body {:result any?}}}
            :handler    (fn [{{in-body :body} :parameters}]
                          {:status 200
                           :body   {:result (cart in-body)}})}}]

   ["/math"
    {:swagger {:tags ["math"]}}

    ["/plus"
     {:get  {:summary    "plus with spec query parameters"
             :parameters {:query {:x int?
                                  :y int?}}
             :responses  {200 {:body {:total int?}}}
             :handler    (fn [{{{:keys [x y]} :query} :parameters}]
                           {:status 200
                            :body   {:total (plus x y)}})}
      :post {:summary    "plus with spec body parameters"
             :parameters {:body {:x int?
                                 :y int?}}
             :responses  {200 {:body {:total int?}}}
             :handler    (fn [{{{:keys [x y]} :body} :parameters}]
                           {:status 200
                            :body   {:total (plus x y)}})}}]]])

(def app
  (ring/ring-handler
    (ring/router
      routes
      {:exception pretty/exception
       :data      {:coercion   reitit.coercion.spec/coercion
                   :muuntaja   m/instance
                   :middleware [swagger/swagger-feature
                                parameters/parameters-middleware
                                muuntaja/format-negotiate-middleware
                                muuntaja/format-response-middleware
                                (exception/create-exception-middleware
                                  {::exception/default (partial exception/wrap-log-to-console exception/default-handler)})
                                muuntaja/format-request-middleware
                                coercion/coerce-response-middleware
                                coercion/coerce-request-middleware
                                multipart/multipart-middleware]}})

    (ring/routes
      (swagger-ui/create-swagger-ui-handler
        {:path   "/"
         :config {:validatorUrl     nil
                  :operationsSorter "alpha"}})
      (ring/create-default-handler))))
