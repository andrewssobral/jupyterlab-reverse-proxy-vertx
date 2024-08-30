# JupyterLab Reverse Proxy with Vert.x

This project implements a reverse proxy for [JupyterLab](https://jupyter.org/) using [Vert.x](https://vertx.io/), a reactive toolkit for building Java applications. This reverse proxy is designed to forward requests from a public-facing port to a JupyterLab instance running on a different port, providing an additional layer of security and customization options.

## Features

- **Secure Proxying:** Handles HTTP requests and WebSocket connections, forwarding them securely to JupyterLab.
- **Customizable Headers:** Modifies and manages headers for improved security, including setting a more permissive Content Security Policy and adjusting cookies.
- **WebSocket Support:** Maintains WebSocket connections, ensuring smooth communication between the client and JupyterLab.
- **Cross-Origin Resource Sharing (CORS):** Configured to allow cross-origin requests, which is essential for various front-end applications.

## Requirements

- Java 8 or higher
- Maven
- JupyterLab (tested with 4.2.5)

## Getting Started

### Installation

1. **Clone the repository:**

    ```bash
    git clone https://github.com/andrewssobral/jupyterlab-reverse-proxy-vertx.git
    cd jupyterlab-reverse-proxy-vertx
    ```

2. **Build the project:**

    Use Maven to compile and build the project:

    ```bash
    ./run-build.sh
    ```

3. **Run the reverse proxy:**

    Start the reverse proxy using the provided script:

    ```bash
    ./run-reverse-proxy.sh
    ```

4. **Run JupyterLab:**

    Ensure that JupyterLab is running on port `9999`:

    ```bash
    ./run-jupyterlab.sh
    ```

### Configuration

- **Port:** The reverse proxy listens on port `8081`. You can modify this in the `App.java` file.
- **JupyterLab Connection:** By default, the reverse proxy forwards requests to `localhost:9999`. Update this configuration in the `App.java` file if needed.

### Usage

After starting the proxy, you can access JupyterLab through the proxy server at `http://localhost:8081`. All HTTP and WebSocket requests will be forwarded to the JupyterLab instance running on port `9999`.

### Testing

Unit tests are included in the project and can be run with:

```bash
mvn test
```

## License

This project is licensed under the MIT License - see the [LICENSE](./LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a pull request or open an issue for any bugs or feature requests.
