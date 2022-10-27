(ns bekind.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn create-db
  "create db and table"
  []
  (try (do
         ;; Migrations for data schema may be required
         #_(jdbc/db-do-commands db (jdbc/drop-table-ddl :items))
         (jdbc/db-do-commands
             db
             (jdbc/create-table-ddl
               :items
               ;; Use an id instead of a timestamp!
               [[:timestamp :datetime :default :current_timestamp]
                [:item :text]])))
       (catch Exception e
         (println (.getMessage e)))))

(defn add-item [item]
  (jdbc/insert! db :items {:item item}))

(defn remove-item [item]
  (jdbc/delete! db :items ["item=? AND timestamp in (SELECT timestamp FROM items WHERE item=? order by timestamp LIMIT 1)" item item]))

(defn list-items []
  (jdbc/query db ["select * from items order by timestamp"]))
