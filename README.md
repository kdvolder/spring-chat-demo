# Spring Boot Chat Demo with Security

A simple WebSocket-based chat application built with Spring Boot, demonstrating:

## Features

- **WebSocket Communication**: Real-time messaging using STOMP over WebSocket
- **Spring Security Integration**: Form-based authentication with session management
- **SAML 2.0 Authentication**: Okta IdP integration alongside form login
- **Multi-Chain Security**: Separate security configurations for different endpoints
- **User Authentication**: In-memory users (alice/bob) + SAML authentication
- **REST API**: User info endpoint (`/api/user`) with proper security
- **Real Username Display**: Shows authenticated user's name in chat and UI

## Quick Start (Form Login Only)

1. **Run the application**: `./mvnw spring-boot:run`
2. **Login**: Use `alice/password` or `bob/password`
3. **Chat**: Type messages in the input field
4. **API Testing**: `curl -u alice:password http://localhost:8080/api/user`

## SAML Setup Instructions

To enable SAML authentication with Okta:

### Step 1: Create Okta Developer Account

1. Go to https://developer.okta.com/signup/
2. Sign up with your email (free)
3. Choose your subdomain (e.g., `dev-123456.okta.com`)
4. Complete MFA setup

### Step 2: Create SAML Application in Okta

1. **In Okta Admin Console**: Applications → Create App Integration
2. **Select**: SAML 2.0
3. **App Name**: "Spring Chat Demo" (or your choice)
4. **SAML Settings**:
   - **Single sign-on URL**: `http://localhost:8080/login/saml2/sso/okta`
   - **Use this for Recipient URL and Destination URL**: ✅ Check
   - **Audience URI (SP Entity ID)**: `http://localhost:8080/saml2/service-provider-metadata/okta`
   - **Name ID format**: EmailAddress
   - **Application username**: Email
5. **Finish** and **copy the metadata URL** (looks like: `https://dev-xxxxx.okta.com/app/xxxxx/sso/saml/metadata`)

### Step 3: Assign Users

1. **In your SAML app**: Go to "Assignments" tab
2. **Assign yourself**: Assign → Assign to People → Select your user → Save

### Step 4: Update Application Configuration

**Option A**: Copy and customize the template:
```bash
cp src/main/resources/application.yml.template src/main/resources/application.yml
# Then edit application.yml and replace YOUR_OKTA_METADATA_URL_HERE with your actual URL
```

**Option B**: Manually update `src/main/resources/application.yml`:

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          okta:
            assertingparty:
              metadata-uri: YOUR_OKTA_METADATA_URL_HERE
```

### Step 5: Test SAML Flow

1. **Start app**: `./mvnw spring-boot:run`
2. **Go to**: http://localhost:8080
3. **Click**: "okta" link under "Login with SAML 2.0"
4. **Login with your Okta credentials**
5. **Success**: You should be authenticated and see your email in chat

## Security Architecture

### Multiple Security Filter Chains

1. **Assets Chain** (`/assets/**`): Public access for static resources
2. **WebSocket Chain** (`/ws/**`): Authenticated access for WebSocket connections  
3. **API Chain** (`/api/**`): Authenticated REST endpoints with 401 responses
4. **Main App Chain**: Form login + SAML login for web pages

### Authentication Methods

- **Form-based**: In-memory users (alice/bob) with password authentication
- **SAML 2.0**: Okta IdP integration with metadata-uri configuration
- **Session-based**: HTTP-only cookies for browser security
- **HTTP Basic**: Optional API access for testing/development

### CSRF Protection

- **Enabled**: For web forms and most endpoints
- **Disabled**: For SAML endpoints (`/saml2/**`) and API endpoints (`/api/**`)
- **Reasoning**: SAML assertions are cryptographically signed; APIs use stateless auth

### SAML Service Provider Metadata

This application publishes **SAML metadata** that describes its configuration to Identity Providers. This metadata endpoint acts like a "configuration descriptor" that IdPs can read to automatically set up SAML integration.

**Endpoint**: `/saml2/service-provider-metadata/{registrationId}`

**Example for Okta registration**:
```bash
curl http://localhost:8080/saml2/service-provider-metadata/okta
```

**Response**:
```xml
<?xml version="1.0" encoding="UTF-8"?><md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="http://localhost:8080/saml2/service-provider-metadata/okta">
    <md:SPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
        <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="http://localhost:8080/login/saml2/sso/okta" index="1"/>
    </md:SPSSODescriptor>
</md:EntityDescriptor>
```

**Response includes**:
- **Entity ID**: Unique identifier for this service provider
- **Assertion Consumer Service (ACS) URL**: Where IdPs should send SAML responses
- **Supported bindings**: HTTP-POST, HTTP-Redirect, etc.
- **Protocol information**: SAML 2.0 capabilities

**Use cases**:
- **IdP auto-configuration**: Import metadata URL into Okta/Azure AD for automatic setup
- **Integration documentation**: Share with partners for B2B SAML setup
- **Configuration verification**: Validate current SAML settings
- **Troubleshooting**: Confirm entity IDs and URLs match IdP configuration

**Note**: In Spring Security 5.x, this endpoint requires manual configuration via `Saml2MetadataFilter`. Spring Security 6.x enables it automatically.

## Key Security Learnings

- **HTTP status codes**: `401 Unauthorized` vs `403 Forbidden`
- **Client-side security**: Never trust user-provided identity claims
- **Principal injection**: Server-side user identity in WebSocket handlers
- **Session cookie security**: HttpOnly, automatic browser inclusion
- **SAML URL patterns**: Spring Security 5.x uses `/login/saml2/sso/{registrationId}`
- **Browser-mediated SAML**: IdP posts assertions via browser redirects

## Troubleshooting

### SAML Login Issues

- **403 Forbidden**: Check CSRF exemption for `/saml2/**` endpoints
- **URL mismatch**: Ensure ACS URL uses `/login/saml2/sso/okta` pattern
- **User not assigned**: Assign your user to the SAML app in Okta
- **Metadata issues**: Verify metadata URL is accessible and correct

### WebSocket Issues

- **Connection failures**: Check authentication and session management
- **Message routing**: Ensure messages go through `/app/` prefix, not direct to `/topic/`

## Technologies

- Spring Boot 2.7.18
- Spring Security 5.7.11 with SAML 2.0 support
- Spring WebSocket/STOMP
- SockJS for WebSocket fallback
- Okta SAML Identity Provider
- Vanilla JavaScript frontend

---

*Note: This project was created as a learning exercise for Spring Security WebSocket integration, SAML 2.0 authentication, and CSRF migration patterns for OpenRewrite recipe development.* 