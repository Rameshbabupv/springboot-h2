#!/usr/bin/env python3
"""Lightweight GraphQL smoke test for the HRMS API."""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path
from typing import Any, Dict


DEFAULT_BASE_URL = "http://localhost:8080"
DEFAULT_QUERY = "{ employees { id empId employeeName emailId } }"
ENV_PATH = Path(__file__).resolve().parent / ".env"


def load_env_defaults(env_path: Path = ENV_PATH) -> Dict[str, str]:
    if not env_path.exists():
        return {}

    defaults: Dict[str, str] = {}
    for raw_line in env_path.read_text(encoding="utf-8").splitlines():
        line = raw_line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        value = value.strip().strip('"').strip("'")
        defaults[key.strip()] = value
    return defaults


def parse_args() -> argparse.Namespace:
    env_defaults = load_env_defaults()
    default_base = env_defaults.get("BASE_URL", DEFAULT_BASE_URL)
    default_query = env_defaults.get("GRAPHQL_QUERY", DEFAULT_QUERY)

    parser = argparse.ArgumentParser(
        description="Send a basic GraphQL query to the HRMS endpoint"
    )
    parser.add_argument(
        "--env-file",
        default=str(ENV_PATH),
        help="Optional path to a .env file with BASE_URL/GRAPHQL_QUERY overrides",
    )
    parser.add_argument(
        "--base-url",
        default=default_base,
        help="Base URL where the Spring Boot app is running (default: %(default)s)",
    )
    parser.add_argument(
        "--query",
        default=default_query,
        help="GraphQL query string to execute",
    )
    args = parser.parse_args()

    # Reload env file if user specifies a custom path
    if args.env_file and Path(args.env_file) != ENV_PATH:
        env_override = load_env_defaults(Path(args.env_file))
        if args.base_url == default_base and "BASE_URL" in env_override:
            args.base_url = env_override["BASE_URL"]
        if args.query == default_query and "GRAPHQL_QUERY" in env_override:
            args.query = env_override["GRAPHQL_QUERY"]

    return args


def run_query(base_url: str, query: str) -> Dict[str, Any]:
    endpoint = base_url.rstrip("/") + "/graphql"
    payload = json.dumps({"query": query}).encode("utf-8")
    request = urllib.request.Request(
        endpoint,
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )

    with urllib.request.urlopen(request) as response:
        body = response.read().decode("utf-8")
        return json.loads(body)


def main() -> int:
    args = parse_args()
    print(f"Sending GraphQL query to {args.base_url.rstrip('/')}/graphql ...")
    try:
        result = run_query(args.base_url, args.query)
    except urllib.error.HTTPError as exc:
        print(f"Request failed with HTTP {exc.code}: {exc.reason}")
        if exc.fp:
            print(exc.fp.read().decode("utf-8"))
        return 1
    except urllib.error.URLError as exc:
        print(f"Unable to reach endpoint: {exc.reason}")
        return 1

    if "errors" in result:
        print("GraphQL errors detected:")
        print(json.dumps(result["errors"], indent=2))
        return 2

    print("Success! Response data:")
    print(json.dumps(result.get("data"), indent=2))
    return 0


if __name__ == "__main__":
    sys.exit(main())
