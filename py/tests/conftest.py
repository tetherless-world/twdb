import os.path

import pytest

from twks.nanopub.nanopublication import Nanopublication


@pytest.fixture
def assertions_ttl_file_path():
    return os.path.abspath(os.path.join(os.path.dirname(__file__), "assertions.ttl"))


@pytest.fixture
def spec_nanopublication_trig_file_path():
    return os.path.abspath(os.path.join(os.path.dirname(__file__), "spec_nanopublication.trig"))


@pytest.fixture
def spec_nanopublication(spec_nanopublication_trig_file_path):
    return Nanopublication.parse(source=spec_nanopublication_trig_file_path)
