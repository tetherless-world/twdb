import urllib
from typing import Optional
from urllib.error import HTTPError
from urllib.parse import quote

from tw_nanopub.nanopublication import Nanopublication


class TwdbClient:
    def __init__(self, *, base_url="http://localhost:8080"):
        """
        Construct a TWDB client.
        :param base_url: base URL of the server, excluding path
        """
        self.__base_url = base_url + "/nanopublication"

    def get_nanopublication(self, nanopublication_uri: str) -> Optional[Nanopublication]:
        """
        Get a nanopublication by its URI.
        :param nanopublication_uri: nanopublication URI
        :return: the nanopublication if present, else None
        """

        request = urllib.request.Request(url=self.__nanopublication_url(nanopublication_uri),
                                         headers={"Accept": "text/trig"})

        try:
            with urllib.request.urlopen(request) as f:
                response_trig = f.read()
                return Nanopublication.parse(format="trig",
                                             data=response_trig)
        except HTTPError as e:
            if e.code == 404:
                return None
            else:
                raise

    def __nanopublication_url(self, nanopublication_uri: str) -> str:
        return self.__base_url + "/" + quote(str(nanopublication_uri), safe="")

    def put_nanopublication(self, nanopublication: Nanopublication) -> None:
        """
        Put a nanopublication.

        :param nanopublication: the nanopublication
        """

        request = urllib.request.Request(url=self.__base_url,
                                         data=nanopublication.serialize(format="trig").encode("utf-8"),
                                         headers={"Content-Type": "text/trig; charset=utf-8"}, method="PUT")
        with urllib.request.urlopen(request) as _:
            pass
