#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import math
import re
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass, asdict
from pathlib import Path
from typing import Any


@dataclass
class HttpResult:
    status: int
    body: Any
    raw: str
    elapsed_ms: float


@dataclass
class TestResult:
    id: str
    name: str
    passed: bool
    elapsed_ms: float
    message: str
    http_statuses: list[int]


class AcceptanceFailure(AssertionError):
    pass


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def approx(a: Any, b: float, tol: float = 0.01) -> bool:
    try:
        return math.isclose(float(a), float(b), abs_tol=tol)
    except (TypeError, ValueError):
        return False


def pick(obj: Any, names: list[str], default=None):
    if not isinstance(obj, dict):
        return default
    for name in names:
        if name in obj:
            return obj[name]
    return default


def request_json(
    base_url: str,
    method: str,
    path: str,
    body: Any = None,
    timeout: float = 10.0,
) -> HttpResult:
    url = base_url.rstrip("/") + path
    data = None
    headers = {"Accept": "application/json"}

    if body is not None:
        data = json.dumps(body).encode("utf-8")
        headers["Content-Type"] = "application/json"

    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    started = time.perf_counter()

    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            raw = resp.read().decode("utf-8", errors="replace")
            status = resp.status
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", errors="replace")
        status = e.code
    except Exception as e:
        elapsed = (time.perf_counter() - started) * 1000
        raise AcceptanceFailure(f"HTTP request failed: {method} {url}: {e}") from e

    elapsed = (time.perf_counter() - started) * 1000

    try:
        parsed = json.loads(raw) if raw.strip() else None
    except json.JSONDecodeError:
        parsed = raw

    return HttpResult(status=status, body=parsed, raw=raw, elapsed_ms=elapsed)


class Suite:
    def __init__(self, base_url: str, contract: dict, run_id: str, timeout: float):
        self.base_url = base_url
        self.contract = contract
        self.run_id = re.sub(r"[^A-Za-z0-9_-]", "_", run_id)
        self.timeout = timeout
        self.alias = contract["field_aliases"]
        self.status_cfg = contract["expected_status"]
        self.results: list[TestResult] = []
        self._current_statuses: list[int] = []

    def http(self, method: str, path: str, body=None) -> HttpResult:
        r = request_json(self.base_url, method, path, body, self.timeout)
        self._current_statuses.append(r.status)
        return r

    def expect_status(self, result: HttpResult, key: str):
        allowed = self.status_cfg[key]
        if result.status not in allowed:
            raise AcceptanceFailure(
                f"expected HTTP {allowed}, got {result.status}; body={result.raw[:800]}"
            )

    def username(self, test_id: str, suffix: str = "") -> str:
        value = f"exp_{self.run_id}_{test_id.lower()}"
        if suffix:
            value += "_" + suffix
        return value[:60]

    def create_user(self, test_id: str, suffix: str = "") -> tuple[Any, str]:
        username = self.username(test_id, suffix)
        r = self.http("POST", "/api/users", {"username": username})
        self.expect_status(r, "create_user")

        uid = pick(r.body, self.alias["user_id"])
        if uid is None:
            raise AcceptanceFailure(f"user id not found in create-user response: {r.raw[:800]}")
        return uid, username

    def get_cash(self, body: Any):
        return pick(body, self.alias["cash"])

    def get_price(self, body: Any):
        if isinstance(body, (int, float)):
            return body
        return pick(body, self.alias["price"])

    def get_holdings(self, portfolio: Any) -> list:
        h = pick(portfolio, self.alias["holdings"], [])
        return h if isinstance(h, list) else []

    def find_holding(self, portfolio: Any, symbol: str):
        for item in self.get_holdings(portfolio):
            item_symbol = pick(item, self.alias["symbol"])
            if str(item_symbol).upper() == symbol.upper():
                return item
        return None

    def portfolio(self, uid) -> HttpResult:
        return self.http("GET", f"/api/users/{uid}/portfolio")

    def buy(self, uid, symbol: str, shares: int) -> HttpResult:
        return self.http(
            "POST",
            "/api/trades/buy",
            {"userId": uid, "symbol": symbol, "shares": shares},
        )

    def sell(self, uid, symbol: str, shares: int) -> HttpResult:
        return self.http(
            "POST",
            "/api/trades/sell",
            {"userId": uid, "symbol": symbol, "shares": shares},
        )

    def execute(self, test_id: str, name: str, fn):
        self._current_statuses = []
        started = time.perf_counter()
        try:
            fn()
            passed = True
            message = "PASS"
        except Exception as e:
            passed = False
            message = str(e)
        elapsed = (time.perf_counter() - started) * 1000

        self.results.append(
            TestResult(
                id=test_id,
                name=name,
                passed=passed,
                elapsed_ms=round(elapsed, 3),
                message=message,
                http_statuses=list(self._current_statuses),
            )
        )

    # T01
    def t01(self):
        r = self.http("GET", "/api/quotes/AAPL")
        self.expect_status(r, "quote_valid")
        price = self.get_price(r.body)
        if not approx(price, 200.00):
            raise AcceptanceFailure(f"AAPL expected 200.00, got {price!r}; body={r.raw[:800]}")

    # T02
    def t02(self):
        r = self.http("GET", "/api/quotes/INVALID")
        self.expect_status(r, "quote_invalid")

    # T03
    def t03(self):
        uid, _ = self.create_user("T03")
        r = self.portfolio(uid)
        self.expect_status(r, "portfolio")
        cash = self.get_cash(r.body)
        if not approx(cash, 10000.00):
            raise AcceptanceFailure(f"new user cash expected 10000.00, got {cash!r}")

    # T04
    def t04(self):
        username = self.username("T04")
        r1 = self.http("POST", "/api/users", {"username": username})
        self.expect_status(r1, "create_user")
        r2 = self.http("POST", "/api/users", {"username": username})
        self.expect_status(r2, "duplicate_user")

    # T05
    def t05(self):
        uid, _ = self.create_user("T05")
        r = self.buy(uid, "AAPL", 10)
        self.expect_status(r, "buy_valid")
        p = self.portfolio(uid)
        self.expect_status(p, "portfolio")
        cash = self.get_cash(p.body)
        if not approx(cash, 8000.00):
            raise AcceptanceFailure(f"cash after BUY expected 8000.00, got {cash!r}")
        h = self.find_holding(p.body, "AAPL")
        if h is None:
            raise AcceptanceFailure(f"AAPL holding not found; portfolio={p.raw[:800]}")
        shares = pick(h, self.alias["shares"])
        if not approx(shares, 10):
            raise AcceptanceFailure(f"AAPL shares expected 10, got {shares!r}")

    # T06
    def t06(self):
        uid, _ = self.create_user("T06")
        r = self.buy(uid, "MSFT", 26)  # 26 * 400 = 10400 > 10000
        self.expect_status(r, "buy_insufficient")
        p = self.portfolio(uid)
        self.expect_status(p, "portfolio")
        cash = self.get_cash(p.body)
        if not approx(cash, 10000.00):
            raise AcceptanceFailure(f"failed BUY changed cash: expected 10000.00, got {cash!r}")

    # T07
    def t07(self):
        uid, _ = self.create_user("T07")
        r = self.buy(uid, "AAPL", 0)
        self.expect_status(r, "shares_zero")

    # T08
    def t08(self):
        uid, _ = self.create_user("T08")
        self.expect_status(self.buy(uid, "AAPL", 10), "buy_valid")
        self.expect_status(self.buy(uid, "MSFT", 5), "buy_valid")
        p = self.portfolio(uid)
        self.expect_status(p, "portfolio")

        cash = self.get_cash(p.body)
        if not approx(cash, 6000.00):
            raise AcceptanceFailure(f"portfolio cash expected 6000.00, got {cash!r}")

        aapl = self.find_holding(p.body, "AAPL")
        msft = self.find_holding(p.body, "MSFT")
        if aapl is None or msft is None:
            raise AcceptanceFailure(f"expected AAPL and MSFT holdings; body={p.raw[:1000]}")
        if not approx(pick(aapl, self.alias["shares"]), 10):
            raise AcceptanceFailure("AAPL expected 10 shares")
        if not approx(pick(msft, self.alias["shares"]), 5):
            raise AcceptanceFailure("MSFT expected 5 shares")

        total = pick(p.body, self.alias["portfolio_total"])
        if total is not None and not approx(total, 10000.00):
            raise AcceptanceFailure(f"portfolio total expected 10000.00, got {total!r}")

    # T09
    def t09(self):
        uid, _ = self.create_user("T09")
        self.expect_status(self.buy(uid, "AAPL", 10), "buy_valid")
        r = self.sell(uid, "AAPL", 3)
        self.expect_status(r, "sell_valid")
        p = self.portfolio(uid)
        self.expect_status(p, "portfolio")

        cash = self.get_cash(p.body)
        if not approx(cash, 8600.00):
            raise AcceptanceFailure(f"cash after SELL expected 8600.00, got {cash!r}")
        aapl = self.find_holding(p.body, "AAPL")
        if aapl is None:
            raise AcceptanceFailure("AAPL holding not found after SELL")
        shares = pick(aapl, self.alias["shares"])
        if not approx(shares, 7):
            raise AcceptanceFailure(f"AAPL expected 7 shares after SELL, got {shares!r}")

    # T10
    def t10(self):
        uid, _ = self.create_user("T10")
        self.expect_status(self.buy(uid, "AAPL", 2), "buy_valid")
        before = self.portfolio(uid)
        self.expect_status(before, "portfolio")
        r = self.sell(uid, "AAPL", 3)
        self.expect_status(r, "oversell")
        after = self.portfolio(uid)
        self.expect_status(after, "portfolio")

        before_cash = self.get_cash(before.body)
        after_cash = self.get_cash(after.body)
        if not approx(before_cash, after_cash):
            raise AcceptanceFailure(
                f"oversell changed cash: before={before_cash!r}, after={after_cash!r}"
            )
        h = self.find_holding(after.body, "AAPL")
        if h is None or not approx(pick(h, self.alias["shares"]), 2):
            raise AcceptanceFailure("oversell changed AAPL position")

    # T11
    def t11(self):
        uid, _ = self.create_user("T11")
        self.expect_status(self.buy(uid, "AAPL", 4), "buy_valid")
        self.expect_status(self.sell(uid, "AAPL", 1), "sell_valid")
        r = self.http("GET", f"/api/users/{uid}/trades")
        self.expect_status(r, "history")

        if not isinstance(r.body, list):
            raise AcceptanceFailure(f"trade history expected JSON array, got {type(r.body).__name__}")

        types = [
            str(pick(x, self.alias["trade_type"], "")).upper()
            for x in r.body
            if isinstance(x, dict)
        ]
        if "BUY" not in types or "SELL" not in types:
            raise AcceptanceFailure(f"history should contain BUY and SELL; types={types}")

    # T12
    def t12(self):
        unknown = "00000000-0000-0000-0000-000000000001"
        r = self.http("GET", f"/api/users/{unknown}/portfolio")
        self.expect_status(r, "unknown_user")

    def run_all(self):
        tests = [
            ("T01", "valid quote", self.t01),
            ("T02", "invalid symbol", self.t02),
            ("T03", "create user with initial cash", self.t03),
            ("T04", "duplicate username", self.t04),
            ("T05", "valid buy", self.t05),
            ("T06", "insufficient cash", self.t06),
            ("T07", "zero shares rejected", self.t07),
            ("T08", "portfolio calculation", self.t08),
            ("T09", "valid sell", self.t09),
            ("T10", "oversell leaves state unchanged", self.t10),
            ("T11", "trade history", self.t11),
            ("T12", "unknown user", self.t12),
        ]
        for tid, name, fn in tests:
            self.execute(tid, name, fn)
        return self.results


def main():
    here = Path(__file__).resolve().parent
    ap = argparse.ArgumentParser()
    ap.add_argument("--base-url", default="http://localhost:8080")
    ap.add_argument("--run-id", default=f"manual-{int(time.time())}")
    ap.add_argument("--timeout", type=float, default=10.0)
    ap.add_argument("--contract", type=Path, default=here / "acceptance_contract.json")
    ap.add_argument("--output", type=Path)
    args = ap.parse_args()

    contract = load_json(args.contract)
    suite = Suite(args.base_url, contract, args.run_id, args.timeout)
    results = suite.run_all()

    passed = sum(1 for x in results if x.passed)
    failed = len(results) - passed

    print("=" * 88)
    print("BLACK-BOX ACCEPTANCE SUITE")
    print("=" * 88)
    print(f"Base URL : {args.base_url}")
    print(f"Run ID   : {args.run_id}")
    print()

    for r in results:
        mark = "PASS" if r.passed else "FAIL"
        print(f"{r.id}  {mark:4}  {r.name:36} {r.elapsed_ms:9.3f} ms  HTTP={r.http_statuses}")
        if not r.passed:
            print(f"     {r.message}")

    summary = {
        "run_id": args.run_id,
        "base_url": args.base_url,
        "total": len(results),
        "passed": passed,
        "failed": failed,
        "pass_rate": passed / len(results) if results else 0.0,
        "tests": [asdict(x) for x in results],
    }

    print()
    print(f"RESULT: {passed}/{len(results)} passed")

    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(
            json.dumps(summary, indent=2, ensure_ascii=False),
            encoding="utf-8",
        )
        print(f"JSON   : {args.output}")

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
