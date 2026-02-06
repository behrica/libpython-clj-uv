# libpython-clj-uv

This library integrates closely 'uv' with libpython-clj.
It provides a single function, `'libpython-clj-uv.sync/sync-python-setup!`
which can be hooked into the startup of `libpython-clj`, by providing a file

`python.edn` containing:

```edn
{
    :python-version "3.14.0"
    :python-deps ["polars==1.37.1"]
    :python-executable ".venv/bin/python"
    :pre-initialize-fn 'libpython-clj-uv.sync/sync-python-setup!
}

```

This will call the function, which will use the information in `python.edn` to create a 
`pyproject.toml` file and the run `uv sync` (which itself uses then the genrated `pyproject.toml` file to create/sync a python venv)

`uv` need to be installed for this to work.

It will take the python libraries to install from key `:python-deps` (the strings have the same syntax the in `pyproject.tom`) and installl the python version defined in `:python-version`.

So it basically allows to define a `pyproject.toml` using `python.edn`
The `python.edn` will be overwritten by each JVM start.


This guaranties that the venv is in sync and existing before initialising `libpython-clj`