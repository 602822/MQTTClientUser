# MQTT Client User

A secure Java-based MQTT client application that integrates with Keycloak for OAuth2/OpenID Connect authentication and enables real-time IoT device communication.

## Overview

This project implements a robust MQTT subscriber client that:
- Authenticates users via **Keycloak** using the OAuth2 Authorization Code Flow with PKCE (Proof Key for Code Exchange)
- Connects to an MQTT broker using JWT tokens for secure authentication
- Automatically refreshes expired JWT tokens without user intervention
- Subscribes to MQTT topics and receives real-time messages

## Features

- **Secure Authentication**: OAuth2 with PKCE security extension to prevent authorization code interception
- **JWT Token Management**: Automatic token parsing, expiration detection, and refresh
- **MQTT Connectivity**: Eclipse Paho MQTT client for reliable message publishing/subscription
- **Browser Integration**: Automatic browser launch for Keycloak login flow
- **Async Operations**: Non-blocking authorization code retrieval using CompletableFuture
- **Token Expiration Handling**: Proactive token refresh 60 seconds before expiration

## Architecture

### Core Components

- **Main.java** - Application entry point; orchestrates the complete authentication and MQTT subscription flow
- **KeycloakAuth.java** - Handles OAuth2/PKCE token exchange and token refresh with Keycloak
- **AuthServer.java** - Lightweight HTTP server that captures the OAuth2 redirect callback with authorization code
- **MQTTSubClient.java** - Manages MQTT client connection, subscription, and token refresh
- **PKCEUtils.java** - Implements PKCE security (code verifier/challenge generation and authorization URL creation)
- **JWTUtils.java** - JWT token parsing and expiration time verification
- **Config.java** - Non-sensitive configuration (Keycloak realm, base URL, MQTT broker URL)
- **ConfigLoader.java** - Loads sensitive credentials from external properties file

## Technology Stack

- **Java**: 21
- **Build Tool**: Maven
- **MQTT Client**: Eclipse Paho (`org.eclipse.paho.client.mqttv3`)
- **JWT Processing**: Nimbus JOSE+JWT (`com.nimbusds:nimbus-jose-jwt`)
- **JSON Parsing**: org.json

## Prerequisites

- Java 21 or higher
- Keycloak server configured with OpenID Connect support
- MQTT broker (e.g., Mosquitto) with JWT token authentication support
- `config.properties` file in the project root with:
  ```properties
  CLIENT_ID=<your-keycloak-client-id>
  REDIRECT_URI=http://localhost:8081/callback
  ```

## Installation

### 1. Clone the Repository
```bash
git clone https://github.com/602822/MQTTClientUser.git
cd MQTTClientUser
```

### 2. Configure Credentials
Create a `config.properties` file in the project root:
```properties
CLIENT_ID=your-keycloak-client-id
REDIRECT_URI=http://localhost:8081/callback
```

### 3. Build the Project
```bash
mvn clean install
```

### 4. Run the Application
```bash
mvn exec:java -Dexec.mainClass="org.example.Main"
```

## Workflow

### Authentication Flow

1. **Generate PKCE Parameters**
   - Code verifier and code challenge are generated using cryptographically secure random values
   - Protects against authorization code interception attacks

2. **Launch Keycloak Login**
   - Application opens default browser with Keycloak login URL
   - Falls back to console message if browser unavailable

3. **Receive Authorization Code**
   - Local HTTP server listens on port 8081 for OAuth2 redirect
   - Extracts authorization code from callback query parameters

4. **Exchange Code for JWT Token**
   - Sends authorization code + code verifier to Keycloak
   - Keycloak validates code verifier against previously stored code challenge
   - Returns access token, refresh token, and expiration time

5. **Connect to MQTT Broker**
   - Uses client ID (derived from Keycloak client ID and username)
   - Uses JWT token as password for authentication

6. **Token Refresh Loop**
   - Monitors token expiration (checks every second)
   - Refreshes token 60 seconds before expiration
   - Reconnects to broker with new token to maintain connection

### MQTT Subscription

After successful authentication and connection:
1. User enters topic name via console
2. Client subscribes to the specified topic
3. Receives and logs all messages published to that topic
4. Main thread continues running to keep JVM alive for message reception

## Security Considerations

- **PKCE**: Prevents authorization code interception attacks
- **JWT Token**: Used instead of passwords for MQTT authentication
- **Token Refresh**: Automatic refresh prevents token expiration during operation
- **Sensitive Data Separation**: Client credentials stored in external `config.properties`, not in code
- **Secure Random Generation**: Uses `SecureRandom` and `MessageDigest` for PKCE implementation

## Configuration Details

### Config.java
```java
KEYCLOAK_REALM = "smartocean-testrealm"
BASE_URL = "http://158.39.77.107:8081"
BROKER_URL = "tcp://158.39.77.107:1883"
```

Modify these values to match your Keycloak and MQTT broker deployment.

## Dependencies

```xml
<!-- MQTT Client -->
<dependency>
    <groupId>org.eclipse.paho</groupId>
    <artifactId>org.eclipse.paho.client.mqttv3</artifactId>
    <version>1.2.5</version>
</dependency>

<!-- JSON Processing -->
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20250517</version>
</dependency>

<!-- JWT Processing -->
<dependency>
    <groupId>com.nimbusds</groupId>
    <artifactId>nimbus-jose-jwt</artifactId>
    <version>10.5</version>
</dependency>
```

## Troubleshooting

### Token Exchange Fails
- Verify Keycloak server is running and accessible
- Ensure `CLIENT_ID` in `config.properties` matches Keycloak client configuration
- Check that `REDIRECT_URI` is registered in Keycloak client settings

### MQTT Connection Fails
- Verify MQTT broker is running on the configured `BROKER_URL`
- Check that broker supports JWT token authentication
- Ensure JWT token is valid and not expired

### Topic Subscription Fails
- Verify topic name is correctly formatted
- Check broker ACL rules allow subscription to the topic

## Future Enhancements

- Support for MQTT message publishing
- Multiple topic subscriptions
- Configurable token refresh threshold
- Support for client certificates (TLS/SSL)
- Message filtering and processing
- Data persistence layer

## License

This project is open source and available under the MIT License.

## Author

602822

## Support

For issues, questions, or contributions, please open an issue on the GitHub repository.
