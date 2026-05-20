import logging
from typing import Any

import httpx

from app.config import settings

logger = logging.getLogger(__name__)

BASE_URL = settings.MARICOPA_API_BASE_URL


def _headers() -> dict[str, str]:
    headers: dict[str, str] = {"User-Agent": ""}
    if settings.MARICOPA_API_TOKEN:
        headers["AUTHORIZATION"] = settings.MARICOPA_API_TOKEN
    return headers


async def search_property(query: str, page: int = 1) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/search/property/"
        params: dict[str, str | int] = {"q": query}
        if page > 1:
            params["page"] = page
        try:
            resp = await client.get(url, headers=_headers(), params=params)
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Maricopa search failed: %s", exc)
            return {"error": str(exc), "results": [], "total": 0}


async def get_parcel_details(apn: str) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/parcel/{apn}"
        try:
            resp = await client.get(url, headers=_headers())
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Parcel details fetch failed for %s: %s", apn, exc)
            return {"error": str(exc)}


async def get_property_info(apn: str) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/parcel/{apn}/propertyinfo"
        try:
            resp = await client.get(url, headers=_headers())
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Property info fetch failed for %s: %s", apn, exc)
            return {"error": str(exc)}


async def get_property_address(apn: str) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/parcel/{apn}/address"
        try:
            resp = await client.get(url, headers=_headers())
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Address fetch failed for %s: %s", apn, exc)
            return {"error": str(exc)}


async def get_valuations(apn: str) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/parcel/{apn}/valuations"
        try:
            resp = await client.get(url, headers=_headers())
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Valuations fetch failed for %s: %s", apn, exc)
            return {"error": str(exc)}


async def get_owner_details(apn: str) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/parcel/{apn}/owner-details"
        try:
            resp = await client.get(url, headers=_headers())
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Owner details fetch failed for %s: %s", apn, exc)
            return {"error": str(exc)}


async def get_residential_details(apn: str) -> dict[str, Any]:
    async with httpx.AsyncClient(timeout=30) as client:
        url = f"{BASE_URL}/parcel/{apn}/residential-details"
        try:
            resp = await client.get(url, headers=_headers())
            resp.raise_for_status()
            return resp.json()
        except httpx.HTTPError as exc:
            logger.error("Residential details fetch failed for %s: %s", apn, exc)
            return {"error": str(exc)}


async def fetch_full_property_data(apn: str) -> dict[str, Any]:
    parcel = await get_parcel_details(apn)
    property_info = await get_property_info(apn)
    address = await get_property_address(apn)
    valuations = await get_valuations(apn)
    owner = await get_owner_details(apn)

    return {
        "parcel": parcel,
        "property_info": property_info,
        "address": address,
        "valuations": valuations,
        "owner": owner,
    }
