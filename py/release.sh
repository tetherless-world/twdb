#!/bin/bash
rm -fr build dist *.egg-info
python3 setup.py sdist bdist_wheel
# python3 -m twine upload --repository-url https://test.pypi.org/legacy/ dist/*
python3 -m twine upload dist/*
