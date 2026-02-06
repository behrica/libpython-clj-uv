(ns libpython-clj-uv.sync
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.java.process :as process]
   [clojure.string :as str])
  (:import
   [com.pty4j PtyProcessBuilder]))

(defn- write-pyproject-toml! [python-edn]

  (let [python-deps
        (:python-deps python-edn)

        python-version (:python-version python-edn)

        py-project-header-lines ["# auto-generated, do not modify by hand"
                                 "[project]"
                                 "name = \"temp\""
                                 "version = \"0.0\""
                                 (format "requires-python = \"==%s\"" python-version)]
        python-deps-lines
        (map
         (fn [dep]
           (format "\"%s\"," dep))
         python-deps)

        py-project-lines
        (concat
         py-project-header-lines
         ["dependencies = ["]
         python-deps-lines
         "]\n")]

    (spit "pyproject.toml"
          (str/join "\n" py-project-lines))))




(defn- uv-installed? []
  (try
    (let [p (process/start  "uv" "--version")]
      ;; We only need to know it starts; stop it quickly.
      (.destroy ^Process p)
      true)
    (catch java.io.IOException _
      false)
    (catch Throwable _
      false)))

(defn- assert-uv-available []
  (when-not (uv-installed?)
    
    (println "The 'uv' tool was not found on your PATH.")
    (println "Install uv from https://github.com/astral-sh/uv, e.g.:")
    (println "  - curl -LsSf https://astral.sh/uv/install.sh | sh")
    (println "  - or: pipx install uv")
    (throw (ex-info "The 'uv' tool is not installed or not on PATH." {:tool "uv" :stage :preflight}))))

(defn- slurp-python-edn []
  (try
          (-> (slurp "python.edn") edn/read-string)
          (catch java.io.FileNotFoundException e
            (throw (ex-info "Missing python.edn. Create python.edn and set :python-version and :python-deps."
                            {:file "python.edn" :stage :read-config} e)))))



(defn sync-python-setup!
  "Synchronize python venv at .venv with 'uv sync'.
  When 'uv' is not available on PATH, throws with guidance to install."
  []
  (println "Synchronize python venv at .venv with 'uv sync'. This might take a few minutes")
  (assert-uv-available)
  (let [python-edn
        (slurp-python-edn)
        _ (write-pyproject-toml! python-edn)
        process (..
                 (PtyProcessBuilder.)
                 (setCommand (into-array String ["uv" "sync"]))
                 start)]

    (while (.isAlive process)
      (io/copy (.getInputStream process) System/out)
      (io/copy (.getErrorStream process) System/err))


    (println "Python environment synchronized with uv.")
    true))
