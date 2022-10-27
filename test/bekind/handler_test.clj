(ns bekind.handler-test
  (:require
    [clojure.test :refer [deftest is]]
    [bekind.handler :as h]))

(deftest app-test
  (is (= 1 (h/app {:foo :bar}))))
