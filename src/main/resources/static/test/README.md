# Security Test Suite

This directory contains a comprehensive set of security tests for the Spring Chat Demo application. Each test file focuses on a specific security aspect or vulnerability type.

## Test Files Organization

### 1. CORS and Origin Policy Tests
These tests examine how browsers and the server handle cross-origin requests:

- `modern-browser-same-origin-policy.html`: Tests how modern browsers enforce Same-Origin Policy
  - Validates same-origin vs cross-origin WebSocket behavior
  - Documents evolution of browser security policies
  - Demonstrates why some Spring Security 5.8 assumptions may no longer apply

- `cors-policy-comparison.html`: Compares CORS behavior across protocols
  - Tests HTTP vs WebSocket vs SockJS
  - Shows how different protocols handle cross-origin restrictions
  - Demonstrates why WebSocket security needs additional server-side protection

- `cross-origin-websocket.html`: Demonstrates legitimate cross-origin WebSocket usage
  - Shows proper authentication for cross-origin connections
  - Documents correct security headers and configurations
  - Example of valid cross-origin WebSocket setup

### 2. Attack Simulations
These tests simulate various attack vectors to verify security measures:

- `malicious-client-attacks.html`: Simulates common malicious client behaviors
  - REST API data theft attempts
  - Malicious form submissions
  - WebSocket connection hijacking
  - Hidden iframe attacks
  - Demonstrates how each attack is prevented

### 3. Security Feature Tests
These tests validate specific security mechanisms:

- `csrf-defaults.html`: Tests CSRF protection behavior
  - Form submission without CSRF token
  - Session cookie behavior
  - CSRF token generation and validation
  - Documents expected security responses

- `websocket-routing-security.html`: Tests WebSocket message routing security
  - Validates message destination restrictions
  - Tests controller bypass prevention
  - Verifies sender identity protection
  - Documents proper vs improper message routing

## Test Categories and Security Layers

### Browser Security
- Same-Origin Policy enforcement
- Cross-Origin Resource Sharing (CORS)
- Modern browser security evolution

### Protocol Security
- HTTP request protection
- WebSocket connection security
- SockJS fallback behavior

### Application Security
- CSRF protection
- Authentication requirements
- Message routing rules
- Identity verification

## Running the Tests

1. Start the application
2. Login as either test user:
   - Username: alice or bob
   - Password: password
3. Open each test file in a browser
4. Check browser console for detailed test results

## Expected Results

Each test file includes specific expectations and success criteria. Generally:

### Security Boundaries
- Cross-origin HTTP requests: Blocked unless explicitly allowed
- WebSocket connections: Require authentication
- Direct topic access: Blocked by message security

### Protection Mechanisms
- CSRF: Active on web forms
- Authentication: Required for WebSocket
- Message Routing: Enforced through controller
- Origin Validation: Configurable per endpoint

## Test Development Guidelines

When adding new security tests:
1. Focus each file on a single security concept
2. Document expected behavior clearly
3. Include console logging for visibility
4. Test both positive and negative cases
5. Update this README with test details 