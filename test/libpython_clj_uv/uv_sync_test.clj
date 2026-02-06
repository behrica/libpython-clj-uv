(ns libpython-clj-uv.uv-sync-test
  (:require [libpython-clj-uv.sync :as uv-sync]
            [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.test :refer [deftest is use-fixtures]]))

(defn delete-directory-recursive
  "Recursively delete a directory."
  [^java.io.File file]
  ;; when `file` is a directory, list its entries and call this
  ;; function with each entry. can't `recur` here as it's not a tail
  ;; position, sadly. could cause a stack overflow for many entries?
  ;; thanks to @nikolavojicic for the idea to use `run!` instead of
  ;; `doseq` :)
  (when (.isDirectory file)
    (run! delete-directory-recursive (.listFiles file)))
  ;; delete the file or directory. if it it's a file, it's easily
  ;; deletable. if it's a directory, we already have deleted all its
  ;; contents with the code above (remember?)
  (io/delete-file file))

(defn python-end-fixture [f]
  (spit
   "python.edn"
   {:python-version "3.14.0"
    :python-deps ["polars==1.37.1"]
    :python-executable ".venv/bin/python"
    :pre-initialize-fn 'libpython-clj-uv.sync/sync-python-setup!})
  (f)
  (clojure.java.io/delete-file "python.edn")
  (clojure.java.io/delete-file "pyproject.toml")
  (clojure.java.io/delete-file "uv.lock")
  (delete-directory-recursive (io/file ".venv"))
  
  )

(deftest sync-test
  (python-end-fixture
   (fn []
     (libpython-clj-uv.sync/sync-python-setup!)
     (is (.exists (io/file "pyproject.toml")))
     (is (.exists (io/file ".venv"))))))