#!/usr/bin/env pwsh

docker compose -f .\\docker\\docker-compose.yml --env-file .\\docker\\compose.env up -d --force-recreate --build 