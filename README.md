## Memoria – Dockerized Microservices Setup

This repository contains 3 services, a gateway and a docker-compose.yaml file to set up and run the services that are part of the Memoria application.

### Prerequisites

Docker must be installed on your machine to run the application in a production-like environment using containers.

### How to Run the App
1. Clone this repository into that folder.

Your directory structure should now look like this:

    /your-app-folder
    ├── memoria-auth
    ├── memoria-data
    ├── memoria-gateway
    ├── memoria-training
    └── docker-compose.yaml, .env.*.example and .env.*.dev files

### Configuration

Each service has a corresponding .env.service_name.example file that contains the required environment variables.
To configure the services:

Copy or rename each .env.service_name.example file by removing the .example extension.

    Example:
    Rename .env.auth.example → .env.auth

The values in the example files are safe for development use, but feel free to customize them before renaming.

**_!_ Note: If you're running a service with the dev profile (e.g., from an IDE), and you've modified .env.service_name, make sure to also update the corresponding .env.service_name.dev file inside the service folder to keep them in sync.**

### Start the App

In the parent folder (where docker-compose.yaml is located), run:

```shell
docker compose up
```

This will build and start all Memoria services via Docker.