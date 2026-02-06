# libpython-clj-uv

This library integrates closely  and `libpython-clj` and `uv`.
It provides a single function, `'libpython-clj-uv.sync/sync-python-setup!`
which can be hooked into the startup of `libpython-clj`, by providing a file

`python.edn` containing:

```edn
{
    :python-version "3.14.0"
    :python-deps ["polars==1.37.1"]
    :python-executable ".venv/bin/python" ;; in Linux
    :pre-initialize-fn 'libpython-clj-uv.sync/sync-python-setup!
}

```

This will call the function 'libpython-clj-uv.sync/sync-python-setup! on JVM start, which will use the information in `python.edn` to create a 
`pyproject.toml` file and then run `uv sync` (which itself uses then the generated `pyproject.toml` file to create/sync a python venv in folder `.venv`)

`uv` needs to be installed for this to work.

It will take the python libraries to install from key `:python-deps` (the strings have the same syntax then in `pyproject.toml`, see uv documentation) and installs the python version defined in `:python-version`.

So it basically allows to define a `pyproject.toml` using `python.edn`
The `python.edn` will be overwritten by each JVM start.

`libpython-clj` is then guarantied to use the created venv.


This guaranties that the venv is in sync and existing before  `libpython-clj` gets initialized.