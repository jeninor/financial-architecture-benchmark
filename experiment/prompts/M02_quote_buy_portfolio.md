Task ID: M02

Architecture:
Monolith

Task:
Implement stock quote retrieval, stock purchase
and portfolio calculation.

AI role:
Code generation and architectural assistance.

Functional requirements:
- GET /api/quotes/{symbol}
- POST /api/trades/buy
- GET /api/users/{userId}/portfolio
- Validate positive shares
- Validate stock symbol
- Validate available cash
- Persist trades
- Update user cash
- Calculate portfolio based on trades
